package com.styryx.ai.genetic.apps.Robby.gui.actions;

import java.awt.event.*;
import java.util.Locale;

import javax.swing.*;

import com.styryx.ai.genetic.apps.Robby.exception.RobbyException;
import com.styryx.ai.genetic.apps.Robby.gui.RobbyPanel;

public class ActionStart extends RobbyAction /* implements TestWorker.ActionListener */ {
	
//	private TestWorker m_tester;
	
	private class TestTimer extends Timer {
		
		public TestTimer() {
			super(500, new ActionListener() {
				final int max = m_main.getActionsPerSession();
				int step = 0;
				public void actionPerformed(ActionEvent evt) {
					try {
						if (++step == max) {
							// Done.
							m_tester.stop();
						}
						m_main.playgroundStep(step, m_locale);
					} catch (Exception e) {
						m_tester.stop();
						RobbyPanel.logException(e,
								RobbyPanel.getMessages(m_locale).getString("Error"),
								m_locale);
					}
				}
			});
			this.start();
		}
		
		public void stop() {
			super.stop();
			// Disable the stop action.
			m_main.getActionStop().setEnabled(false);
			// Enable the create action.
//			m_main.getActionCreate().setEnabled(true);
			// Do not re-enable the start action, because we have to create new playground.
			// Set the create button as default.
			m_main.setDefaultButton(m_main.getCreateButton());
		}
		
	}
	
	private Timer m_tester;
	
	public ActionStart(RobbyPanel main, Locale locale) {
		super(main, locale, RobbyPanel.getMessages(locale).getString("Start"),
				RobbyPanel.getMessages(locale).getString("TestEvolvedStrategy", "Test using the evolved strategy"));
		setEnabled(false);
	}
	
	public void actionPerformed(ActionEvent evt) {
		// Disable the new playground creation during testing.
//		m_main.getActionCreate().setEnabled(false);
		// Disable itself.
		setEnabled(false);
		// Enable the stop action.
		m_main.getActionStop().setEnabled(true);
		// Set the stop action as default.
		m_main.setDefaultButton(m_main.getStopButton());
		// Start the testing procedure.
		try {
			m_main.setPlaygroundStrategy();
//			m_tester = new TestWorker(this, m_playground, (Integer)m_actions.getValue());
//			m_tester.execute();
			m_tester = new TestTimer();
		} catch (RobbyException e) {
			// Disable the stop action.
			m_main.getActionStop().setEnabled(false);
			// Enable the create action.
			m_main.getActionCreate().setEnabled(true);
			// Enable itself.
			setEnabled(true);
			RobbyPanel.logException(e,
					RobbyPanel.getMessages(m_locale).getString("Error"),
					m_locale);
		}
	}
//	public void step(int step, Playground.Action action) {
//		m_testingPanel.repaintPlayground();
//		m_testStep.setText(" " + step);
//		m_testAction.setText(" " + action.toString());
//		m_testScore.setText(" " + m_playground.getScore());
//	}

	public void cancel() {
		if (null != m_tester) {
//			m_tester.cancel(false);
			m_tester.stop();
		}
	}
}
