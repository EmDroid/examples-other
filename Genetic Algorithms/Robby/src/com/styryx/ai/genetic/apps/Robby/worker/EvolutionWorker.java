package com.styryx.ai.genetic.apps.Robby.worker;

import java.util.*;
import java.util.concurrent.Future;

import javax.swing.*;

import com.styryx.ai.genetic.apps.Robby.exception.RobbyException;
import com.styryx.ai.genetic.apps.Robby.gui.RobbyPanel;
import com.styryx.ai.genetic.apps.Robby.gui.RobbyPanel.World;
import com.styryx.ai.genetic.apps.Robby.worker.Playground;
import com.styryx.ai.genetic.selector.*;

public class EvolutionWorker extends SwingWorker<Boolean, EvolutionWorker.GenerationResult> {
	
	public class GenerationResult {

		private final int m_gen;
		private final RankedStrategies m_strategies;

		public GenerationResult(int gen, RankedStrategies rankedPopulation) {
			m_gen = gen;
			m_strategies = rankedPopulation;
		}
		
		public int gen() {
			return m_gen;
		}
		
		public RankedStrategies strategies() {
			return m_strategies;
		}
		
	}

	public interface ResultListener {
		void logGeneration(GenerationResult result);
		void finalizeGui();
		void processResult(double score, int generation, Playground.Strategy[] population);
	}
	
	protected class EvolutionInterrupted extends RobbyException {
		public EvolutionInterrupted() {
			super(RobbyPanel.getMessages(m_locale).getString("InterruptedByUser", "Interrupted by user."));
		}
	}

	final private Locale m_locale;
	final private ResultListener m_listener;
	
	private final int m_playgroundSize;
	private Playground.Strategy[] m_population;
	private final int m_sessions;
	private final int m_actionsPerSession;
	private final int m_generations;
	private final double m_mutationProbability;
	private final int m_selection;
	private final boolean m_crossover;
	private final int m_threads;
	
	private double m_score;
	
	private boolean m_result;
	private Exception m_thrown = null;

	public EvolutionWorker(Locale locale, ResultListener listener, int playgroundSize,
			World world, int sessions, int actionsPerSession,
			int generations, double mutationProbability, int selection, boolean crossover,
			int threads) {
		m_locale = locale;
		m_listener = listener;
		m_playgroundSize = playgroundSize;
		m_population = world.getPopulation();
		m_sessions = sessions;
		m_actionsPerSession = actionsPerSession;
		m_generations = generations;
		m_mutationProbability = mutationProbability;
		m_selection = selection;
		m_crossover = crossover;
		m_threads = threads;
		m_score = Integer.MIN_VALUE;
	}

	protected void done() {
		m_listener.finalizeGui();
		if (m_result) {
			// Successfully done.
			// Set the best score and the strategy chain.
			m_listener.processResult(m_score, m_generations, m_population);
		} else {
			if (isCancelled()) {
				// The evolution was canceled.
//				m_thrown = new EvolutionInterrupted();
			}
			final boolean thrown = m_thrown != null;
			if (thrown) {
				RobbyPanel.logException(m_thrown,
						RobbyPanel.getMessages(m_locale).getString("EvolutionFailed", "Evolution failed"),
						m_locale);
			}
		}
	}
	
	protected void process(List< GenerationResult > result) {
		// Process intermediate results.
		if (isCancelled()) {
			return;
		}
		for (int i = 0; i < result.size(); ++i) {
			m_listener.logGeneration(result.get(i));
		}
	}
	
	private class PlayThread extends Thread {
		private final Future<Boolean> m_runner;
		private final Random m_randomizer;
		private final Playground.Strategy[] m_population;
		private final int m_start;
		private final int m_end;
		private final StrategyGenom[] m_results;
		public PlayThread(Future<Boolean> runner, Random randomizer, Playground.Strategy[] population, int start, int end, StrategyGenom[] results) {
			m_runner = runner;
			m_randomizer = randomizer;
			m_population = population;
			m_start = start;
			m_end = end;
			m_results = results;
		}
		public void run() {
			try {
				// Play selected strategies.
				for (int i = m_start; i < m_end; ++i) {
					// Process all sessions.
					// Compute average score from the sessions.
					double score = Playground.processStrategy(m_runner, m_randomizer, m_population[i],
							m_playgroundSize, m_sessions, m_actionsPerSession, m_locale);
					m_results[i] = new StrategyGenom(score, m_population[i]);
//					int score = 0;
//					for (int ses = 0; ses < m_sessions; ++ses) {
//						// Check for cancel request.
//						if (isCancelled()) {
//							return;
//						}
//						// Create new session (playground).
//						Playground playground = new Playground(m_playgroundSize);
//						playground.setStrategy(m_population[i]);
//						for (int step = 0; step < m_actionsPerSession; ++step) {
//							playground.step();
//						}
//						score += playground.getScore();
//					}
//					// Compute average score from the sessions.
//					double averageScore = score / (double)m_sessions;
//					m_results[i] = new RankedStrategy(averageScore, m_population[i]);
				}
			} catch (Exception e) {
				e.printStackTrace();
				m_thrown = e;
				cancel(false);
			}
		}
	}
	
	private class SelectionThread extends Thread {
		private final RankedStrategies m_strategies;
		private final Random m_randomizer;
		private final int m_start;
		private final int m_end;
		private final Playground.Strategy[] m_population;
		public SelectionThread(RankedStrategies ranked, Random randomizer, int start, int end, Playground.Strategy[] population) {
			m_strategies = ranked;
			m_randomizer = randomizer;
			m_start = start;
			m_end = end;
			m_population = population;
		}
		public void run() {
			try {
				// Select new strategies.
				int i = m_start;
				while (i < m_end) {
					// Check for cancel request.
					if (isCancelled()) {
						return;
					}
					// Roulette-wheel selection.
					if (RouletteWheelSelector.sortRequired(m_selection)) {
						// The strategies must be sorted.
						// Pre-sort the strategies to avoid the need of synchronization between threads.
						m_strategies.sort();
					}
					// Select the mates.
					RouletteWheelSelector<Playground.Strategy> selector = new RouletteWheelSelector<Playground.Strategy>(m_strategies, m_selection);
					Playground.Strategy[] parents = new Playground.Strategy[]{
							selector.select(m_randomizer),
							selector.select(m_randomizer)
						};
					Playground.Strategy[] children;
					if (m_crossover) {
						// Create crossover of the two mates.
						children = Playground.Strategy.crossover(parents[0], parents[1], m_mutationProbability, m_randomizer, m_locale);
					} else {
						// Only mutate the selected items.
						children = new Playground.Strategy[2];
						children[0] = Playground.Strategy.mutate(parents[0], m_mutationProbability, m_randomizer, m_locale);
						children[1] = Playground.Strategy.mutate(parents[1], m_mutationProbability, m_randomizer, m_locale);
					}
					m_population[i] = children[0];
					if (++i < m_end) {
						m_population[i] = children[1];
					}
					++i;
//					// Roulette-wheel selection.
//					// Select the mates.
//					RouletteWheelSelector selector = new RouletteWheelSelector(m_strategies, true);
//					Strategy parent1 = selector.select();
//					Strategy parent2 = selector.select();
//					// Create crossover of the two mates.
//					Strategy[] children = Strategy.crossover(parent1, parent2, m_mutationProbability);
//					m_population[i] = children[0];
//					if (++i < m_end) {
//						m_population[i] = children[1];
//					}
//					++i;
				}
			} catch (Exception e) {
				e.printStackTrace();
				m_thrown = e;
				cancel(false);
			}
		}
	}
	
	public class StrategyGenom extends RankedGenom<Playground.Strategy> {
		public StrategyGenom(double fitness, Playground.Strategy strategy) {
			super(fitness, strategy);
		}
	}
	
	public class RankedStrategies extends RankedPopulation<Playground.Strategy> {
		public RankedStrategies(StrategyGenom[] values) {
			super(values);
		}
		public RankedStrategies sort() {
			super.sort();
			return this;
		}
	}
	
	private RankedStrategies rankPopulation(int cpus, int populationBunch,
			PlayThread[] threads, Random[] randomizers) throws Exception {
		StrategyGenom[] results = new StrategyGenom[m_population.length];
		int start = 0;
		int left = m_population.length;
		int size, end;
		for (int cpu = 0; cpu < cpus; ++cpu) {
			size = Math.min(populationBunch, left);
			end = start + size;
			PlayThread thread = new PlayThread(this, randomizers[cpu], m_population, start, end, results);
			thread.start();
			threads[cpu] = thread;
			start = end;
			left -= size;
		}
		for (int cpu = 0; cpu < cpus; ++cpu) {
			threads[cpu].join()/* .wait()*/;
		}
//		for (int i = 0; i < m_population.length; ++i) {
//			// Compute average score from the sessions.
//			double score = Playground.processStrategy(this, m_population[i],
//					m_playgroundSize, m_sessions, m_actionsPerSession, m_locale);
//			results.add(new RankedGenom<Playground.Strategy>(score, m_population[i]));
//		}
		return new RankedStrategies(results);
	}
	
	protected Boolean doInBackground() throws Exception {
		try {
			// Do the stuff.
			if (isCancelled()) {
				return m_result = false;
			}
			int populationBunch = m_population.length / m_threads;
			if (0 != (m_population.length % m_threads)) {
				++populationBunch;
			}
			int selectionBunch = populationBunch;
			if (0 != (selectionBunch % 2)) {
				++selectionBunch;
			}
			Random[] randomizers = new Random[m_threads];
			for (int cpu = 0; cpu < m_threads; ++cpu) {
				randomizers[cpu] = new Random();
			}
			PlayThread[] playThreads = new PlayThread[m_threads];
			// Process all generations.
			int gen = 0;
			// Rank the initial population.
			//TODO Rank the population only if not ranked yet.
			RankedStrategies ranked = rankPopulation(m_threads, populationBunch, playThreads, randomizers);
			SelectionThread[] selectionThreads = new SelectionThread[m_threads];
			while (gen < m_generations) {
				// Select candidates for next level.
				Playground.Strategy[] newPopulation = new Playground.Strategy[m_population.length];
				int start = 0;
				int left = m_population.length;
				int size, end;
				for (int cpu = 0; cpu < m_threads; ++cpu) {
					size = Math.min(selectionBunch, left);
					end = start + size;
					SelectionThread thread = new SelectionThread(ranked, randomizers[cpu], start, end, newPopulation);
					thread.start();
					selectionThreads[cpu] = thread;
					start = end;
					left -= size;
				}
				for (int cpu = 0; cpu < m_threads; ++cpu) {
					selectionThreads[cpu].join()/* .wait()*/;
				}
				if (isCancelled()) {
					return m_result = false;
				}
//				int i = 0;
//				while (i < m_population.length) {
//					// Roulette-wheel selection.
//					// Select the mates.
//					RouletteWheelSelector<Playground.Strategy> selector = new RouletteWheelSelector<Playground.Strategy>(ranked, m_selection);
//					Playground.Strategy[] parents = new Playground.Strategy[]{selector.select(), selector.select()};
//					Playground.Strategy[] children;
//					if (m_crossover) {
//						// Create crossover of the two mates.
//						children = Playground.Strategy.crossover(parents[0], parents[1], m_mutationProbability, m_locale);
//					} else {
//						// Only mutate the selected items.
//						children = new Playground.Strategy[2];
//						children[0] = Playground.Strategy.mutate(parents[0], m_mutationProbability, m_locale);
//						children[1] = Playground.Strategy.mutate(parents[1], m_mutationProbability, m_locale);
//					}
//					newPopulation[i] = children[0];
//					if (++i < m_population.length) {
//						newPopulation[i] = children[1];
//					}
//					++i;
//				}
				m_population = newPopulation;
				ranked = rankPopulation(m_threads, populationBunch, playThreads, randomizers);
				// Process the current generation.
				publish(new GenerationResult(++gen, ranked.sort()));
			}
			// Assign current best chain.
			RankedGenom<Playground.Strategy> best = ranked.get(0);
			m_score = best.fitness();
		} catch (Exception e) {
			// Failure.
			e.printStackTrace();
			m_thrown = e;
			return m_result = false;
		}
		// Success.
		return m_result = true;
	}

}
