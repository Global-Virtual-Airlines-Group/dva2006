// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.discord;

import java.util.*;

/**
 * A bean to store Discord content filter results. 
 * @author Luke
 * @version 11.0
 * @since 11.0
 */

class FilterResults {
	
	private final Collection<String> _flagged = new LinkedHashSet<String>();

	/**
	 * Returns if the filter results detected no key words.
	 * @return TRUE if no flagged key words were detected, otherwise FALSE
	 */
	public boolean isOK() {
		return _flagged.isEmpty();
	}
	
	/**
	 * Adds a flagged key word.
	 * @param flagged the key word
	 */
	public void add(String flagged) {
		_flagged.add(flagged);
	}

	/**
	 * Returns the flagged key words.
	 * @return a Collection of flagged key words
	 */
	public Collection<String> getFlaggedResults() {
		return Collections.unmodifiableCollection(_flagged);
	}
}