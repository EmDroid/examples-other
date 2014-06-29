package com.styryx.ai.genetic.selector;

import java.util.Arrays;
import java.util.Random;

import com.styryx.ai.genetic.Genom;
import com.styryx.algorithm.AlgorithmException;
import com.styryx.algorithm.InvalidAlgorithm;

public class RouletteWheelSelector<GenomType extends Genom> {

	public static final int SELECTION_RANK_BASED = 0;
	public static final int SELECTION_FITNESS_BASED = 1;
	
	private static final Random randomizer = new Random();
	
	public static boolean sortRequired(int selection) {
		switch (selection) {
		case SELECTION_FITNESS_BASED:
			return true;
		default:
			return false;
		}
	}

	private final RankedPopulation<GenomType> m_strategies;
	private double m_rankTotal;
	private final double[] m_rankStarts;
	private final double[] m_rankEnds;

	public RouletteWheelSelector(RankedPopulation<GenomType> strategies, int selection) throws AlgorithmException {
		// Create the pool.
		m_strategies = strategies;
		m_rankTotal = 0;
		int size = m_strategies.size();
		m_rankStarts = new double[size];
		m_rankEnds = new double[size];
		// The worst possible score.
		double scoreWorst = 0.0;
		if (sortRequired(selection)) {
			strategies.sort();
		}
		for (int i = 0; i < size; ++i) {
			double prob;
			switch (selection) {
			case SELECTION_RANK_BASED:
				prob = size - i /* + 1 */;
				break;
			case SELECTION_FITNESS_BASED:
				double fitness = m_strategies.get(i).fitness();
				if (fitness < scoreWorst) {
					// Has to update the previous ranks.
					double diff = scoreWorst - fitness;
					double advance = 0.0;
					for (int j = 0; j < i; ++j) {
						m_rankStarts[j] += advance;
						advance += diff;
						m_rankEnds[j] += advance;
					}
					m_rankTotal += advance;
					scoreWorst = fitness;
				}
				prob = fitness - scoreWorst + 1.0 /* some minimal chance for all genoms */;
				if (prob < 0.0) {
					// Should not happen!
					throw new InvalidAlgorithm("Probability less than zero!");
				}
				break;
			default:
				throw new SelectorException("Invalid selection type: " + selection);
			}
			m_rankStarts[i] = m_rankTotal;
			m_rankEnds[i] = (m_rankTotal += prob);
		}
	}

	public GenomType select(Random randomizer) {
		double selector = randomizer.nextDouble() * m_rankTotal;
		int selected = Arrays.binarySearch(m_rankStarts, selector);
		if (selected < 0) {
			selected = -1 * (selected + 2);
		}
		if ((m_rankStarts[selected] > selector) || (m_rankEnds[selected] < selector)) {
			throw new RuntimeException("Selection error.");
		}
		return m_strategies.get(selected).genom();
	}

}
