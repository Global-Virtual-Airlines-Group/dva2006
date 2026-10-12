// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

/**
 * An enumeration of Claude AI models.
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

enum ClaudeModel {
	HAIKU55("claude-haiku-5-5"), SONNET55("claude-sonnet-5-5"), SONNET5("claude-sonnet-5"), OPUS5("claude-opus-5");
	
	private String _id;
	
	ClaudeModel(String id) {
		_id = id;
	}
	
	/**
	 * Returns the model ID.
	 * @return the ID
	 */
	public String getID() {
		return _id;
	}
}