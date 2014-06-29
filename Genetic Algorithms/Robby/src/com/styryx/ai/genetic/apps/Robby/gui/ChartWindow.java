package com.styryx.ai.genetic.apps.Robby.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.Locale;

import javax.swing.*;

import com.styryx.ai.genetic.apps.Robby.gui.components.ChartPanel;
import com.styryx.gui.components.Chart;
import com.styryx.util.ResourceBundle;

public class ChartWindow extends JFrame {
	
	public ChartWindow(RobbyPanel main, Locale locale, JComboBox chartRange, Chart chart, ResourceBundle messages) {
		super(messages.getString("RobbyTitle", "Robby - The soda can collector") + ": "
				+ messages.getString("EvolutionChart", "Evolution chart"));
		setIconImages(main.getRootWindow().getIconImages());
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		chart.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {
				if (isVisible()) setVisible(false);
			}
		});
		ChartPanel.createPanel(main, locale, (JPanel) this.getContentPane(), chartRange, chart, messages);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension quarterSize = new Dimension(screenSize.width >> 2, screenSize.height >> 2);
		screenSize.width -= quarterSize.width;
		screenSize.height -= quarterSize.height;
		quarterSize.width >>= 1;
		quarterSize.height >>= 1;
		setBounds(quarterSize.width, quarterSize.height, screenSize.width, screenSize.height);
	}

}
