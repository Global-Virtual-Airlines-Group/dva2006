// Copyright 2005, 2006, 2007, 2008, 2009, 2012, 2016, 2019, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.functions;

import java.util.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.Temporal;

import org.deltava.util.*;

/**
 * A JSP Function Library to store miscellaneous functions.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class MiscFunctions {

	// static class
	private MiscFunctions() {
		super();
	}
	
	/**
	 * A JSP-friendly function to get an element from a List. Equivalent to {@link List#get(int) }.
	 * @param l the List containing the elements
	 * @param idx the element position within the List
	 * @return the element, or null if an invalid index is specified
	 */
	public static <T> T get(List<T> l, int idx) {
		try {
			return l.get(idx);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns the first object in a Collection.
	 * @param c the Collection
	 * @return the first Object returned by the Collection's iterator.
	 */
	public static <T> T first(Collection<T> c) {
		return CollectionUtils.isEmpty(c) ? null : c.iterator().next();
	}

	/**
	 * Returns a subset of a Collection.
	 * @param c the Collection
	 * @param size the number of entries to return
	 * @return a Collection with the specified number of entries, or less if c is smaller than size
	 */
	public static <T> Collection<T> subset(Collection<T> c, int size) {
		if (c == null)
			return null;
		else if (c.size() <= size)
			return c;

		// Reduce the size
		List<T> results = new ArrayList<T>(c);
		return results.subList(0, size);
	}

	/**
	 * A JSP-friendly function to check if an object exists within a Collection.
	 * @param c the Collection
	 * @param obj the Object
	 * @return TRUE if c is not null and c.contains(obj), otherwise FALSE
	 * @see Collection#contains(Object)
	 */
	public static <T> boolean contains(Collection<T> c, T obj) {
		return (c != null) && c.contains(obj);
	}

	/**
	 * &quot;Escape&quot; a string by stripping out all inline HTML.
	 * @param s the String to format
	 * @return the escaped String
	 * @see StringUtils#stripInlineHTML(String)
	 */
	public static String escape(String s) {
		return StringUtils.stripInlineHTML(s);
	}
	
	/**
	 * Performs a search and replace operation.
	 * @param s the string to update
	 * @param old the subtext to remove
	 * @param n the subtext to replace with
	 * @return the updated string
	 * @since 1.1
	 */
	public static String replace(String s, String old, String n) {
		return s.replace(old, n);
	}

	/**
	 * Concatenates a series of list elements into a String.
	 * @param c a Collection of objects
	 * @param delim the delimiter
	 * @return a delimited String
	 */
	public static String splice(Collection<?> c, String delim) {
		return StringUtils.listConcat(c, delim);
	}

	/**
	 * Converts a number to hexadecimal.
	 * @param val the number to convert
	 * @return the converted number, with &quot;0x&quot; prepended to it
	 * @see StringUtils#formatHex(long)
	 */
	public static String toHex(long val) {
		return StringUtils.formatHex(val);
	}

	/**
	 * Converts a string to lower case.
	 * @param s the string to convert
	 * @return the lower case string, or null
	 */
	public static String toLower(String s) {
		return (s == null) ? null : s.toLowerCase();
	}

	/**
	 * Converts a string to upper case.
	 * @param s the string to convert
	 * @return the upper case string, or null
	 */
	public static String toUpper(String s) {
		return (s == null) ? null : s.toUpperCase();
	}
	
	/**
	 * Generates a CSS color with alpha. 
	 * @param c the RGB color
	 * @param a the alpha factor from 0 to 1
	 * @return a CSS color with alpha
	 */
	public static String toRGBA(int c, float a) {
		StringBuilder sb = new StringBuilder("rgba(");
		sb.append((c >> 16) & 0xFF);
		sb.append(',');
		sb.append((c >> 8) & 0xFF);
		sb.append(',');
		sb.append(c & 0xFF);
		sb.append(',');
		sb.append(a);
		sb.append(");");
		return sb.toString();
	}

	/**
	 * Formats a Date into a String for use in a JSP Tag parameter.
	 * @param dt the date/time value
	 * @param fmt the format pattern
	 * @return the formatted date/time
	 */
	public static String format(Temporal dt, String fmt) {
		ZonedDateTime zdt = null;
		if (dt instanceof Instant i)
			zdt = ZonedDateTime.ofInstant(i, ZoneOffset.UTC);
		else if (dt instanceof ZonedDateTime)
			zdt = (ZonedDateTime) dt;
		else if (dt instanceof LocalDate ld)
			zdt = ZonedDateTime.of(ld, LocalTime.of(0, 0), ZoneOffset.UTC);
		else if (dt != null)
			throw new IllegalArgumentException("Unknown temporal type - " + dt.getClass().getName());
			
		DateTimeFormatter df = new DateTimeFormatterBuilder().appendPattern(fmt).toFormatter();
		return (zdt == null) ? "" : df.format(zdt);
	}

	/**
	 * Displays the time difference between two date/times.
	 * @param t1 the first date/time
	 * @param t2 the second date/time
	 * @return the difference in seconds, or 0 if either date is null
	 */
	public static long difference(Temporal t1, Temporal t2) {
		return ((t1 == null) || (t2 == null)) ? 0 : Duration.between(t1, t2).getSeconds();
	}
}