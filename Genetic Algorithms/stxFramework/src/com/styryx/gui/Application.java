package com.styryx.gui;

import java.util.Locale;

import javax.swing.SwingUtilities;

import com.styryx.app.ConsoleApplication;
import com.styryx.app.ExceptionHandler;
import com.styryx.util.LoggerGetter;

public abstract class Application extends ConsoleApplication implements MainWindowHolder {
	
	protected void run() throws Exception {
		// Start the main window.
		class ApplicationRunnable implements Runnable {
			private final Application m_app;
			public ApplicationRunnable(Application app) {
				m_app = app;
			}
			public void run() {
				LoggerGetter loggerGetter = null;
				try {
					loggerGetter = m_app.getLoggerGetter();
					WindowInitializer.create(m_app).initialize(Locale.getDefault());
				} catch (Throwable e) {
					ExceptionHandler.handleException(loggerGetter, Locale.getDefault(), e);
				}
			}
		}
		SwingUtilities.invokeLater(new ApplicationRunnable(this));
	}

}
