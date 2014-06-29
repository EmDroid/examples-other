package com.styryx.ai.genetic.apps.Robby.gui.actions;

import java.awt.event.ActionEvent;
import java.util.Locale;
import java.util.Random;

import com.styryx.ai.genetic.apps.Robby.gui.RobbyPanel;
import com.styryx.ai.genetic.apps.Robby.worker.Playground;

public class ActionManual extends RobbyAction /* implements OptimalWorker.ResultListener */ {
	private static Random randomizer = new Random();
	public ActionManual(RobbyPanel main, Locale locale) {
		super(main, locale, RobbyPanel.getMessages(locale).getString("Refresh"),
				RobbyPanel.getMessages(locale).getString("RefreshManual", "Refresh the selected manual strategy"));
	}
	public void actionPerformed(ActionEvent evt) {
		// Start the optimum search procedure.
//		m_actionEvolve.setEnabled(false);
		setEnabled(false);
		try {
			int strategyId = m_main.getManualStrategyId();
//			JOptionPane.showMessageDialog(null, "Strategy id: " + strategyId);
			Playground.Strategy strategy = Playground.Strategy.getOptimal(strategyId, m_locale);
			double score = Playground.processStrategy(null, randomizer, strategy,
					m_main.getPlaygroundSize(), m_main.getSessions(), m_main.getActionsPerSession(), m_locale);
			finalizeGui();
			processResult(score, strategy.getString());
//			OptimalWorker worker = new OptimalWorker(this, PLAYGROUND_SIZE,
//					(Integer)m_sessions.getValue(), (Integer)m_actions.getValue());
//			worker.execute();
		} catch (Exception e) {
			finalizeGui();
			RobbyPanel.logException(e,
					RobbyPanel.getMessages(m_locale).getString("CannotComputeManual", "Cannot compute manual strategy"),
					m_locale);
		}
	}
	public void processResult(double score, String chain) {
		m_main.setBestStrategy(score, chain);
		m_main.setDefaultButton(m_main.getCreateButton());
	}
	public void finalizeGui() {
		setEnabled(true);
//		m_actionEvolve.setEnabled(true);
	}
}
