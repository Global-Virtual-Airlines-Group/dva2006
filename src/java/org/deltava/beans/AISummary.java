// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * A bean to store AI summaries. 
 * @author Luke
 * @version 13.0
 * @since 12.5
 */

public class AISummary extends AIResponse {
	
	private final String _text;

	/**
	 * Creates the bean.
	 * @param id the database ID
	 * @param text the summary text
	 */
	public AISummary(int id, String text) {
		super(id);
		_text = text;
	}

	/**
	 * Returns the summary text.
	 * @return the text
	 */
	public String getText() {
		return _text;
	}
}