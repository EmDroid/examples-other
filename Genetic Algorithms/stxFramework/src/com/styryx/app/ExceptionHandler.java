package com.styryx.app;

import java.io.PrintStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.styryx.util.LoggerGetter;
import com.styryx.util.ResourceBundle;

public class ExceptionHandler {
	
	public static void handleException(LoggerGetter loggerGetter, Locale locale, Throwable e) {
		try {
			/*
			Method getLogger = loggerClass.getMethod("getLogger");
			Logger logger = (Logger) getLogger.invoke(loggerClassInstance);
			*/
			Logger logger = (loggerGetter != null)
				? loggerGetter.getLogger()
				: Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
			/*
			if (null == logger) {
				logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
			}
			*/
			logger.log(Level.SEVERE,
					ResourceBundle.getFrameworkBundle(locale).getString("ExceptionCaught", "Exception caught") + "!", e);
		} catch(Throwable e1) {
			// Could not log normally - try to log to System.err at least.
			handleExceptionInternal(System.err, "Exception caught: ", e1);
			handleExceptionInternal(System.err, "When logging exception: ", e);
		}
	}
	
	private static void handleExceptionInternal(PrintStream s, String msg, Throwable e) {
		s.print(msg);
		try {
			e.printStackTrace(s);
		} catch (Throwable e1) {
			try {
				s.println(e.getMessage());
			} catch( Throwable e2) {
				s.println("Could not get exception description!");
			}
		}
	}

}
