// Copyright 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fb;

/**
 * A bean to store Facebook authentication tokens across airlines. 
 * @author Luke
 * @version 3.7
 * @since 3.4
 */

public class FacebookCredentials implements java.io.Serializable {
	
	private String _appID;
	private String _pageID;
	private String _pageToken;

	/**
	 * Creates a new credentials object.
	 * @param appID the application ID
	 */
	public FacebookCredentials(String appID) {
		super();
		_appID = appID;
	}

	/**
	 * Returns the Facebook application ID.
	 * @return the application ID
	 */
	public String getID() {
		return _appID;
	}
	
	/**
	 * Returns the Facebook page ID.
	 * @return the page ID
	 */
	public String getPageID() {
		return _pageID;
	}
	
	/**
	 * Returns the Facebook application page access token.
	 * @return the token
	 */
	public String getPageToken() {
		return _pageToken;
	}
	
	/**
	 * Updates the Facebook page ID for this application.
	 * @param id the page ID
	 */
	public void setPageID(String id) {
		_pageID = id;
	}

	/**
	 * Updates the Facebook page token for this application.
	 * @param token the page token
	 */
	public void setPageToken(String token) {
		_pageToken = token;
	}
	
	public int hashCode() {
		return _appID.hashCode();
	}
	
	public String toString() {
		return "FB-" + _appID;
	}
}