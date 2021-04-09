// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.net.URL;
import java.time.Instant;

/**
 * A bean to store push notification endpoint data. 
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class PushEndpoint extends DatabaseBean {
	
	private final String _url;
	private Instant _createdOn;
	private String _auth;
	private String _pub256dh;

	/**
	 * Creates the endpoint.
	 * @param id the user's database ID
	 * @param url the endpoint URL 
	 */
	public PushEndpoint(int id, String url) {
		super();
		setID(id);
		_url = url;
	}

	/**
	 * Returns the endpoint URL.
	 * @return the URL
	 */
	public String getURL() {
		return _url;
	}
	
	public String getHost() {
		try {
			URL url = new URL(_url);
			return url.getHost();
		} catch (Exception e) {
			return "INVALID";
		}
	}
	
	/**
	 * Returns the creation time of this endpoint.
	 * @return the creation date/time
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the endpoint auth secret.
	 * @return the secret
	 */
	public String getAuth() {
		return _auth;
	}
	
	/**
	 * Returns the endpoint public key.
	 * @return the key
	 */
	public String getPub256DH() {
		return _pub256dh;
	}
	
	/**
	 * Updates the endpoint auth secret.
	 * @param auth the secret
	 */
	public void setAuth(String auth) {
		_auth = auth;
	}

	/**
	 * Updates the endpoint public key.
	 * @param pubDH the key
	 */
	public void setPub256DH(String pubDH) {
		_pub256dh = pubDH;
	}
	
	/**
	 * Updates the creation date of this endpoint.
	 * @param dt the date/time
	 */
	public void setCreatedOn(Instant dt) {
		_createdOn = dt;
	}
	
	@Override
	public int hashCode() {
		return _url.hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(String.valueOf(getID()));
		buf.append('/').append(getHost());
		return buf.toString();
	}
	
	public boolean equals(PushEndpoint pe) {
		return (getID() == pe.getID()) && _url.equals(pe._url);
	}
}