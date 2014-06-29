/* $Id: CommandLineParser.java 438 2010-08-03 15:26:43Z styryx $ */

package com.styryx.app;

import java.util.ArrayList;
import java.util.List;

/**
 * Command line parser.
 * 
 * @author Emil Maskovsky
 */
public class CommandLineParser {
	
	/**
	 * Command line parameter.
	 * 
	 * @author Emil Maskovsky
	 */
	private class Parameter {

		/**
		 * Parameter description.
		 */
		private final String m_description;
		
		/**
		 * The parameter value.
		 */
		private String m_value;

		/**
		 * Command line parameter.
		 * 
		 * @param description parameter description
		 */
		public Parameter(String description) {
			m_description = description;
		}

		/**
		 * Get the parameter description.
		 * @return
		 */
		public String description() {
			return m_description;
		}

		/**
		 * Set the parameter value.
		 * 
		 * @param value the new parameter value
		 */
		public void setValue(String value) {
			if (null == value) {
				throw new IllegalArgumentException("the value of parameter cannot be null");
			}
			m_value = value;
		}
		
		/**
		 * Get the parameter value.
		 * @return
		 */
		public String getValue() {
			return m_value;
		}

	}

	/**
	 * List of parameters.
	 */
	private final List<Parameter> m_params = new ArrayList<Parameter>();
	
	/**
	 * The application name.
	 */
	private final String m_appName;
	
	public CommandLineParser(String appName) {
		m_appName = appName;
	}

	/**
	 * Add command line parameter.
	 * 
	 * @param description parameter description
	 */
	public CommandLineParser addParam(String description) {
		if (null == description) {
			throw new IllegalArgumentException("the command line parameter description cannot be null");
		}
		m_params.add(new Parameter(description));
		return this;
	}

	/**
	 * Add command line switch.
	 * 
	 * @param name switch name
	 * @param longName switch long name (can be <code>null</code>, in such case no long name is used)
	 * @param description switch description
	 * @return
	 */
	public CommandLineParser addSwitch(String name, String longName, String description) {
		if (null == name) {
			throw new IllegalArgumentException("the command line switch name cannot be null");
		}
		if (null == description) {
			throw new IllegalArgumentException("the command line switch description cannot be null");
		}
		return this;
	}

	/**
	 * Parse the arguments.
	 * 
	 * @param args command line arguments
	 * 
	 * @throws CommandLineParseException the command line error.
	 */
	public CommandLineParser parseArguments(String[] args) throws CommandLineParseException {
		boolean usage = true;
		if (args.length < m_params.size()) {
			if (usage) {
				showUsage();
			}
			throw new CommandLineParseException("missing command line parameters");
		}
		if (args.length > m_params.size()) {
			if (usage) {
				showUsage();
			}
			System.err.println("too much command line parameters");
		}
		for (int i = 0; i < m_params.size(); ++i) {
			m_params.get(i).setValue(args[i]);
		}
		return this;
	}

	/**
	 * Show the usage.
	 */
	private void showUsage() {
		StringBuffer buff = new StringBuffer();
		buff.append(m_appName);
		for (Parameter param : m_params) {
			buff.append(" <");
			buff.append(param.description());
			buff.append(">");
		}
		System.out.println(buff);
	}

	/**
	 * Get the <code>n</code>-th parameter.
	 * 
	 * @param n the ordinal number of parameter
	 * 
	 * @return
	 * The parameter value.
	 */
	public String getParam(int n) {
		return m_params.get(n).getValue();
	}

}
