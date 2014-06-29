package com.styryx.ai.genetic.selector;

import java.util.Arrays;
import com.styryx.ai.genetic.Genom;

public class RankedPopulation<GenomType extends Genom> /* extends ArrayList<RankedGenom<GenomType>> */ {
	
	private final RankedGenom<GenomType>[] m_values;
	
	private boolean m_sorted;
	
//	public RankedPopulation() {
//		super();
//		init();
//	}
//
//	public RankedPopulation(int initialCapacity) {
//		super(initialCapacity);
//		init();
//	}

	public RankedPopulation(RankedGenom<GenomType>[] values) {
//		super(values);
		m_values = values;
		init();
	}

	private void init() {
		m_sorted = false;
	}

	public RankedPopulation<GenomType> sort() {
		if (!m_sorted) {
			m_sorted = true;
//			Collections.sort(this);
			Arrays.sort(m_values);
		}
		return this;
	}
	
	public RankedGenom<GenomType> get(int index) {
		return m_values[index];
	}

	public int size() {
		return m_values.length;
	}

}
