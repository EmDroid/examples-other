package com.styryx.app.applet;

import java.applet.AppletContext;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JApplet;

import com.styryx.app.ExceptionHandler;
import com.styryx.gui.CloseHandler;
import com.styryx.gui.RootPanelHolder;
import com.styryx.gui.components.Hyperlink;
import com.styryx.util.LoggerGetter;

public abstract class Applet extends JApplet implements RootPanelHolder, CloseHandler {
	
	private static Logger getDefaultLogger() {
		return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	}

	private LoggerGetter m_loggerGetter = new LoggerGetter() {
		public Logger getLogger() {
			return getLoggerInternal();
		}
	};
	
	/** The close JavaScript. */
	private String m_close = null;
	
	private class LinkOpener extends Hyperlink.LinkOpener {
//		private final Applet m_applet;
		private final AppletContext m_context;
		public LinkOpener(Applet applet) {
//			m_applet = applet;
			m_context = applet.getAppletContext();
		}
		protected void openLink(URI link, String target) throws IOException {
//			try {
//				final netscape.javascript.JSObject win = netscape.javascript.JSObject.getWindow(m_applet);
//				win.eval("window.open('" + link + "', '" + target + "');");
//			} catch (Throwable e) {
				// Ignore the exception and try to launch normally.
				m_context.showDocument(link.toURL(), target);
//			}
		}
	}
	
	/** {@inheritDoc} */
	final public void init() {
		try {
			Hyperlink.setLinkOpener(new LinkOpener(this));
			super.init();
			m_close = this.getParameter("close");
			RootPanelHolder.PanelInitializer.create(this).initialize(getRootPane(), getLocale(), this);
			onInit();
		} catch (Throwable e) {
			handleException(e);
		}
	}
	
	/** {@inheritDoc} */
	final public void start() {
		try {
			super.start();
			onStart();
		} catch (Throwable e) {
			handleException(e);
		}
	}

	/** {@inheritDoc} */
	final public void stop() {
		try {
			super.stop();
			onStop();
		} catch (Throwable e) {
			handleException(e);
		}
	}

	/** {@inheritDoc} */
	final public void destroy() {
		try {
			super.destroy();
			onDestroy();
		} catch (Throwable e) {
			handleException(e);
		}
	}

	protected void onInit() throws Exception {
		// Nothing done by default.
	}

	protected void onStart() throws Exception {
		// Nothing done by default.
	}

	protected void onStop() throws Exception {
		// Nothing done by default.
	}

	protected void onDestroy() throws Exception {
		// Nothing done by default.
	}

	public void close() {
		if (getAppletContext().toString().startsWith("sun.applet.AppletViewer")) {
			// Not running under browser - exit normally.
			stop();
			destroy();
			System.exit(0);
		}
		if ((m_close != null) && (!"".equals(m_close.trim()))) {
			try {
				final netscape.javascript.JSObject win = netscape.javascript.JSObject.getWindow(this);
				win.eval(m_close);
			} catch (Throwable e) {
				handleException(e);
			}
		}
	}

	private Logger getLoggerInternal() {
		try {
			return getLogger();
		} catch (Throwable e) {
			Logger logger = getDefaultLogger();
			logger.log(Level.SEVERE, "Error getting specific logger!", e);
			return logger;
		}
	}

	protected Logger getLogger() {
		return getDefaultLogger();
	}

	protected void handleException(Throwable e) {
		ExceptionHandler.handleException(m_loggerGetter, getLocale(), e);
	}

}
