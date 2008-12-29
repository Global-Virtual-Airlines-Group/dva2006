// Copyright 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.cooler;

import java.net.*;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to store Water Cooler Linked Images and their descriptions.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class LinkedImage extends DatabaseBean {
	
	private int _threadID;
	private String _url;
	private String _desc;

	/**
	 * Creates the Linked Image bean.
	 * @param id the sort order
	 * @param url the URL to link to
	 * @throws IllegalArgumentException if id is zero or negative, or the URL is invalid
	 */
	public LinkedImage(int id, String url) {
		super();
		setID(id);
		setURL(url);
	}

	/**
	 * Returns the Image URL.
	 * @return the URL
	 * @see LinkedImage#setURL(String)
	 */
	public String getURL() {
		return _url;
	}
	
	/**
	 * Returns the Image description.
	 * @return the description
	 * @see LinkedImage#setDescription(String)
	 */
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Returns the thread ID.
	 * @return the thread database ID
	 * @see LinkedImage#setThreadID(int)
	 */
	public int getThreadID() {
		return _threadID;
	}
	
	/**
	 * Updates the linked Image URL.
	 * @param url the URL
	 * @throws IllegalArgumentException if the URL is malformed or invalid
	 * @see LinkedImage#getURL()
	 */
	public void setURL(String url) {
		try {
			URL u = new URL(url);
			_url = u.toString();
		} catch (MalformedURLException mue) {
			throw new IllegalArgumentException("Invalid URL - " + mue);
		}
	}
	
	/**
	 * Updates the Message Thread ID. 
	 * @param id the thread database ID
	 * @see LinkedImage#getThreadID()
	 */
	public void setThreadID(int id) {
		validateID(_threadID, id);
		_threadID = id;
	}
	
	/**
	 * Updates the Image Description.
	 * @param desc the description
	 * @see LinkedImage#getDescription()
	 */
	public void setDescription(String desc) {
		_desc = desc;
	}
	
	/**
	 * Returns the url's hash code.
	 */
	public int hashCode() {
		return _url.hashCode();
	}
	
	/**
	 * Tests for equality by comparing the URLs.
	 */
	public boolean equals(Object o) {
		return (o instanceof LinkedImage) ? _url.equals(o.toString()) : false;
	}
	
	/**
	 * Returns the URL.
	 */
	public String toString() {
		return _url;
	}
}