// Copyright 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.io.*;
import java.util.*;

/**
 * A utility class to mark and filter out profanity within strings.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

@Deprecated
public class ProfanityFilter {
	
	private static final Collection<String> _badWords = new TreeSet<String>(Collections.reverseOrder());
	
	// Singleton
	private ProfanityFilter() {
	}
	
	/**
	 * Initializes the filter with a set of words or phrases.
	 * @param words a Collection of words or phrases
	 */
	public static void init(Collection<String> words) {
		for (Iterator<String> i = words.iterator(); i.hasNext(); )
			_badWords.add(i.next().toUpperCase());
	}
	
	/**
	 * Initializes the filter with words or phrases loaded from a stream, one per line.
	 * @param is the Stream to load
	 * @throws IOException if an I/O error occurs
	 */
	public static void init(InputStream is) throws IOException {
		if (is == null) return;
		LineNumberReader lr = new LineNumberReader(new InputStreamReader(is), 20480);
		
		// Load the content
		Collection<String> words = new ArrayList<String>();
		while (lr.ready())
			words.add(lr.readLine().toUpperCase());

		lr.close();
		_badWords.addAll(words);
	}
	
	/**
	 * Clears the filter list. <i>Used for unit testing</i>
	 */
	protected static void clear() {
		_badWords.clear();
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
	
	/**
	 * Applies the filter to a message to remove inappropriate content.
	 * @param msg the message text
	 * @return the filtered test
	 */
	public static String filter(String msg) {
		if (msg == null)
			return null;
		
		// Create an uppercase version of the string
		String ucMsg = msg.toUpperCase();

		// Iterate through the string
		for (Iterator<String> i = _badWords.iterator(); i.hasNext(); ) {
			String word = i.next();
			if (ucMsg.contains(word))
				msg = msg.replace(word, "****");
		}
		
		return msg;
	}
}