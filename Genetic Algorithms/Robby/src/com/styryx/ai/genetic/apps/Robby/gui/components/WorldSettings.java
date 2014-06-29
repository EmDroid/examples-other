package com.styryx.ai.genetic.apps.Robby.gui.components;

import javax.swing.*;
import javax.swing.GroupLayout.*;

import com.styryx.ai.genetic.apps.Robby.gui.RobbyPanel;
import com.styryx.util.ResourceBundle;

public class WorldSettings extends JPanel {
	
	public WorldSettings(RobbyPanel main, ResourceBundle messages) {
		super();
		setBorder(BorderFactory.createTitledBorder(
				messages.getString("World") + ":"));
		
		// The population size.
		JSpinner population = main.getPopulationComponent();
		JLabel populationLabel = new JLabel(
				messages.getString("Population") + ":");
		populationLabel.setToolTipText(population.getToolTipText());
		
		// The number of sessions.
		JSpinner sessions = main.getSessionsComponent();
		JLabel sessionsLabel = new JLabel(
				messages.getString("Sessions") + ":");
		sessionsLabel.setToolTipText(sessions.getToolTipText());
		
		// The number of actions per session.
		JSpinner actions = main.getActionsPerSessionComponent();
		JLabel actionsLabel = new JLabel(
				messages.getString("SessionActions", "Actions per session") + ":");
		actionsLabel.setToolTipText(actions.getToolTipText());
		
		// The "New world" button.
		JButton create = main.getCreateWorldButton();
		
		// The world settings layout.
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createSequentialGroup()
				.addComponent(populationLabel)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
					GroupLayout.DEFAULT_SIZE,
					Integer.MAX_VALUE)
				.addComponent(population,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(layout.createSequentialGroup()
				.addComponent(sessionsLabel)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
					GroupLayout.DEFAULT_SIZE,
					Integer.MAX_VALUE)
				.addComponent(sessions,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(layout.createSequentialGroup()
				.addComponent(actionsLabel)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
					GroupLayout.DEFAULT_SIZE,
					Integer.MAX_VALUE)
				.addComponent(actions,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(create)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(populationLabel)
				.addComponent(population,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(sessionsLabel)
				.addComponent(sessions,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(actionsLabel)
				.addComponent(actions,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
			)
			.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
					GroupLayout.DEFAULT_SIZE,
					Integer.MAX_VALUE)
			.addComponent(create)
		);
	}
	
}
