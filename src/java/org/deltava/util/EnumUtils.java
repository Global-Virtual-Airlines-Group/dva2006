// Copyright 2020, 2025, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;
import java.lang.reflect.Method;

/**
 * A utility class for enumeration operations.
 * @author Luke
 * @version 12.4
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

	/**
	 * Creates an Iterator of Enumeration values.
	 * @param c the Enumeration class
	 * @return an Iterator of Enumeration values
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> Iterator<E> iterator(Class<E> c) {
		try {
			Method m = c.getMethod("values");
			E[] values = (E[]) m.invoke(null);
			return List.of(values).iterator();
		} catch (Exception e) {
			return Collections.emptyIterator();
		}
	}
}