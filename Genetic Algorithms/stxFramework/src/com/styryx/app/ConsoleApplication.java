/* $Id: ConsoleApplication.java 556 2010-09-02 08:51:07Z styryx $ */

package com.styryx.app;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.styryx.util.LoggerGetter;

/**
 * Console application main class.
 * 
 * @author Emil Maskovsky
 */
public abstract class ConsoleApplication {

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

		ConsoleApplication createApplication();
		
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
			final ConsoleApplication app = initializer.createApplication();
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

	private static Logger getDefaultLogger() {
		return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	}

//	final private String[] m_args;
	
	private ReturnCode m_returnCode;

	private LoggerGetter m_loggerGetter;
	
	protected ConsoleApplication() {
//		m_args = args;
		// Success bu default.
		m_returnCode = ReturnCode.SUCCESS;
		m_loggerGetter = null;
	}
	
	protected void setReturnCode(ReturnCode rc) {
		m_returnCode = rc;
	}

	protected LoggerGetter getLoggerGetter() {
		if (null == m_loggerGetter) {
			m_loggerGetter = new LoggerGetter() {
				public Logger getLogger() {
					try {
						return getLoggerInternal();
					} catch (Throwable e) {
						return getDefaultLogger();
					}
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
		cmdLine.addSwitch("h", "help", "usage");
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
	protected abstract void run() throws Exception;

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

}
