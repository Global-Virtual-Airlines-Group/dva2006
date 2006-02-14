// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;

/**
 * A utility class to mark and filter out profanity within strings.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ProfanityFilter {
	
	private static Collection<String> _badWords = new HashSet<String>();
	
	// Singleton
	private ProfanityFilter() {
	}
	
	/**
	 * Initializes the filter with a set of words or phrases.
	 * @param words a Collection of words or phrases
	 */
	public static synchronized void init(Collection<String> words) {
		for (Iterator<String> i = words.iterator(); i.hasNext(); )
			_badWords.add(i.next().toUpperCase());
	}
	
	/**
	 * Applies the filter to a message to detect inappropriate content.
	 * @param msg the message text
	 * @return TRUE if inappropriate content was detected, otherwise FALSE
	 */
	public static boolean flag(String msg) {
		if (msg == null)
			return false;
		
		// Create an uppercase version of the string
		String ucMsg = msg.toUpperCase();
		
		// Iterate through the string
		for (Iterator<String> i = _badWords.iterator(); i.hasNext(); ) {
			String word = i.next();
			
			//	Stop at the first match
			if (ucMsg.contains(word))
				return true;
		}
		
		return false;
	}
	
	public static String filter(String msg) {
		if (msg == null)
			return null;
		
		// Create an uppercase version of the string
		String ucMsg = msg.toUpperCase();

		// Iterate through the string
		for (Iterator<String> i = _badWords.iterator(); i.hasNext(); ) {
			String word = i.next();
			if (ucMsg.contains(word))
				StringUtils.replace(msg, word, "****");
		}
		
		return msg;
	}
}