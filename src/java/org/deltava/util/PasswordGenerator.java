package org.deltava.util;

import java.util.Random;

/**
 * A utility class to generate random web site passwords.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public final class PasswordGenerator {

	private static final String PWD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz123456789";
	private static long LAST_SEED = 0;
	
	private PasswordGenerator() {
	}

	/**
	 * Generates a new random password.
	 * @param pwdLength the length of the password
	 * @return the password
	 */
	public static String generate(int pwdLength) {
		StringBuffer buf = new StringBuffer();
		
		// Seed the random number generator
		Random rnd = null;
		synchronized (PasswordGenerator.class) {
			long seed = System.currentTimeMillis();
			if (seed == PasswordGenerator.LAST_SEED)
				PasswordGenerator.LAST_SEED = ++seed;
			
			rnd = new Random(seed);
		}
		
		// Generate the password
		for (int x = 0; x < pwdLength; x++) {
			int charOfs = rnd.nextInt(PasswordGenerator.PWD_CHARS.length());
			buf.append(PasswordGenerator.PWD_CHARS.charAt(charOfs));
		}
		
		// Return the password
		return buf.toString();
	}
}