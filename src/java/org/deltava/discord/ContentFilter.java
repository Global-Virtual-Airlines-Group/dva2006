// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.discord;

import java.util.*;

/**
 * A Content filter for Discord messages.
 * @author Luke
 * @author Danielw
 * @version 11.1
 * @since 11.0
 */

class ContentFilter {
	
	private final Collection<String> _keywords = new LinkedHashSet<String>();
	private final Collection<String> _safewords = new LinkedHashSet<String>();

	/**
	 * Initializes the content filter.
	 * @param kw a Collection of keywords to trigger on
	 * @param sw a Collection of safe keywords to ignore
	 */
	public void init(Collection<String> kw, Collection<String> sw) {
		kw.stream().map(String::toLowerCase).forEach(_keywords::add);
		sw.stream().map(String::toLowerCase).forEach(_safewords::add);
	}
	
	/**
	 * Adds a word to the content filter.
	 * @param keyword the keyword
	 * @param isSafe TRUE if an accepted word, otherwise FALSE
	 * @return TRUE if the word was not present and added, otherwise FALSE
	 */
	public boolean add(String keyword, boolean isSafe) {
		Collection<String> list = isSafe ? _safewords : _keywords;
		return list.add(keyword.toLowerCase());
	}
	
	/**
	 * Removes a word from the content filter.
	 * @param keyword the keyword
	 * @param isSafe TRUE if an accepted word, otherwise FALSE
	 * @return TRUE if the word was present and removed, otherwise FALSE
	 */
	public boolean delete(String keyword, boolean isSafe) {
		Collection<String> list = isSafe ? _safewords : _keywords;
		return list.remove(keyword.toLowerCase());
	}
	
	/**
	 * Returns all keywords.
	 * @return a Collection of keywords
	 */
	public Collection<String> getKeywords() {
		return Collections.unmodifiableCollection(_keywords);
	}

	/**
	 * Returns all accepted keywords.
	 * @return a Collection of accepted keywords
	 */
	public Collection<String> getSafewords() {
		return Collections.unmodifiableCollection(_safewords);
	}
	
	/**
	 * Searches a message for prohibited or accepted keywords.
	 * @param msg the message
	 * @return a FilterResults bean
	 */
	public FilterResults search(String msg) {
		FilterResults fr = new FilterResults();
		StringTokenizer tkns = new StringTokenizer(msg);
		while (tkns.hasMoreElements()) {
			String tkn = tkns.nextToken().toLowerCase();
			if (_safewords.contains(tkn)) continue;
			if (_keywords.contains(tkn))
				fr.add(tkn);
		}
		
		return fr;
	}
}