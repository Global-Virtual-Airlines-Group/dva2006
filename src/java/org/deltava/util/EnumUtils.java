// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.lang.reflect.Method;

/**
 * A utility class for enumeration operations.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class EnumUtils {

	// static class
	private EnumUtils() {
		super();
	}
	
	/**
	 * Exception-safe enumeration parser.
	 * @param <E> the Enumeration
	 * @param c the Enumeration Class
	 * @param value the value to parse
	 * @param defaultValue the default value if invalid or an error occurs
	 * @return an Enumeration value
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> E parse(Class<E> c, String value, E defaultValue) {
		try {
			Method m = c.getMethod("valueOf", String.class);
			E e = (E) m.invoke(null, value.toUpperCase());
			return (e != null) ? e : defaultValue;
		} catch (Exception e) {
			return defaultValue;
		}
	}
}