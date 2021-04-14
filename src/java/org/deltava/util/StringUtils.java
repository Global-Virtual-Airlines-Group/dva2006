// Copyright 2005, 2006, 2007, 2009, 2010, 2012, 2016, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.text.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoField;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

/**
 * A String utility class.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public final class StringUtils {

	private static final String UPPER_AFTER = " \'-";
	
	private static final String RESERVED_CHARS = "<>&\"\'\\\n:";
	private static final String[] REPLACE_ENTITIES = {"&lt;", "&gt;", "&amp;", "&quot;", "&#039;", "&#092;", "<br />\n", "&#58;"};

	// We're a singleton, alone and lonely
	private StringUtils() {
		super();
	}

	/**
	 * A null-safe mechanism to check if a String is null or empty.
	 * @param s the String to check
	 * @return TRUE if s is null or empty, otherwise FALSE
	 */
	public static boolean isEmpty(CharSequence s) {
		return ((s == null) || (s.length() == 0));
	}
	
	/**
	 * Removes leading and trailing double-quotes from a CSV string etntity. This will only remove a trailing quote if a leading quote is present.
	 * @param s the string
	 * @return the string minus any leading/trailing quotes
	 */
	public static String removeCSVQuotes(String s) {
		if ((s == null) || (s.length() < 2)) return s;
		boolean leadingQ = (s.charAt(0) == '"');
		boolean trailingQ = (s.charAt(s.length() - 1) == '"');
		return (leadingQ && trailingQ) ? s.substring(1, s.length() - 1) : s;
	}
	
	/**
	 * Escapes single and double quotes in a string with a leading backslash.
	 * @param s the string
	 * @return the escaped string
	 * @throws NullPointerException if s is null
	 */
	public static String escapeQuotes(String s) {
		StringBuilder buf = new StringBuilder(s.length() + 4);
		for (int x = 0; x < s.length(); x++) {
			char c = s.charAt(x);
			if ((c == '\'') || (c == '\"') || (c == '\\'))
				buf.append('\\');
			
			buf.append(c);
		}
		
		return buf.toString();
	}

	/**
	 * Converts a string to "proper case".
	 * @param s the string to convert
	 * @return the converted string
	 * @throws NullPointerException if the string is null
	 */
	public static String properCase(String s) {
		StringBuilder buf = new StringBuilder(s.length() + 2);
		buf.append(Character.toUpperCase(s.charAt(0))); // Convert first character to uppercase always
		for (int x = 1; x < s.length(); x++) {
			char c = s.charAt(x);
			if (UPPER_AFTER.indexOf(s.charAt(x - 1)) != -1)
				buf.append(Character.toUpperCase(c));
			else
				buf.append(Character.toLowerCase(c));
		}

		return buf.toString();
	}
	
	/**
	 * Filters a string by passing in a character filtering Predicate.
	 * @param s the string
	 * @param p the Predicate
	 * @return the filtered string, or null
	 */
	public static String filter(String s, Predicate<Character> p) {
		if (s == null) return null;
		StringBuilder buf = new StringBuilder();
		for (int x = 0; x < s.length(); x++) {
			char c = s.charAt(x);
			if (p.test(Character.valueOf(c)))
				buf.append(c);
		}
		
		return buf.toString();
	}

	/**
	 * Strips out inline HTML in a string by replacing &lt; and &gt; with <code>&lt;</code> and <code>&gt;</code>.
	 * @param s the string to Convert
	 * @return the formatted string, null if s is null
	 */
	public static String stripInlineHTML(String s) {
		if (s == null) return null;

		StringBuilder buf = new StringBuilder(s.length() + 16);
		for (int x = 0; x < s.length(); x++) {
			char c = s.charAt(x);
			int pos = RESERVED_CHARS.indexOf(c);
			if (pos > -1)
				buf.append(REPLACE_ENTITIES[pos]);
			else
				buf.append(c);
		}

		return buf.toString();
	}
	
	/**
	 * Concatenates a collection of Strings into a single delimited String.
	 * @param values the List of values
	 * @param delim the value delimiter
	 * @return the delimited value stirng
	 */
	public static String listConcat(Collection<?> values, String delim) {
		if (values == null) return "";
		StringBuilder buf = new StringBuilder(64);
		for (Iterator<?> i = values.iterator(); i.hasNext();) {
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
		StringBuilder buf = new StringBuilder("get");
		buf.append(Character.toUpperCase(pName.charAt(0)));
		return buf.append(pName.substring(1)).toString();
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
	 * Parses a string and converts into a 32-bit integer.
	 * @param value the value to convert
	 * @param defaultValue the default value
	 * @return the value parsed into a number, or the default value if conversion fails
	 */
	public static int parse(String value, int defaultValue) {
		if (isEmpty(value)) return defaultValue;
		try {
			return parseHex(value);
		} catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}
	
	/**
	 * Parses a string and converts into a 64-bit integer.
	 * @param value the value to convert
	 * @param defaultValue the default value
	 * @param isUnsigned TRUE to treat as an unsigned integer, otherwise FALSE
	 * @return the value parsed into a number, or the default value if conversion fails
	 */
	public static long parse(String value, long defaultValue, boolean isUnsigned) {
		if (isEmpty(value)) return defaultValue;
		try {
			return isUnsigned ? Long.parseUnsignedLong(value) : Long.parseLong(value);
		} catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}
	
	/**
	 * Parses a string and converts into a floating-point number.
	 * @param value the value to convert
	 * @param defaultValue the default value
	 * @return the value parsed into a number, or the default value if conversion fails
	 */
	public static double parse(String value, double defaultValue) {
		if (isEmpty(value)) return defaultValue;
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}
	
	/**
	 * Parses a date using a given format pattern.
	 * @param dt the date to parse
	 * @param fmt the format pattern
	 * @return the parsed Date
	 * @see LocalDateTime#parse(java.lang.CharSequence)
	 */
	public static Instant parseInstant(String dt, String fmt) {
		DateTimeFormatterBuilder dfb = new DateTimeFormatterBuilder().appendPattern(fmt);
		dfb.parseDefaulting(ChronoField.HOUR_OF_DAY, 0);
		dfb.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0);
		dfb.parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0);
		LocalDateTime ldt = LocalDateTime.parse(dt, dfb.toFormatter());
		return ldt.toInstant(ZoneOffset.UTC);
	}
	
	/**
	 * Parses a date using a given format pattern.
	 * @param dt the date to parse
	 * @param fmt the format pattern
	 * @param tz the ZoneId
	 * @return the parsed Date
	 * @see LocalDateTime#parse(java.lang.CharSequence)
	 */
	public static ZonedDateTime parseLocal(String dt, String fmt, ZoneId tz) {
		DateTimeFormatterBuilder dfb = new DateTimeFormatterBuilder().appendPattern(fmt);
		dfb.parseDefaulting(ChronoField.HOUR_OF_DAY, 0);
		dfb.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0);
		dfb.parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0);
		LocalDateTime ldt = LocalDateTime.parse(dt, dfb.toFormatter());
		return ZonedDateTime.of(ldt, tz);
	}
	
	/**
	 * Parses an epoch Date.
	 * @param dt the date in milliseconds since Epoch
	 * @return a Date, or null
	 */
	public static Instant parseEpoch(String dt) {
		try {
			return Instant.ofEpochMilli(Long.parseLong(dt));
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Helper method to parse RFC3339 date strings.
	 * @param dt the date/time string
	 * @return a Date
	 * @throws IllegalArgumentException if the date/time is not in RFC3339 format
	 */
	public static Instant parseRFC3339Date(String dt) {
		return parseInstant(dt, "yyyy-MM-dd'T'HH:mm:ssZ");
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
		StringBuilder buf = new StringBuilder(32);

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
	 */
	public static String format(Instant dt, String fmtPattern) {
		return format(ZonedDateTime.ofInstant(dt, ZoneOffset.UTC), fmtPattern);
	}
	
	/**
	 * Formats a date into a string using a particular pattern.
	 * @param d the date
	 * @param fmtPattern the formatter pattern
	 * @return the formatted date
	 */
	public static String format(LocalDate d, String fmtPattern) {
		return DateTimeFormatter.ofPattern(fmtPattern).format(d);
	}

	/**
	 * Formats a date/time into a string using a particular pattern.
	 * @param dt the date/time
	 * @param fmtPattern the formatter pattern
	 * @return the formatted date/time
	 */
	public static String format(ZonedDateTime dt, String fmtPattern) {
		return DateTimeFormatter.ofPattern(fmtPattern).format(dt);
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
		StringBuilder buf = new StringBuilder(s.length() + 4);
		for (int x = 0; x < s.length(); x++) {
			char c = s.charAt(x);
			if (chars.indexOf(c) == -1)
				buf.append(c);
		}

		return buf.toString();
	}
	
	/**
	 * Trims a Collection of Strings, removing elements if the string is empty.
	 * @param s the Collection of Strings to trim
	 * @return a List of Strings
	 */
	public static List<String> nullTrim(Collection<String> s) {
		return (s == null) ? null : s.stream().filter(e -> !isEmpty(e)).collect(Collectors.toList());
	}
	
	/**
	 * Returns the query parameters on a URL.
	 * @param url the URL
	 * @return a Map of parameters and values
	 */
	public static Map<String, String> getURLParameters(String url) {
		if (isEmpty(url) || (url.indexOf('?') == -1))
			return Collections.emptyMap();
		
		Map<String, String> results = new LinkedHashMap<String, String>();
		StringTokenizer pTkns = new StringTokenizer(url.substring(url.indexOf('?') + 1), "&");
		while (pTkns.hasMoreTokens()) {
			StringTokenizer vTkns = new StringTokenizer(pTkns.nextToken(), "=");
			String k = vTkns.nextToken();
			results.put(k, vTkns.hasMoreTokens() ? vTkns.nextToken() : "");
		}
		
		return results;
	}
	
	/**
	 * Parses a line of CSV-fomratted text.
	 * @param data the text
	 * @return a CSVTokens object
	 */
	public static CSVTokens parseCSV(String data) {
		return new CSVTokens(data);
	}
}