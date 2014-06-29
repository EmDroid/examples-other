package com.styryx.gui;

import java.awt.BorderLayout;
import java.awt.event.*;

import javax.swing.*;

import com.styryx.util.ResourceBundle;


public abstract class SwingApplicationLaunchable extends SwingApplication implements ActionListener {
	
	private AbstractButton m_launcher;
	
	protected void createMainPanel(JRootPane rootPane) {
		// Add the launcher button.
		m_launcher = createLauncher();
		m_launcher.addActionListener(this);
		add(m_launcher, BorderLayout.CENTER);
	}
	
	protected AbstractButton createLauncher() {
		return new JButton(ResourceBundle.getFrameworkBundle(getLocale()).getString("Launch"));
	}

	protected void onInit() throws Exception {
		if (null != getParameter("autostart")) {
			// Launch automatically.
			launch();
		}
	}

	public void actionPerformed(ActionEvent evt) {
		// Allowed to create more windows.
		try {
			launch();
		} catch (Throwable e) {
			handleException(e);
		}
	}

	private void launch() throws Exception {
		// Create and launch the main window.
		initializeMainWindow(getLocale(), this);
	}

}
