package com.styryx.ai.genetic.apps.Robby.gui.components;

import javax.swing.*;
import javax.swing.GroupLayout.*;

import com.styryx.ai.genetic.apps.Robby.gui.RobbyPanel;
import com.styryx.util.ResourceBundle;

public class EvolutionResults extends JPanel {
	
	public EvolutionResults(RobbyPanel main, ResourceBundle messages) {
		super();
		setBorder(BorderFactory.createTitledBorder(
				messages.getString("BestStrategy", "Best evolved strategy") + ":"));
		
		// The best score.
		JTextField score = main.getScoreComponent();
		JLabel scoreLabel = new JLabel(
				messages.getString("Score") + ":");
		scoreLabel.setToolTipText(score.getToolTipText());
		
		// The manual solution.
		JComboBox manual = main.getManualStrategyComponent();
		JLabel manualLabel = new JLabel(
				messages.getString("ManualSolution", "Manual solution") + ":");
		manualLabel.setToolTipText(manual.getToolTipText());
		
		// The manual refresh button.
		JButton manualRefresh = main.getManualRefreshButton();
		
		// The best strategy chain.
		JTextArea chain = main.getChainComponent();
		JLabel chainLabel = new JLabel(
				messages.getString("Chain") + ":");
		chainLabel.setToolTipText(chain.getToolTipText());
		
		// The best strategy panel layout.
		GroupLayout resultsLayout = new GroupLayout(this);
		setLayout(resultsLayout);
		resultsLayout.setAutoCreateGaps(true);
		resultsLayout.setAutoCreateContainerGaps(true);
		resultsLayout.setHorizontalGroup(resultsLayout.createParallelGroup()
			.addGroup(resultsLayout.createSequentialGroup()
				.addComponent(scoreLabel)
				.addComponent(score,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
				.addGap(10, 20, Integer.MAX_VALUE)
				.addComponent(manualLabel)
				.addComponent(manual,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
				.addComponent(manualRefresh)
			)
			.addComponent(chainLabel)
			.addComponent(chain,
				GroupLayout.PREFERRED_SIZE,
				GroupLayout.DEFAULT_SIZE,
				GroupLayout.PREFERRED_SIZE)
		);
		resultsLayout.setVerticalGroup(resultsLayout.createSequentialGroup()
			.addGroup(resultsLayout.createParallelGroup(Alignment.BASELINE)
				.addComponent(scoreLabel)
				.addComponent(score,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
				.addComponent(manualLabel)
				.addComponent(manual)
				.addComponent(manualRefresh)
			)
			.addComponent(chainLabel)
			.addComponent(chain,
				GroupLayout.PREFERRED_SIZE,
				GroupLayout.DEFAULT_SIZE,
				GroupLayout.PREFERRED_SIZE)
		);
	}
	
}
