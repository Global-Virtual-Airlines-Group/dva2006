// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;
import java.text.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

/**
 * A common String utility class.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public final class StringUtils {

	private static final String UPPER_AFTER = " \'-";

	// We're a singleton, alone and lonely
	private StringUtils() {
	}

	/**
	 * A null-safe mechanism to check if a String is null or empty
	 * @param s the String to check
	 * @return TRUE if s is null or empty, otherwise FALSE
	 */
	public static boolean isEmpty(CharSequence s) {
		return ((s == null) || (s.length() == 0));
	}

	/**
	 * Converts a string to "proper case".
	 * @param s The string to convert
	 * @return the converted string
	 * @throws NullPointerException if the string is null
	 */
	public static String properCase(String s) {
		StringBuilder buf = new StringBuilder(s.length());
		buf.append(Character.toUpperCase(s.charAt(0))); // Convert first character to
		// uppercase always
		for (int x = 1; x < s.length(); x++) {
			char c = s.charAt(x);
			if (UPPER_AFTER.indexOf(s.charAt(x - 1)) != -1) {
				buf.append(Character.toUpperCase(c));
			} else {
				buf.append(Character.toLowerCase(c));
			}
		}

		return buf.toString();
	}

	/**
	 * Strips out inline HTML in a string by replacing &lt; and &gt; with <code>&lt;</code> and <code>&gt;</code>.
	 * @param s the string to Convert
	 * @return the formatted string, null if s is null
	 */
	public static String stripInlineHTML(String s) {
		if (s == null)
			return null;

		StringBuilder buf = new StringBuilder(s.length());
		for (int x = 0; x < s.length(); x++) {
			char c = s.charAt(x);
			if (c == '<')
				buf.append("&lt;");
			else if (c == '>')
				buf.append("&gt;");
			else if (c == '&')
				buf.append("&amp;");
			else if (c == '\"')
				buf.append("&quot;");
			else if (c == '\'')
				buf.append("&#039;");
			else if (c == '\\')
				buf.append("&#092;");
			else if (c == '\n') {
				buf.append("<br />");
				buf.append(System.getProperty("line.separator"));
			} else
				buf.append(c);
		}

		return buf.toString();
	}
	
	/**
	 * Escapes forward slashes and single quotes for use when writing HTML using JavaScript.
	 * @param rawString the string to escape
	 * @return an escaped string
	 * @throws NullPointerException if rawString is null
	 */
	public static String escapeSlashes(String rawString) {
		StringBuilder buf = new StringBuilder(rawString);
		for (int x = 0; x < buf.length(); x++) {
			if (buf.charAt(x) == '/') {
				buf.insert(x, '\\');
				x++;
			} else if (buf.charAt(x) == '\'') {
				buf.insert(x, '\\');
				x++;
			}
		}

		return buf.toString();
	}

	/**
	 * Concatenates a collection of Strings into a single delimited String.
	 * @param values the List of values
	 * @param delim the value delimiter
	 * @return the delimited value stirng
	 */
	public static String listConcat(Collection values, String delim) {
		StringBuilder buf = new StringBuilder(48);
		for (Iterator i = values.iterator(); i.hasNext();) {
			buf.append(String.valueOf(i.next()));
			if (i.hasNext())
				buf.append(delim);
		}

		return buf.toString();
	}

	/**
	 * Returns the method name used to retrieve a particular property value. This conforms to the Java Bean
	 * specification by prepending the string &quot;get&quot; to the property name, and converting the first character
	 * of the property name to uppercase.
	 * @param pName the property name
	 * @return the name of the get method for this property
	 */
	public static String getPropertyMethod(String pName) {
		return "get" + pName.substring(0, 1).toUpperCase() + pName.substring(1);
	}

	/**
	 * Parses a string that may be in hexadecimal. If the string starts with &quot;0x&quot;, it is treated as a hex
	 * string, otherwise it is parsed as a regular number.
	 * @param hexValue the number to parse
	 * @return the value
	 * @throws NumberFormatException if hexValue cannot be converted to a number
	 */
	public static int parseHex(String hexValue) {
		return (!hexValue.startsWith("0x")) ? Integer.parseInt(hexValue) : Integer.parseInt(hexValue.substring(2), 16);
	}

	/**
	 * Parses a string and converts into a number.
	 * @param value the value to convert
	 * @param defaultValue the default value
	 * @return the value parsed into a number, or the default value if conversion fails
	 */
	public static int parse(String value, int defaultValue) {
		if (isEmpty(value))
			return defaultValue;

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}
	
	/**
	 * Parses a date using a given format pattern.
	 * @param dt the date to parse
	 * @param fmt the format pattern
	 * @return the parsed Date
	 * @throws IllegalArgumentException if the date cannot be parsed
	 * @see DateFormat#parse(java.lang.String)
	 */
	public static Date parseDate(String dt, String fmt) {
		try {
			return new SimpleDateFormat(fmt).parse(dt);
		} catch (ParseException pe) {
			throw new IllegalArgumentException(pe);
		}
	}

	/**
	 * Converts a number to a hexadecimal value by prepending &quot;0x&quot; and converting to hexadecimal.
	 * @param value the number to convert
	 * @return the hexadecimal value
	 * @see StringUtils#parseHex(String)
	 */
	public static String formatHex(long value) {
		return "0x" + Long.toHexString(value);
	}
	
	/**
	 * Returns the index of a value within an array of Strings.
	 * @param values the array of Strings
	 * @param value the String to search for
	 * @return the offset of the value in the array, or -1 if not found
	 */
	public static int arrayIndexOf(String[] values, String value) {
		return arrayIndexOf(values, value, -1);
	}

	/**
	 * Returns the index of a value within an array of Strings.
	 * @param values the array of Strings
	 * @param value the String to search for
	 * @param defaultValue the default value to return if not found 
	 * @return the offset of the value in the array, or defaultValue if not found
	 */
	public static int arrayIndexOf(String[] values, String value, int defaultValue) {
		if ((values == null) || (value == null))
			return defaultValue;
		for (int x = 0; x < values.length; x++)
			if (value.equals(values[x]))
				return x;

		return defaultValue;
	}

	/**
	 * Splits a delimited String into a List of elements.
	 * @param s the String
	 * @param delim the delimiter
	 * @return a List of String
	 * @see StringTokenizer#StringTokenizer(String, String)
	 */
	public static List<String> split(String s, String delim) {
		if (s == null)
			return null;

		List<String> results = new ArrayList<String>();
		StringTokenizer tk = new StringTokenizer(s, delim, true);
		String lastEntry = "";

		// Parse empty tokens properly
		while (tk.hasMoreTokens()) {
			String entry = tk.nextToken();
			if (delim.equals(entry)) {
				results.add(lastEntry);
				lastEntry = "";
			} else
				lastEntry = entry;
		}

		if (!"".equals(lastEntry))
			results.add(lastEntry);

		return results;
	}

	/**
	 * Formats a Latitude/Longitude pair.
	 * @param loc the location
	 * @param asHTML TRUE if HTML formatting to be used, otherwise FALSE
	 * @param formatMask bitmask specifying the portion to format
	 * @return a formatted latitude/longitude
	 * @see GeoLocation#LATITUDE
	 * @see GeoLocation#LONGITUDE
	 */
	public static String format(GeoLocation loc, boolean asHTML, int formatMask) {
		StringBuilder buf = new StringBuilder(24);

		// Format the latitude
		if ((formatMask & GeoLocation.LATITUDE) != 0) {
			buf.append(Math.abs(GeoPosition.getDegrees(loc.getLatitude())));
			buf.append(asHTML ? "<sup>o</sup> " : " ");
			buf.append(GeoPosition.getMinutes(loc.getLatitude()));
			buf.append(asHTML ? "&#39; " : "\" ");
			buf.append(GeoPosition.getSeconds(loc.getLatitude()));
			buf.append(asHTML ? "&quot; " : "\" ");
			buf.append((GeoPosition.getDegrees(loc.getLatitude()) < 0) ? "S " : "N ");
		}

		// Format the longitude
		if ((formatMask & GeoLocation.LONGITUDE) != 0) {
			buf.append(Math.abs(GeoPosition.getDegrees(loc.getLongitude())));
			buf.append(asHTML ? "<sup>o</sup> " : " ");
			buf.append(GeoPosition.getMinutes(loc.getLongitude()));
			buf.append(asHTML ? "&#39; " : "\" ");
			buf.append(GeoPosition.getSeconds(loc.getLongitude()));
			buf.append(asHTML ? "&quot; " : "\" ");
			buf.append((GeoPosition.getDegrees(loc.getLongitude()) < 0) ? 'W' : 'E');
		}

		return buf.toString().trim();
	}

	/**
	 * Formats an integer into a string using a particular pattern.
	 * @param value the number to format
	 * @param fmtPattern the formatter pattern
	 * @return the formatted number
	 * @see StringUtils#format(double, String)
	 * @see NumberFormat#format(long)
	 */
	public static String format(long value, String fmtPattern) {
		return new DecimalFormat(fmtPattern).format(value);
	}

	/**
	 * Formats a floating-point number into a string using a particular pattern.
	 * @param value the number to format
	 * @param fmtPattern the formatter pattern
	 * @return the formatted number
	 * @see StringUtils#format(long, String)
	 * @see NumberFormat#format(long)
	 */
	public static String format(double value, String fmtPattern) {
		return new DecimalFormat(fmtPattern).format(value);
	}

	/**
	 * Formats a date/time into a string using a particular pattern.
	 * @param dt the date/time
	 * @param fmtPattern the formatter pattern
	 * @return the formatted date/time
	 * @see DateFormat#format(Date)
	 */
	public static String format(Date dt, String fmtPattern) {
		return new SimpleDateFormat(fmtPattern).format(dt);
	}

	/**
	 * Strips out specific charaters from a string.
	 * @param s the input string
	 * @param chars the characters to remove
	 * @return the string with no characters specified in chars
	 */
	public static String strip(CharSequence s, String chars) {
		if (s == null)
			return null;
		else if (chars == null)
			return s.toString();

		// Strip out the characters
		StringBuilder buf = new StringBuilder(s.length());
		for (int x = 0; x < s.length(); x++) {
			char c = s.charAt(x);
			if (chars.indexOf(c) == -1)
				buf.append(c);
		}

		return buf.toString();
	}
	
	/**
	 * Trims a String, returning null if the string is empty.
	 * @param s the string to trim
	 * @return null, or the trimmed string
	 * @see String#trim()
	 */
	public static String nullTrim(String s) {
		return isEmpty(s) ? null : s.trim();
	}
}