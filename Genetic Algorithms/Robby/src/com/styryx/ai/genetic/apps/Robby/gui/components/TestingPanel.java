package com.styryx.ai.genetic.apps.Robby.gui.components;

import javax.swing.*;
import javax.swing.GroupLayout.*;

import com.styryx.ai.genetic.apps.Robby.gui.RobbyPanel;
import com.styryx.util.ResourceBundle;

public class TestingPanel extends JPanel {
	
	public TestingPanel(RobbyPanel main, ResourceBundle messages) {
		super();
		setBorder(BorderFactory.createTitledBorder(
				messages.getString("Testing") + ":"));
		
		// The playground panel.
		PlaygroundPanel playground = main.getPlaygroundPanel();
		
		// The playground buttons.
		JButton create = main.getCreateButton();
		JButton start = main.getStartButton();
		JButton stop = main.getStopButton();
		
		// The tested strategy chain.
		JTextField testChain = main.getTestChain();
		JLabel testChainLabel = new JLabel(
				messages.getString("TestedChain", "Tested chain") + ":");
		testChainLabel.setToolTipText(testChain.getToolTipText());
		
		// The step counter.
		JTextField testStep = main.getTestStep();
		JLabel testStepLabel = new JLabel(
				messages.getString("Step") + ":");
		testStepLabel.setToolTipText(testStep.getToolTipText());
		
		// The test action.
		JTextField testAction = main.getTestAction();
		JLabel testActionLabel = new JLabel(
				messages.getString("Action") + ":");
		testActionLabel.setToolTipText(testAction.getToolTipText());
		
		// The test score.
		JTextField testScore = main.getTestScore();
		JLabel testScoreLabel = new JLabel(
				messages.getString("Score") + ":");
		testScoreLabel.setToolTipText(testScore.getToolTipText());
		
		// The testing panel layout.
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(playground)
			.addGroup(layout.createSequentialGroup()
				.addComponent(create, 0, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE)
				.addComponent(start, 0, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE)
				.addComponent(stop, 0, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE)
			)
			.addGroup(layout.createSequentialGroup()
				.addComponent(testChainLabel)
				.addComponent(testChain)
			)
			.addGroup(layout.createSequentialGroup()
				.addComponent(testStepLabel)
				.addComponent(testStep,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(layout.createSequentialGroup()
				.addComponent(testActionLabel)
				.addComponent(testAction,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(layout.createSequentialGroup()
				.addComponent(testScoreLabel)
				.addComponent(testScore,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
			)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(playground)
			.addGroup(layout.createParallelGroup()
				.addComponent(create)
				.addComponent(start)
				.addComponent(stop)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(testChainLabel)
				.addComponent(testChain)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(testStepLabel)
				.addComponent(testStep)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(testActionLabel)
				.addComponent(testAction)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(testScoreLabel)
				.addComponent(testScore)
			)
		);
		layout.linkSize(SwingConstants.HORIZONTAL, testStepLabel, testActionLabel, testScoreLabel);
	}

}
