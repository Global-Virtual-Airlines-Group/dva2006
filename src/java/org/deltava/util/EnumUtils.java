// Copyright 2020, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.lang.reflect.Method;

/**
 * A utility class for enumeration operations.
 * @author Luke
 * @version 11.5
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
	
	/**
	 * Returns the higher of two enumerations. 
	 * @param e1 the first enum
	 * @param e2 the second enum
	 * @return the larger enum based on ordinal
	 */
	public static <E extends Enum<E>> E max(E e1, E e2) {
		return (e1.ordinal() > e2.ordinal()) ? e1 : e2;
	}
}