package com.styryx.ai.genetic.selector;

import com.styryx.ai.genetic.Genom;

public class RankedGenom< GenomType extends Genom > implements Comparable< RankedGenom< GenomType > > {

	private final double m_fitness;
	private final GenomType m_genom;
	
	public RankedGenom(double fitness, GenomType genom) {
		m_fitness = fitness;
		m_genom = genom;
	}

	public double fitness() {
		return m_fitness;
	}

	public GenomType genom() {
		return m_genom;
	}

	public int compareTo(RankedGenom<GenomType> right) {
		if (null == right) {
			return 1;
		}
		double leftFitness = fitness();
		double rightFitness = right.fitness();
		if (leftFitness < rightFitness) {
			return 1;
		} else if (leftFitness > rightFitness) {
			return -1;
		} else {
			return 0;
		}
	}

}
