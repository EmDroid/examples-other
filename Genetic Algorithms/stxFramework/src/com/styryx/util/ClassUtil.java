package com.styryx.util;

public class ClassUtil {

	public static String getSimpleName(Class<?> app) {
		String className = app.getName();
		int dot = className.lastIndexOf('.');
		if (-1 == dot) {
			return className;
		} else {
			return className.substring(dot + 1);
		}
	}

}
