package com.styryx.gui.components;

import java.awt.*;
import java.text.NumberFormat;
import java.util.*;

import javax.swing.*;

import com.styryx.gui.components.Chart.ValueLine.Value;

public class Chart extends JPanel {
	
	private static final Color BACKGROUND = Color.WHITE;
	private static final Color CHART_BACKGROUND = new Color(0xBF, 0xBF, 0xBF);
	private static final Color CHART_HINTS = Color.WHITE;
	private static final Color CHART_LINES = Color.BLACK;
	private static final Color CHART_TEXT = Color.BLACK;
	
	private static final Color CHART_DATA = Color.RED;
	
	private static final int MARGIN = 5;
	private static final int MARGIN_DBL = MARGIN << 1;
	
	private static final int AXIS_SPACE = 4;
	private static final int AXIS_SIZE = 2;
	private static final int AXIS_SHIFT = AXIS_SPACE;
	private static final int AXIS_FULL_SHIFT = (AXIS_SPACE << 1) + AXIS_SIZE + AXIS_SHIFT;
	
	private final static int ID_X = 0;
	private final static int ID_Y = 1;
//	private final static int ID_MIN = 0;
//	private final static int ID_MAX = 1;
	
	private static final Stroke sm_hintStroke = new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] {1, 3}, 0);
	
	private double m_min[];
	private double m_max[];
	private boolean[] m_rangeInitialized;
	private boolean[] m_maintainZero;
	
	private Color m_colorBackground = CHART_BACKGROUND;
	private Color m_colorHints = CHART_HINTS;
	private Color m_colorLines = CHART_LINES;
	private Color m_colorText = CHART_TEXT;
	private Color m_colorData = CHART_DATA;
	
	public static class ValueLine {
		public static class Value {
			private final Number x;
			private final Number y;
			public Value(Number x, Number y) {
				this.x = x;
				this.y = y;
			}
		}
		private final ArrayList<Value> m_points = new ArrayList<Value>();
		private void clear() {
			m_points.clear();
		}
		private void addPoint(Value value) {
			m_points.add(value);
		}
	}
	
	private Map<Integer, ValueLine> m_lines;
	
	public Chart() {
		super();
		init();
	}
	
    public Chart(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
		init();
    }
    
    public Chart(LayoutManager layout, boolean isDoubleBuffered) {
    	super(layout, isDoubleBuffered);
		init();
    }
    
    public Chart(LayoutManager layout) {
    	super(layout);
		init();
    }
    
    private void init() {
    	setBackground(BACKGROUND);
    	initInternal();
		m_lines = new HashMap<Integer, ValueLine>();
		 m_maintainZero = new boolean[]{
				// X-axis starts with first value.
				false,
				// Y-axis starts with 0.
				true};
    }
    
	private void initInternal() {
		m_min = new double[]{0.0, 0.0};
		m_max = new double[]{1.0, 1.0};
		m_rangeInitialized = new boolean[]{false, false};
	}
	
	public void clear() {
    	initInternal();
		for (ValueLine line : m_lines.values()) {
			line.clear();
		}
		repaint();
	}
	
	/*
Graphics2D g = ...;
Point2D loc = ...;
Font font = Font.getFont("Helvetica-bold-italic");
FontRenderContext frc = g.getFontRenderContext();
TextLayout layout = new TextLayout("This is a string", font, frc);
layout.draw(g, (float)loc.getX(), (float)loc.getY());

Rectangle2D bounds = layout.getBounds();
bounds.setRect(bounds.getX()+loc.getX(),
              bounds.getY()+loc.getY(),
              bounds.getWidth(),
              bounds.getHeight());
g.draw(bounds);
	 */
	
	private class AxisSetup {

		public int digits;
		public double minValue;
		public double maxValue;
		public int steps;
		public int stepSize;
		public double rangeStep;
		
		private boolean rangeFits(double rangeMin, double rangeMax, int maxSteps) {
			if ((rangeMax - rangeMin) <= maxSteps) {
				return true;
			}
			return false;
		}
		
		public AxisSetup() {}
		
		public AxisSetup(int id, int axisSize, int descrSize, int lastSize) {
			estimate(id, axisSize, descrSize, lastSize);
		}
		
		public void estimate(int id, int axisSize, int descrSize, int lastSize) {
			// Maximum steps count.
			int maxSteps = (axisSize - lastSize) / descrSize;
			double range = m_max[id] - m_min[id];
			// Estimate the range according maximum steps.
			double multiplier = 1;
			while ((range > 0) && (range < maxSteps)) {
				range *= 10;
				multiplier /= 10;
			}
			double rangeMax = Math.ceil(m_max[id] / multiplier);
			double rangeMin = Math.floor(m_min[id] / multiplier);
			while (!rangeFits(rangeMin, rangeMax, maxSteps)) {
				rangeMax = Math.ceil(rangeMax / 10);
				rangeMin = Math.floor(rangeMin / 10);
				multiplier *= 10;
			}
			int steps = (int) (rangeMax - rangeMin);
			if (0 == steps) {
				++steps;
				--rangeMin;
			}
			double minValue = rangeMin * multiplier;
			double maxValue = rangeMax * multiplier;
			// Try to re-estimate the steps according to maxSteps.
			int stepsMul = maxSteps / steps;
			boolean enableReestimate = false;
			if ((stepsMul >= 5) && (((maxValue - minValue) / steps) > 1.0)) {
				steps *= 5;
			} else if (stepsMul >= 4) {
				steps *= 4;
			} else if (stepsMul >= 2) {
				steps *= 2;
				enableReestimate = true;
			}
			double rangeStep = (maxValue - minValue) / steps;
			boolean maxRanged;
			while ((steps > 1) && ((maxRanged = ((maxValue - rangeStep) >= m_max[id]))
					|| ((minValue + rangeStep) <= m_min[id]))) {
				if (maxRanged) {
					maxValue -= rangeStep;
				} else {
					minValue += rangeStep;
				}
				--steps;
				if (enableReestimate && ((steps * 2) < maxSteps)) {
					steps *= 2;
					rangeStep /= 2;
					enableReestimate = false;
				}
			}
//			while ((steps > 1) && ((minValue + rangeStep) <= m_min[id])) {
//				minValue += rangeStep;
//				--steps;
//				if (enableReestimate && ((steps * 2) < maxSteps)) {
//					steps *= 2;
//					rangeStep /= 2;
//				}
//			}
			// Estimate the digits count.
			int digits = 0;
			double digitRangeStep = rangeStep;
			while (digitRangeStep > Math.floor(digitRangeStep)) {
				digitRangeStep *= 10;
				++digits;
			}
			this.steps = realign(axisSize, lastSize, steps);
			this.rangeStep = rangeStep;
			this.digits = digits;
			this.minValue = minValue;
			this.maxValue = maxValue;
		}
		
		public void realign(int axisSize, int lastSize) {
			this.steps = realign(axisSize, lastSize, this.steps);
		}
		
		private int realign(int axisSize, int lastSize, int steps) {
			int availableSize = axisSize - lastSize;
			while (((this.stepSize = availableSize / steps) * (steps + 1)) <= availableSize) {
				++steps;
			}
			return steps;
		}
		
		public int estimateValue(double value) {
			int result = (int) Math.round(((value - minValue) / rangeStep) * stepSize);
			return result;
		}
		
	}
	
	private double maxCaption(double min, double max) {
		double maxValue = Math.max(Math.abs(min), Math.abs(max));
		if (min < 0) {
			maxValue *= -1;
		}
		return maxValue;
	}
	
//	private double minCaption(double min, double max) {
//		if ((min <= 0) && (max >= 0)) {
//			return 0;
//		} else {
//			return Math.min(Math.abs(min), Math.abs(max));
//		}
//	}
//	
    protected void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	if (null != g) {
    		Graphics2D g2d = (Graphics2D)g.create();
    		try {
    			Dimension size = getSize();
    			Rectangle bounds = new Rectangle(
    					MARGIN, MARGIN,
    					size.width - MARGIN_DBL,
    					size.height - MARGIN_DBL);
    			NumberFormat numberFormatter = NumberFormat.getInstance();
    			FontMetrics metrics = g2d.getFontMetrics();
    			int fontLineSpacing = metrics.getHeight();
    			int fontHeight = metrics.getAscent();
    			int fontHeightHalf = fontHeight >> 1;
    			int fontWidth = metrics.stringWidth(numberFormatter.format(
    					maxCaption(m_min[ID_X], m_max[ID_X])));
    			int fontWidthHalf = metrics.stringWidth(numberFormatter.format(m_max[ID_X])) >> 1;
				int fontWidthSpacing = (fontLineSpacing - fontHeight) << 1;
    			int[] descriptSize = new int[]{
    					// X description height.
    					fontHeight,
    					// Y description width.
    					50,
    			};
//    			Rectangle[] descriptArea = new Rectangle[]{
//    					// X-axis description.
//    					new Rectangle(
//		    					bounds.x,
//		    					bounds.height - descriptSize[ID_X],
//		    					bounds.width - descriptSize[ID_Y],
//		    					descriptSize[ID_X]),
//		    			// Y-axis description.
//    					new Rectangle(
//		    					bounds.x,
//		    					bounds.y,
//		    					descriptSize[ID_Y],
//		    					bounds.height - descriptSize[ID_X]),
//		    			};
    			Rectangle chartArea = new Rectangle(
    					descriptSize[ID_Y] + AXIS_FULL_SHIFT,
        				bounds.y,
        				bounds.width - descriptSize[ID_Y]  - AXIS_FULL_SHIFT,
        				bounds.height - descriptSize[ID_X] - AXIS_FULL_SHIFT);
    			// Draw the graph background.
    			g2d.setColor(m_colorBackground);
    			g2d.fillRect(chartArea.x, chartArea.y, chartArea.width, chartArea.height);
    			g2d.setColor(m_colorLines);
    			// The X axis.
    			int pos;
    			double value;
    			AxisSetup axisSetupX = new AxisSetup();
    			int lastFontWidth;
//				System.out.println("=================");
				int digits;
    			do {
        			lastFontWidth = fontWidth;
    				axisSetupX.estimate(ID_X, chartArea.width, fontWidth + fontWidthSpacing, fontWidthHalf);
    				numberFormatter.setMinimumFractionDigits(digits = axisSetupX.digits);
        			fontWidth = metrics.stringWidth(numberFormatter.format(
        					maxCaption(axisSetupX.minValue, axisSetupX.maxValue)));
    				fontWidthHalf = metrics.stringWidth(numberFormatter.format(axisSetupX.maxValue)) >> 1;
//					System.out.println("Digits: " + axisSetupX.digits
//							+ "\nLast width: " + lastFontWidth
//							+ "\nNew width: " + fontWidth);
    			} while (fontWidth > lastFontWidth);
//				axisSetupX =  new AxisSetup(ID_X, chartArea.width, fontWidth + fontWidthSpacing, fontWidthHalf);
    			axisSetupX.realign(chartArea.width, fontWidthHalf);
    			g2d.setColor(m_colorLines);
    			g2d.drawLine(chartArea.x, chartArea.y + chartArea.height + AXIS_SPACE,
    					chartArea.x + chartArea.width, chartArea.y + chartArea.height + AXIS_SPACE);
    			pos = chartArea.x;
    			numberFormatter.setMinimumFractionDigits(digits);
    			value = axisSetupX.minValue;
    			for (int i = 0; i <= axisSetupX.steps; ++i) {
	    			if (i > 0) {
	    				// Draw the hint line.
		    			g2d.setColor(m_colorHints);
		    			Stroke orig = g2d.getStroke();
		    			g2d.setStroke(sm_hintStroke);
		    			g2d.drawLine(pos, chartArea.y, pos, chartArea.y + chartArea.height);
		    			g2d.setStroke(orig);
	    			}
	    			// Draw the small value line.
	    			g2d.setColor(m_colorLines);
	    			g2d.drawLine(pos, chartArea.y + chartArea.height + AXIS_SPACE,
	    					pos, chartArea.y + chartArea.height + AXIS_SPACE + AXIS_SIZE);
	    			// Draw the value.
	    			String str = numberFormatter.format(value);
	    			int strWidth = metrics.stringWidth(str) >> 1;
	    			g2d.setColor(m_colorText);
	    			g2d.drawString(str,
	    					pos - strWidth,
	    					bounds.y + bounds.height);
	    			// Advance the position.
	    			pos += axisSetupX.stepSize;
	    			value += axisSetupX.rangeStep;
    			}
    			// The Y axis.
    			AxisSetup axisSetupY = new AxisSetup(ID_Y, chartArea.height, fontLineSpacing, fontHeightHalf);
    			g2d.setColor(m_colorLines);
    			g2d.drawLine(chartArea.x - AXIS_SPACE, chartArea.y,
    					chartArea.x - AXIS_SPACE, chartArea.y + chartArea.height);
    			pos = chartArea.y + chartArea.height;
    			numberFormatter.setMinimumFractionDigits(axisSetupY.digits);
    			value = axisSetupY.minValue;
    			for (int i = 0; i <= axisSetupY.steps; ++i) {
	    			if (i > 0) {
	    				// Draw the hint line.
		    			g2d.setColor(m_colorHints);
		    			Stroke orig = g2d.getStroke();
		    			g2d.setStroke(sm_hintStroke);
		    			g2d.drawLine(chartArea.x, pos, chartArea.x + chartArea.width, pos);
		    			g2d.setStroke(orig);
	    			}
	    			// Draw the small value line.
	    			g2d.setColor(m_colorLines);
	    			g2d.drawLine(chartArea.x - AXIS_SPACE, pos,
	    					chartArea.x - AXIS_SPACE - AXIS_SIZE, pos);
	    			// Draw the value.
	    			String str = numberFormatter.format(value);
	    			int strWidth = metrics.stringWidth(str);
	    			g2d.setColor(m_colorText);
	    			g2d.drawString(str, bounds.x + descriptSize[ID_Y] - strWidth, pos + fontHeightHalf);
	    			// Advance the position.
	    			pos -= axisSetupY.stepSize;
	    			value += axisSetupY.rangeStep;
    			}
    			// Draw the data.
    			Stroke orig = g2d.getStroke();
    			g2d.setStroke(new BasicStroke(2));
    			for (ValueLine line : m_lines.values()) {
        			g2d.setColor(m_colorData);
        			ValueLine.Value entryPrev = null;
        			for (ValueLine.Value entry : line.m_points) {
        				int x1, y1;
        				int x2 = axisSetupX.estimateValue(entry.x.doubleValue());
        				int y2 = axisSetupY.estimateValue(entry.y.doubleValue());
        				if (null == entryPrev) {
        					// Draw the point.
        					x1 = x2;
        					y1 = y2;
        				} else {
        					// Draw the line.
        					x1 = axisSetupX.estimateValue(entryPrev.x.doubleValue());
        					y1 = axisSetupY.estimateValue(entryPrev.y.doubleValue());
        				}
        				g2d.drawLine(chartArea.x + x1, chartArea.y + chartArea.height - y1,
        						chartArea.x + x2, chartArea.y + chartArea.height - y2);
        				entryPrev = entry;
        			}
    			}
    			g2d.setStroke(orig);
    			// Draw the chart frame.
    			g2d.setColor(m_colorLines);
    			g2d.drawRect(chartArea.x, chartArea.y, chartArea.width, chartArea.height);
    		} finally {
    			g2d.dispose();
    		}
    	}
    }
    
    public void addPoint(double x, double y) {
    	addPoint(new Value(x, y));
    }
	
    public void addPoint(int x, int y) {
    	addPoint(new Value(x, y));
    }
	
	public void addPoint(Value value) {
    	addPoint(1, value);
	}

	public void addPoints(Collection<Value> values) {
		addPoints(1, values);
	}

    public void addPoint(int setId, double x, double y) {
    	addPoint(setId, new Value(x, y));
    }
	
    public void addPoint(int setId, int x, int y) {
    	addPoint(setId, new Value(x, y));
    }
	
	public void addPoint(int setId, Value value) {
    	ValueLine line = m_lines.get(setId);
    	if (null == line) {
    		// Create new point line.
    		m_lines.put(setId, line = new ValueLine());
    	}
    	line.addPoint(value);
    	adjustRange(ID_X, value.x.doubleValue());
    	adjustRange(ID_Y, value.y.doubleValue());
    	repaint();
    }

	public void addPoints(int setId, Collection<Value> values) {
		Iterator<Value> iter = values.iterator();
		while (iter.hasNext()) {
			addPoint(setId, iter.next());
		}
	}

	private void adjustRange(int id, double value) {
		boolean adjusted = false;
    	if (!m_rangeInitialized[id] || (m_min[id] > value)) {
    		m_min[id] = (m_maintainZero[id]) ? Math.min(0, value) : value;
    		adjusted = true;
    	}
    	if (!m_rangeInitialized[id] || (m_max[id] < value)) {
    		m_max[id] = (m_maintainZero[id]) ? Math.max(0, value) : value;
    		adjusted = true;
    	}
    	m_rangeInitialized[id] = true;
    	if (adjusted) {
    		// Re-adjust the ranges.
    	}
	}

}
