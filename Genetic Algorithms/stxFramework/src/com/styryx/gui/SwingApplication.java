package com.styryx.gui;

import java.applet.Applet;
import java.applet.AppletContext;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JApplet;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.styryx.app.CommandLineParseException;
import com.styryx.app.CommandLineParser;
import com.styryx.app.ExceptionHandler;
import com.styryx.gui.components.Hyperlink;
import com.styryx.util.LoggerGetter;
import com.styryx.util.ResourceBundle;

public abstract class SwingApplication extends JApplet implements CloseHandler {
	
	private static Logger getDefaultLogger() {
		return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	}

	private LoggerGetter m_loggerGetter = null;
	
	protected LoggerGetter getLoggerGetter() {
		if (null == m_loggerGetter) {
			m_loggerGetter = new LoggerGetter() {
				public Logger getLogger() {
					return getLoggerInternal();
				}
			};
		}
		return m_loggerGetter;
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
	
	public JFrame initializeMainWindow(Locale locale, CloseHandler closeHandler) throws Exception {
		MainWindow mainWin = createMainWindow(locale);
		if (null == mainWin) {
			throw new NullPointerException("createMainWindow() must not return 'null'!");
		}
		mainWin.setCloseHandler(closeHandler);
		// Setup the window panel.
		JRootPane rootPane = mainWin.getRootPane();
		RootPanelHolder.PanelInitializer.create(mainWin).initialize(rootPane, locale, mainWin);
		// Setup the window listener.
//		if (null != m_closeHandler) {
//			mainWin.addWindowListener(this);
//		}
		// Set the close operation.
		mainWin.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// Setup the look and feel.
        if (JDialog.isDefaultLookAndFeelDecorated()) {
        	boolean supportsWindowDecorations =
        		UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations) {
            	mainWin.setUndecorated(true);
            	rootPane.setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
            }
        }
		// Show the window.
        mainWin.pack();
		mainWin.setVisible(true);
		return mainWin;
	}
	
	protected abstract MainWindow createMainWindow(Locale locale) throws Exception;
	
	public void initializeMainPanel(JRootPane rootPane) throws Exception {
		setupLookAndFeel();
		createMainPanel(rootPane);
		JMenuBar menu = createMenu();
		if (null != menu) {
			rootPane.setJMenuBar(menu);
		}
	}

	protected void setupLookAndFeel() throws Exception {
		// Set cross-platform look nad feel by default.
		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
	}

	protected JMenuBar createMenu() {
		// No menu by default.
		return null;
	}

	protected abstract void createMainPanel(JRootPane rootPane) throws Exception;

	////////////////////////////////
	// Application related stuff. //
	////////////////////////////////
	
	/**
	 * The application return code.
	 * 
	 * @author Emil Maskovsky
	 */
	public static class ReturnCode {

		/** Application succeeded. */
		public static final ReturnCode SUCCESS = new ReturnCode(0);
		/** Application failed. */
		public static final ReturnCode FAILURE = new ReturnCode(1);

		/**
		 * The associated return code.
		 */
		private final int m_code;

		/**
		 * The application return code.
		 * 
		 * @param code the associated return code
		 */
		public ReturnCode(int code) {
			m_code = code;
		}

		/**
		 * Get the associated return code.
		 * 
		 * @return
		 */
		public int getCode() {
			return m_code;
		}

	}
	
	protected interface Initializer {

		SwingApplication createApplication();
		
	}
	
	protected static void startup(Initializer initializer, String[] args) {
		LoggerGetter loggerGetter = null;
		try {
			// Default logger (app not created yet, creation of the app can throw).
			loggerGetter = new LoggerGetter() {
				public Logger getLogger() {
					return getDefaultLogger();
				}
			};
			final SwingApplication app = initializer.createApplication();
			loggerGetter = app.getLoggerGetter();
			ReturnCode returnCode = app.start(args);
			int rc = returnCode.getCode();
			if (ReturnCode.SUCCESS.getCode() != rc) {
				System.exit(rc);
			}
		} catch (Throwable e) {
			try {
				//TODO The locale from command line.
				ExceptionHandler.handleException(loggerGetter, Locale.getDefault(), e);
			} finally {
				System.exit(ReturnCode.FAILURE.getCode());
			}
		}
	}
	
	private ReturnCode m_returnCode = ReturnCode.SUCCESS;

	private ReturnCode start(String[] args) throws Exception {
		processCommandLineArguments(args);
		run();
		return m_returnCode;
	}
	
	/**
	 * Process command line arguments.
	 * 
	 * @param args command line arguments
	 * 
	 * @throws CommandLineParseException the command line error.
	 */
	private void processCommandLineArguments(String[] args) throws CommandLineParseException {
		CommandLineParser cmdLine = new CommandLineParser(getClass().getSimpleName());
		cmdLine.addSwitch("h", "help", ResourceBundle.getFrameworkBundle(getLocale()).getString("usage"));
		initCmdLine(cmdLine);
		// Process the command line arguments.
		cmdLine.parseArguments(args);
		processCmdLine(cmdLine);
	}
	
	/**
	 * Run the application.
	 * 
	 * @throws Exception application failure.
	 */
	protected void run() throws Exception {
		// Start the main window.
		class ApplicationRunnable implements Runnable {
			private final SwingApplication m_app;
			public ApplicationRunnable(SwingApplication app) {
				m_app = app;
			}
			public void run() {
				LoggerGetter loggerGetter = null;
				try {
					loggerGetter = m_app.getLoggerGetter();
					initializeMainWindow(getLocale(), null);
				} catch (Throwable e) {
					ExceptionHandler.handleException(loggerGetter, getLocale(), e);
				}
			}
		}
		SwingUtilities.invokeLater(new ApplicationRunnable(this));
	}
	
	/**
	 * Init the command line.
	 * 
	 * @param cmdLine the command line parser to be set up
	 */
	protected void initCmdLine(CommandLineParser cmdLine) {
		// Nothing done by default.
		// Override to provide custom functionality.
	}
	
	/**
	 * Process the command line.
	 * 
	 * @param cmdLine the parsed command line
	 * 
	 * @throws CommandLineParseException on parse error.
	 */
	protected void processCmdLine(CommandLineParser cmdLine) throws CommandLineParseException {
		// Nothing done by default.
		// Override to provide custom functionality.
	}
	
	///////////////////////////
	// Applet related stuff. //
	///////////////////////////

	/** The close JavaScript. */
	private String m_close = null;
	
	private class LinkOpener extends Hyperlink.LinkOpener {
		private final AppletContext m_context;
		public LinkOpener(Applet applet) {
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
			String locale = getParameter("locale");
			if (null != locale) {
				setLocale(new Locale(locale));
			}
			m_close = this.getParameter("close");
			initializeMainPanel(getRootPane());
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
		if (!isActive() || getAppletContext().toString().startsWith("sun.applet.AppletViewer")) {
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
	
}
