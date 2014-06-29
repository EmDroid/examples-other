package com.styryx.ai.genetic.apps.Robby;

import java.util.Locale;

import javax.swing.UIManager;

import com.styryx.ai.genetic.apps.Robby.gui.RobbyWindow;
import com.styryx.gui.SwingApplicationLaunchable;

public class Robby extends SwingApplicationLaunchable {
	
	public static void main(String[] args) {
		startup(new Initializer() {
			public Robby createApplication() {
				return new Robby();
			}
		}, args);
	}

	protected void setupLookAndFeel() throws Exception {
		// Set cross-platform look nad feel by default.
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	}

	protected RobbyWindow createMainWindow(Locale locale) throws Exception {
		return new RobbyWindow(getLocale());
	}
	
}
