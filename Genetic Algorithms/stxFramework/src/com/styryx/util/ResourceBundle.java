package com.styryx.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResourceBundle {
	
	final private static Logger logger = Logger.getLogger(ResourceBundle.class.getName());
	
//	private static ResourceBundle sm_frameworkBundle = getBundle("stxFramework", Locale.getDefault());
	
	public static ResourceBundle getFrameworkBundle(Locale locale) {
		return getBundle("stxFramework", locale, null);
	}
	
	public static ResourceBundle getBundle(final String baseName, Locale locale) {
		return getBundle(baseName, locale, getFrameworkBundle(locale));
	}

	public static ResourceBundle getBundle(final String baseName, Locale locale, final ResourceBundle parent) {
		java.util.ResourceBundle bundle;
		try {
			bundle = java.util.ResourceBundle.getBundle(baseName, locale);
			if (!locale.getLanguage().equalsIgnoreCase(bundle.getLocale().getLanguage())) {
				bundle = null;
			}
		} catch (MissingResourceException e) {
			logger.log(Level.FINE, e.getLocalizedMessage());
			// If the resource bundle not found, the messages will not be loaded and the default
			// values will be always returned (the m_bundle will be initialized with null, so it
			// will not be used).
			bundle = null;
		}
		return new ResourceBundle(bundle, parent);
	}

	final private java.util.ResourceBundle m_bundle;
	final private ResourceBundle m_parent;

	private ResourceBundle(final java.util.ResourceBundle bundle, final ResourceBundle parent) {
		m_bundle = bundle;
		m_parent = parent;
	}

	public String getString(final String key) {
		// Use the key as the default value.
		return getString(key, key);
	}

	public String getString(final String key, final String defValue) {
		if (m_bundle != null) {
			// If the resource bundle was found, try to get the message from it.
			try {
				return m_bundle.getString(key);
			} catch (MissingResourceException e) {
				logger.log(Level.FINE, e.getLocalizedMessage());
				// Could not get the message - will continue with parent.
			}
		}
		if (m_parent != null) {
			// Try to get the message from the parent resource bundle.
			return m_parent.getString(key, defValue);
		}
		if (defValue != null) {
			// If default value set, use it.
			return defValue;
		}
		return key;
	}

}
