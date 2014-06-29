package com.styryx.ai.genetic.apps.Robby.gui.components;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.Locale;

import javax.swing.*;
import javax.swing.GroupLayout.*;

import com.styryx.ai.genetic.apps.Robby.gui.RobbyPanel;
import com.styryx.ai.genetic.apps.Robby.gui.actions.RobbyAction;
import com.styryx.gui.components.Chart;
import com.styryx.util.ResourceBundle;

public class ChartPanel extends JPanel {
	
	public ChartPanel(RobbyPanel main, Locale locale, JComboBox chartRange, Chart chart, ResourceBundle messages) {
		super();
		setBorder(BorderFactory.createTitledBorder(
				messages.getString("EvolutionChart", "Evolution chart") + ":"));
		createPanel(main, locale, this, chartRange, chart, messages);
	}
	
	private static class ChangeAction extends RobbyAction {

		private final Chart m_chart;

		public ChangeAction(RobbyPanel main, Locale locale, Chart chart) {
			super(main, locale, null, null);
			m_chart = chart;
		}

		public void actionPerformed(ActionEvent evt) {
			JComboBox rangeComponent = (JComboBox) evt.getSource();
			m_chart.clear();
			switch (rangeComponent.getSelectedIndex()) {
			case 0:
				// Last iteration.
				m_chart.addPoints(m_main.getChartIteration());
				break;
			case 1:
				// Full evolution.
				m_chart.addPoints(m_main.getChartFull());
				break;
			default:
				// Unknown selection.
			}
			m_chart.repaint();
		}
		
	}
	
	public static void createPanel(RobbyPanel main, Locale locale, JPanel panel, JComboBox chartRange,
			Chart chart, ResourceBundle messages) {
		// The evolution chart range.
		chartRange.addItem(messages.getString("LastIteration", "Last iteration"));
		chartRange.addItem(messages.getString("FullEvolution", "Full evolution"));
		chartRange.setSelectedIndex(1);
		chartRange.setToolTipText(messages.getString("SelectChartRange", "Select chart range"));
		chartRange.setAction(new ChangeAction(main, locale, chart));
		JLabel chartRangeLabel = new JLabel(messages.getString("ChartRange", "Chart range") + ":");
		chartRangeLabel.setToolTipText(chartRange.getToolTipText());
		
		chart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		// The evolution chart layout.
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createSequentialGroup()
				.addComponent(chartRangeLabel)
				.addComponent(chartRange,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(chart)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(chartRangeLabel)
				.addComponent(chartRange,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(chart)
		);
	}
	
}
