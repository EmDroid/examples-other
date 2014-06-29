/* $Id: ApplicationException.java 438 2010-08-03 15:26:43Z styryx $ */

package com.styryx.app;

import com.styryx.app.ConsoleApplication.ReturnCode;

/**
 * The application exception main class.
 * 
 * @author Emil Maskovsky
 */
public class ApplicationException extends Exception {
	
	private final ReturnCode m_code;
	
	/**
	 * The application exception main class.
	 * 
	 * @param message the detail message
	 * @param code the application error code
	 */
	ApplicationException(String message, ReturnCode code) {
		super(message);
		m_code = code;
	}

	/**
	 * The application exception main class.
	 * <p>
	 * Associates the exception with the {@link ConsoleApplication#RC_FAILURE}
	 * error code.
	 * 
	 * @param message the detail message
	 */
	public ApplicationException(String message) {
		this(message, ReturnCode.FAILURE);
	}

	public ReturnCode getCode() {
		return m_code;
	}

}
