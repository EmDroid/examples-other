/* $Id: CommandLineParseException.java 432 2009-03-26 22:20:41Z styryx $ */

package com.styryx.app;

/**
 * Command line parser exception.
 * 
 * @author Emil Maskovsky
 */
public class CommandLineParseException extends ApplicationException {

	/**
	 * Command line parser exception.
	 * 
	 * @param message the detail message
	 */
	public CommandLineParseException(String message) {
		super(message);
	}

}
