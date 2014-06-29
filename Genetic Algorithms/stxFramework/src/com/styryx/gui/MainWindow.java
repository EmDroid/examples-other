package com.styryx.gui;

import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;
import java.util.Locale;

import javax.swing.*;

import com.styryx.app.ExceptionHandler;

public abstract class MainWindow extends JFrame implements RootPanelHolder, WindowListener, CloseHandler {
	
	private CloseHandler m_closeHandler = null;
	
	public MainWindow(String title, Locale locale) {
		this(title, null, locale);
	}
	
	public MainWindow(String title, String iconName, Locale locale) {
		super(title);
		setLocale(locale);
		if (null != iconName) {
			// Try to load the icon.
			try {
				URL resource = getClass().getClassLoader().getResource(iconName);
				if (null != resource) {
					setIconImage(new ImageIcon(resource).getImage());
				}
			} catch (Exception e) {
				ExceptionHandler.handleException(null, getLocale(), e);
				// Continue execution.
			}
		}
		addWindowListener(this);
	}
	
	public void setCloseHandler(CloseHandler closeHandler) {
		m_closeHandler = closeHandler;
	}
	
	public void windowClosed(WindowEvent e) {
		if (null != m_closeHandler) {
			m_closeHandler.close();
		}
	}
	
	public void windowClosing(WindowEvent evt) {
//		if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this,
//				"Do you really want to close this window?",
//				"Close confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)) {
			evt.getWindow().dispose();
//		}
	}
	
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	
	public void close() {
		WindowEvent event = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(event);
	}

}
