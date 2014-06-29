package com.styryx.ai.genetic.apps.Robby.gui.actions;

import java.awt.event.ActionEvent;
import java.util.Locale;

import com.styryx.ai.genetic.apps.Robby.gui.RobbyPanel;
import com.styryx.ai.genetic.apps.Robby.gui.RobbyPanel.World;
import com.styryx.ai.genetic.apps.Robby.worker.EvolutionWorker;
import com.styryx.ai.genetic.apps.Robby.worker.Playground;

public class ActionEvolve extends RobbyAction implements EvolutionWorker.ResultListener {
	
	private EvolutionWorker m_worker = null;
	
	public ActionEvolve(RobbyPanel main, Locale locale) {
		super(main, locale, RobbyPanel.getMessages(locale).getString("Evolve"),
				RobbyPanel.getMessages(locale).getString("ProcessEvolution", "Process the evolution"));
	}
	
	public void actionPerformed(ActionEvent evt) {
		m_main.ensureWorldCreated();
		// Start the evolution.
//		m_actionNew.setEnabled(false);
		setEnabled(false);
		m_main.getChartRange().setEnabled(false);
		m_main.getChartRangeBig().setEnabled(false);
		try {
//			JOptionPane.showMessageDialog(null, "Available processors: " + Runtime.getRuntime().availableProcessors());
			int generations = m_main.getGenerations();
			m_worker = new EvolutionWorker(m_locale, this, m_main.getPlaygroundSize(), m_main.getWorld(),
					m_main.getSessions(), m_main.getActionsPerSession(),
					generations, m_main.getMutations(), m_main.getSelectionType(), m_main.getCrossover(),
					m_main.getThreads());
			m_main.resetProgress(generations);
			m_main.clearChart();
			m_worker.execute();
		} catch (Exception e) {
			finalizeGui();
			RobbyPanel.logException(e,
					RobbyPanel.getMessages(m_locale).getString("CannotStartEvolution", "Cannot start evolution"),
					m_locale);
		}
	}
	
	public void logGeneration(EvolutionWorker.GenerationResult result) {
		int gen = result.gen();
		m_main.setProgress(gen);
		gen += m_main.getWorld().getGeneration();
		EvolutionWorker.RankedStrategies scores = result.strategies();
		StringBuffer str = new StringBuffer();
		str.append(RobbyPanel.getMessages(m_locale).getString("Generation") + " " + gen + ":");
		for (int i = 0; i < scores.size(); ++i) {
			str.append(" " + m_main.formatScore(scores.get(i).fitness()));
		}
		str.append('\n');
		m_main.getEvolutionLog().append(str.toString());
		m_main.chartAdd(gen, scores.get(0).fitness());
	}
	
	public void processResult(double score, int generation, Playground.Strategy[] population) {
		World world = m_main.getWorld();
		world.addGeneration(generation);
		world.setPopulation(population);
		m_main.setBestStrategy(score, population[0].getString());
		m_main.setDefaultButton(m_main.getCreateButton());
	}
	
	public void finalizeGui() {
		m_main.getChartRange().setEnabled(true);
		m_main.getChartRangeBig().setEnabled(true);
		setEnabled(true);
//		m_actionNew.setEnabled(true);
	}
	
	public void cancel() {
		if (null != m_worker) {
			m_worker.cancel(false);
		}
	}
	
}
