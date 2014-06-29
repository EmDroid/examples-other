package com.styryx.ai.genetic.apps.Robby.gui.actions;

import java.awt.event.ActionEvent;
import java.util.Locale;

import com.styryx.ai.genetic.apps.Robby.gui.RobbyPanel;

public class ActionCreate extends RobbyAction {
	
	public ActionCreate(RobbyPanel main, Locale locale) {
		super(main, locale, RobbyPanel.getMessages(locale).getString("Create"),
				RobbyPanel.getMessages(locale).getString("CreateNewPlayground", "Create new playground"));
	}
	
	public void actionPerformed(ActionEvent evt) {
		setEnabled(false);
		try {
			// Stop any current testing.
			m_main.getActionStart().cancel();
			// Try to create the playground.
			m_main.createNewPlayground(m_locale);
			m_main.getActionStart().setEnabled(true);
			m_main.setDefaultButton(m_main.getStartButton());
		} catch (Exception e) {
			RobbyPanel.logException(e,
					RobbyPanel.getMessages(m_locale).getString("CreateFailed", "Playground creation failed"),
					m_locale);
		}
		setEnabled(true);
	}
}
