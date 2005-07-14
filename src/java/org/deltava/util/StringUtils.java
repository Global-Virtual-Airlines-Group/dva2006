// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.util;

import java.util.*;

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
    public static boolean isEmpty(String s) {
    	return ((s == null) || (s.length() == 0));
    }
    
    /**
     * Converts a string to "proper case".
     * @param s The string to convert
     * @return the converted string
     * @throws NullPointerException if the string is null
     */
    public static String properCase(String s) {
        StringBuffer buf = new StringBuffer();
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
     * Strips out inline HTML in a string by replacing &lt; and &gt; with <code>&lt;</code> and
     * <code>&gt;</code>.
     * @param s the string to Convert
     * @return the formatted string, null if s is null
     */
    public static String stripInlineHTML(String s) {
        if (s == null)
            return null;

        StringBuffer buf = new StringBuffer();
        for (int x = 0; x < s.length(); x++) {
            char c = s.charAt(x);
            if (c == '<') {
                buf.append("&lt;");
            } else if (c == '>') {
                buf.append("&gt;");
            } else if (c == '&') {
                buf.append("&amp;");
            } else if (c == '\"') {
                buf.append("&quot;");
            } else if (c == '\'') {
                buf.append("&#039;");
            } else if (c == '\\') {
                buf.append("&#092;");
            } else if (c == '\n') {
                buf.append("<br />");
                buf.append(System.getProperty("line.separator"));
            } else {
                buf.append(c);
            }
        }

        return buf.toString();
    }
    
    /**
     * Returns a null-safe trimmed string.
     * @param rawString the string to trim
     * @return a trimmed string, or EMPTY_STRING if rawString is null
     * @see String#trim()
     */
    public static String trim(String rawString) {
        return (rawString == null) ? "" : rawString.trim();
    }
    
    /**
     * Concatenates a collection of Strings into a single delimited String.
     * @param values the List of values
     * @param delim the value delimiter
     * @return the delimited value stirng
     */
    public static String listConcat(Collection values, String delim) {
        StringBuffer buf = new StringBuffer();
        for (Iterator i = values.iterator(); i.hasNext(); ) {
            buf.append(String.valueOf(i.next()));
            if (i.hasNext())
                buf.append(delim);
        }
        
        return buf.toString();
    }
    
    /**
     * Returns the method name used to retrieve a particular property value. This conforms to
     * the Java Bean specification by prepending the string &quot;get&quot; to the property name,
     * and converting the first character of the property name to uppercase.  
     * @param pName the property name
     * @return the name of the get method for this property
     */
    public static String getPropertyMethod(String pName) {
    	return "get" + pName.substring(0, 1).toUpperCase() + pName.substring(1);
    }
    
    /**
     * Parses a string that may be in hexadecimal. If the string starts with &quot;0x&quot;, it is treated
     * as a hex string, otherwise it is parsed as a regular number.
     * @param hexValue the number to parse
     * @return the value
     * @throws NumberFormatException if hexValue cannot be converted to a number
     */
    public static int parseHex(String hexValue) {
        return (!hexValue.startsWith("0x")) ? Integer.parseInt(hexValue) : Integer.parseInt(hexValue.substring(2), 16);
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
       if (values == null)
          return -1;
       
       for (int x = 0; x < values.length; x++) {
          if (values[x].equals(value))
             return x;
       }
       
       return -1;
    }
    
    /**
     * Splits a delimited String into a List of elements.
     * @param s the String
     * @param delim the delimiter
     * @return a List of String
     * @see StringTokenizer#StringTokenizer(String, String)
     */
    public static List split(String s, String delim) {
    	
    	List results = new ArrayList();
    	StringTokenizer tk = new StringTokenizer(s, delim, true);
        String lastEntry = "";
        
        // Parse empty tokens properly
        while (tk.hasMoreTokens()) {
        	String entry = tk.nextToken();
        	if (delim.equals(entry)) {
        		results.add(lastEntry);
        		lastEntry = "";
        	} else {
        		lastEntry = entry;
        	}
        }
    	
        if (!"".equals(lastEntry))
           results.add(lastEntry);
        
    	return results;
    }
}