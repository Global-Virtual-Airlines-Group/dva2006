// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.functions;

import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.deltava.util.StringUtils;

/**
 * A JSP Function Library to store miscellaneous functions.
 * @author Luke
 * @version 1.0
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
	 * A JSP-friendly function to get an element from a List. Equivalent to {@link List#get(int) }.
	 * @param l the List containing the elements
	 * @param idx the element position within the List
	 * @return the element
	 * @throws IndexOutOfBoundsException if the index is out of range
	 */
	public static Object get(List l, int idx) {
		return l.get(idx);
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
	 * Converts a number to hexadecimal.
	 * @param val the number to convert
	 * @return the converted number, with &quot;0x&quot; prepended to it
	 * @see StringUtils#formatHex(long)
	 */
	public static String toHex(long val) {
		return StringUtils.formatHex(val);
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
}