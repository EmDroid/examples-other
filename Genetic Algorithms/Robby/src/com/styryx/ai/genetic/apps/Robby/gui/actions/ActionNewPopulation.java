package com.styryx.ai.genetic.apps.Robby.gui.actions;

import java.awt.event.ActionEvent;
import java.util.Locale;

import com.styryx.ai.genetic.apps.Robby.gui.RobbyPanel;

public class ActionNewPopulation extends RobbyAction {
	
	public ActionNewPopulation(RobbyPanel main, Locale locale) {
		super(main, locale, RobbyPanel.getMessages(locale).getString("NewPopulation", "New population"),
			RobbyPanel.getMessages(locale).getString("CreateNewPopulation", "Create new population"));
	}
	
	public void actionPerformed(ActionEvent evt) {
		ActionEvolve m_actionEvolve = m_main.getActionEvolve();
		m_actionEvolve.cancel();
		m_actionEvolve.setEnabled(false);
		setEnabled(false);
		try {
			// Try to create the new world.
			m_main.createNewWorld(m_locale);
			m_main.getEvolutionLog().setText("");
			m_main.setDefaultButton(m_main.getEvolveButton());
		} catch (Exception e) {
			RobbyPanel.logException(e,
					RobbyPanel.getMessages(m_locale).getString("CannotCreateNewPopulation", "Cannot create new population"),
					m_locale);
		} finally {
			setEnabled(true);
			m_actionEvolve.setEnabled(true);
		}
	}
	
}
