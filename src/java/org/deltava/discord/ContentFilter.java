// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.discord;

import java.util.*;

/**
 * 
 * @author Luke
 * @author danielw
 * @version 11.0
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
	
	public boolean add(String keyword, boolean isSafe) {
		Collection<String> list = isSafe ? _safewords : _keywords;
		return list.add(keyword);
	}
	
	public boolean delete(String keyword, boolean isSafe) {
		Collection<String> list = isSafe ? _safewords : _keywords;
		return list.remove(keyword);
	}
	
	public Collection<String> getKeywords() {
		return Collections.unmodifiableCollection(_keywords);
	}

	public Collection<String> getSafewords() {
		return Collections.unmodifiableCollection(_safewords);
	}
	
	/**
	 * 
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