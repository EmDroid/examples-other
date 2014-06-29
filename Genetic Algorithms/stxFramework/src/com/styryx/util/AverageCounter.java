/* $Id: AverageCounter.java 436 2010-07-27 20:37:47Z styryx $ */

package com.styryx.util;

public class AverageCounter {
	
	private double m_total = 0;
	private long m_count = 0;

	public void add(double value) {
		m_total += value;
		++m_count;
	}

	public double getAverage() {
		return m_total / m_count;
	}

}
