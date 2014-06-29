package com.styryx.ai.genetic.apps.Robby.gui.components;

import javax.swing.*;
import javax.swing.GroupLayout.*;

import com.styryx.ai.genetic.apps.Robby.gui.RobbyPanel;
import com.styryx.util.ResourceBundle;

public class EvolutionSettings extends JPanel {
	
	public EvolutionSettings(RobbyPanel main, ResourceBundle messages) {
		super();
		setBorder(BorderFactory.createTitledBorder(
				messages.getString("Evolution") + ":"));
		
		// The generations count.
		JSpinner generations = main.getGenerationsComponent();
		JLabel generationsLabel = new JLabel(
				messages.getString("Generations") + ":");
		generationsLabel.setToolTipText(generations.getToolTipText());
		
		// The mutation probability.
		JSpinner mutations = main.getMutationsComponent();
		JLabel mutationsLabel = new JLabel(
				messages.getString("Mutations", "Mutation probability") + ":");
		mutationsLabel.setToolTipText(mutations.getToolTipText());
		
		// The selection type.
		JComboBox selection = main.getSelectionComponent();
		JLabel selectionLabel = new JLabel(
				messages.getString("SelectionAlgorithm", "Selection algorithm") + ":");
		selectionLabel.setToolTipText(selection.getToolTipText());
		
		// The crossover checkbox.
		JCheckBox crossover = main.getCrossoverComponent();
		
		// The threads count.
		JSpinner threads = main.getThreadsComponent();
		JLabel threadsLabel = new JLabel(
				messages.getString("NumThreads", "Threads count") + ":");
		threadsLabel.setToolTipText(threads.getToolTipText());
		
		// The "Evolve" button.
		JButton evolve = main.getEvolveButton();
		
		// The evolution progress.
		JProgressBar progress = main.getProgressComponent();
		
		// The evolution settings layout.
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createSequentialGroup()
				.addComponent(generationsLabel)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
					GroupLayout.DEFAULT_SIZE,
					Integer.MAX_VALUE)
				.addComponent(generations,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(layout.createSequentialGroup()
				.addComponent(mutationsLabel)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
					GroupLayout.DEFAULT_SIZE,
					Integer.MAX_VALUE)
				.addComponent(mutations,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(layout.createSequentialGroup()
				.addComponent(selectionLabel)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
					GroupLayout.DEFAULT_SIZE,
					Integer.MAX_VALUE)
				.addComponent(selection,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(crossover)
			.addGroup(layout.createSequentialGroup()
				.addComponent(threadsLabel)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
					GroupLayout.DEFAULT_SIZE,
					Integer.MAX_VALUE)
				.addComponent(threads,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(layout.createSequentialGroup()
				.addComponent(evolve /*, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE */)
				.addComponent(progress)
			)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(generationsLabel)
				.addComponent(generations,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(mutationsLabel)
				.addComponent(mutations,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(selectionLabel)
				.addComponent(selection,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(crossover)
			.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
					GroupLayout.DEFAULT_SIZE,
					Integer.MAX_VALUE)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(threadsLabel)
				.addComponent(threads,
					GroupLayout.PREFERRED_SIZE,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(layout.createParallelGroup()
				.addComponent(evolve)
				.addComponent(progress /*,
					GroupLayout.DEFAULT_SIZE,
					GroupLayout.DEFAULT_SIZE,
					Integer.MAX_VALUE*/)
			)
		);
		layout.linkSize(SwingConstants.VERTICAL, evolve, progress);
	}
	
}
