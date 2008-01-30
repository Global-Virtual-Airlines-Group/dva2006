// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.functions;

import java.util.*;
import java.text.*;

import org.deltava.util.*;

/**
 * A JSP Function Library to store miscellaneous functions.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class MiscFunctions {

	/**
	 * A JSP-friendly way of getting the size of a Collection.
	 * @param c the Collection
	 * @return the number of elements
	 * @see Collection#size()
	 */
	public static int sizeof(Collection c) {
		return (c != null) ? c.size() : 0;
	}
	
	/**
	 * A JSP function to return a Map's keys.
	 * @param m the Map
	 * @return a Collection of keys, or null if m is null
	 */
	public static <T> Collection<T> keys(Map<T, Object> m) {
		return (m == null) ? null : m.keySet();
	}

	/**
	 * A JSP-friendly function to get an element from a List. Equivalent to {@link List#get(int) }.
	 * @param l the List containing the elements
	 * @param idx the element position within the List
	 * @return the element
	 * @throws IndexOutOfBoundsException if the index is out of range
	 */
	public static <T> T get(List<T> l, int idx) {
		return (l == null) ? null : l.get(idx);
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
		return (c == null) ? false : c.contains(obj);
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
	public static String splice(Collection c, String delim) {
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
	 * Formats a Date into a String for use in a JSP Tag parameter.
	 * @param dt the date/time value
	 * @param fmt the format pattern
	 * @return the formatted date/time
	 * @see java.text.DateFormat#format(java.util.Date)
	 */
	public static String format(Date dt, String fmt) {
		DateFormat df = new SimpleDateFormat(fmt);
		return (dt == null) ? "" : df.format(dt);
	}

	/**
	 * Displays the time difference between two date/times. Both date/times are assumed to be in the same time zone.
	 * @param d1 the first date/time
	 * @param d2 the second date/time
	 * @return the difference in seconds, or 0 if either date is null
	 */
	public static int difference(Date d1, Date d2) {
		if ((d1 == null) || (d2 == null))
			return 0;

		return (int) ((d2.getTime() - d1.getTime()) / 1000);
	}
}