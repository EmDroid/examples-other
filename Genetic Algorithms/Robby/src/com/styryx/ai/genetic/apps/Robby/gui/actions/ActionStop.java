package com.styryx.ai.genetic.apps.Robby.gui.actions;

import java.awt.event.*;
import java.util.Locale;

import com.styryx.ai.genetic.apps.Robby.gui.RobbyPanel;

public class ActionStop extends RobbyAction {
	
	public ActionStop(RobbyPanel main, Locale locale) {
		super(main, locale, RobbyPanel.getMessages(locale).getString("Stop"),
				RobbyPanel.getMessages(locale).getString("StopTestingProcedure", "Stop the testing procedure"));
		setEnabled(false);
	}
	
	public void actionPerformed(ActionEvent e) {
		// Disable itself.
		setEnabled(false);
		// Try to stop the processing.
		m_main.getActionStart().cancel();
	}
}
