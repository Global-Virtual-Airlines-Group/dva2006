// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.ts3;

/**
 * A utility class to escape TS3 Server Query commands
 * @author Luke
 * @version 6.1
 * @since 6.1
 */

public class QueryEncoder {
	
	private static final char[] CHARS = {'\\', '/', ' ', '|', 0x07, 0x08, 0xc, 0xa, 0xd, 0x9, 0xb};
	private static final String[] ESC_CHARS = {"\\\\", "\\/", "\\s", "\\p", "\\a", "\\b", "\\f", "\\n", "\\r", "\\t", "\\v"};

	// singleton
	private QueryEncoder() {
		super();
	}

	/**
	 * Encodes a string for submission to the TeamSpeak 3 server query interface.
	 * @param cmd the string
	 * @return the encoded string
	 */
	public static String encode(String cmd) {
		String results = cmd;
		for (int x = 0; x < CHARS.length; x++) {
			int ofs = results.indexOf(CHARS[x]);
			while (ofs != -1) {
				StringBuilder buf = new StringBuilder(results.substring(0, ofs));
				buf.append(ESC_CHARS[x]);
				buf.append(results.substring(ofs + 1));
				results = buf.toString();
				ofs = results.indexOf(CHARS[x]);
			}
		}
		
		return results;
	}

	/**
	 * Decodes a string returned by the TeamSpeak 3 server query interface.
	 * @param cmd the string
	 * @return the decoded string
	 */
	public static String decode(String cmd) {
		String results = cmd;
		for (int x = 0; x < ESC_CHARS.length; x++) {
			int ofs = results.indexOf(ESC_CHARS[x]);
			while (ofs != -1) {
				StringBuilder buf = new StringBuilder(results.substring(0, ofs));
				buf.append(CHARS[x]);
				buf.append(results.substring(ofs + 1));
				results = buf.toString();
				ofs = results.indexOf(ESC_CHARS[x]);
			}
		}
		
		return results;
	}
}