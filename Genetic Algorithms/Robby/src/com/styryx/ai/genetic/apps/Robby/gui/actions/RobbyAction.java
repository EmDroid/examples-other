package com.styryx.ai.genetic.apps.Robby.gui.actions;

import java.util.Locale;

import javax.swing.*;

import com.styryx.ai.genetic.apps.Robby.gui.RobbyPanel;

abstract public class RobbyAction extends AbstractAction {
	
	protected final RobbyPanel m_main;
	protected final Locale m_locale;
	
	public RobbyAction(RobbyPanel main, Locale locale, String title, String tooltip) {
		super(title);
		m_main = main;
		m_locale = locale;
		putValue(Action.SHORT_DESCRIPTION, tooltip);
	}
	
	
	
}
