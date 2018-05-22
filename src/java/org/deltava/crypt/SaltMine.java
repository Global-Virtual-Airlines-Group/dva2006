// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.crypt;

import java.util.Random;

/**
 * A utility class to generate hash salts.
 * @author Luke
 * @version 8.3
 * @since 8.3
 */

public class SaltMine {

	private static final String SALT_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz./";
	
	private static final Random _r = new Random();

	// static class
	private SaltMine() {
		super();
	}

	/**
	 * Generates a random hash salt.
	 * @param length the length of the salt
	 * @return a salt
	 */
	public static String generate(int length) {
		StringBuilder buf = new StringBuilder();
		for (int x = 0; x < length; x++) {
			int ofs = _r.nextInt(SALT_CHARS.length());
			buf.append(SALT_CHARS.charAt(ofs));
		}
		
		return buf.toString();
	}
}