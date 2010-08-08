// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.help;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store Help Desk response templates.
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class ResponseTemplate implements Cacheable, Comparable<ResponseTemplate> {

	private String _title;
	private String _body;

	/**
	 * Returns the template title.
	 * @return the title
	 */
	public String getTitle() {
		return _title;
	}

	/**
	 * Returns the response body.
	 * @return the body
	 */
	public String getBody() {
		return _body;
	}
	
	/**
	 * Updates the template title.
	 * @param title the title
	 * @throws NullPointerException if title is null
	 */
	public void setTitle(String title) {
		_title = title.trim();
	}
	
	/**
	 * Updates the response body.
	 * @param body the body
	 */
	public void setBody(String body) {
		_body = body;
	}
	
	public Object cacheKey() {
		return _title;
	}
	
	public String toString() {
		return _title;
	}
	
	public int hashCode() {
		return _title.hashCode();
	}

	/**
	 * Compares two templates by comparing their titles.
	 */
	public int compareTo(ResponseTemplate rt2) {
		return _title.compareTo(rt2._title);
	}
}