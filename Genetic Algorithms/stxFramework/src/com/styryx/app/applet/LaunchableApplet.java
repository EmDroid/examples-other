package com.styryx.app.applet;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.Locale;

import javax.swing.*;

import com.styryx.gui.MainWindowHolder;

public abstract class LaunchableApplet extends Applet implements ActionListener, MainWindowHolder {
	
	private AbstractButton m_launcher;
	
	public class PanelInitializer extends Applet.PanelInitializer {
		
		final private ActionListener m_listener;
		
		public PanelInitializer(ActionListener listener) {
			m_listener = listener;
		}

		protected void createMainPanel(JRootPane rootPane, Locale locale) {
			// Add the launcher button.
			m_launcher = createLauncher();
			m_launcher.addActionListener(m_listener);
			add(m_launcher, BorderLayout.CENTER);
		}
		
	}
	
	protected void onInit() throws Exception {
		if (null != this.getParameter("autostart")) {
			// Launch automatically.
			launch();
		}
	}

	private void launch() throws Exception {
		// Create and launch the main window.
		WindowInitializer.create(this, this).initialize(getLocale());
	}

	public void actionPerformed(ActionEvent evt) {
		// Allowed to create more windows.
		try {
			launch();
		} catch (Throwable e) {
			handleException(e);
		}
	}

	public PanelInitializer createPanelInitializer() {
		return new PanelInitializer(this);
	}
	
	protected AbstractButton createLauncher() {
		return new JButton("Launch");
	}

}
