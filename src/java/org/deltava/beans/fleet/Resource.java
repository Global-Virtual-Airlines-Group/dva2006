// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fleet;

import java.net.*;
import java.util.Date;

import org.deltava.beans.*;

/**
 * A bean to store Web resource data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Resource extends DatabaseBean implements ViewEntry, AuthoredBean {
	
	private String _url;
	private int _authorID;
	private int _lastUpdateID;
	
	private Date _created;
	private int _hitCount;
	private String _desc;
	private boolean _public;
	
	/**
	 * Creates a new Web Resource bean.
	 * @param url the URL of the resource
	 * @throws IllegalArgumentException if the URL is invalid
	 * @see Resource#setURL(String)
	 */
	public Resource(String url) {
		super();
		setURL(url);
	}

	/**
	 * Returns the Resource URL.
	 * @return the URL
	 * @see Resource#setURL(String)
	 */
	public String getURL() {
		return _url;
	}
	
	/**
	 * Returns the Author of this Resource.
	 * @return the Author's database ID
	 * @see Resource#setAuthorID(int)
	 */
	public int getAuthorID() {
		return _authorID;
	}
	
	/**
	 * Returns the last updater of this Resource.
	 * @return the Updater's database ID
	 * @see Resource#setLastUpdateID(int)
	 * @see Resource#getAuthorID()
	 */
	public int getLastUpdateID() {
		return _lastUpdateID;
	}
	
	/**
	 * Returns the creation date of this Resource.
	 * @return the date/time the Resource was created
	 * @see Resource#setCreatedOn(Date)
	 */
	public Date getCreatedOn() {
		return _created;
	}
	
	/**
	 * Returns the number of times this Resource has been visited.
	 * @return the number of hits
	 * @see Resource#setHits(int)
	 */
	public int getHits() {
		return _hitCount;
	}
	
	/**
	 * Returns the description of this Resource.
	 * @return the description
	 * @see Resource#setDescription(String)
	 */
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Returns wether this Resource is available to the public.
	 * @return TRUE if all users can see this Resource, otherwise FALSE
	 * @see Resource#setPublic(boolean)
	 */
	public boolean getPublic() {
		return _public;
	}
	
	/**
	 * Updates the URL of this Resource.
	 * @param url the URL
	 * @throws IllegalArgumentException if the URL is invalid
	 * @see Resource#getURL()
	 */
	public void setURL(String url) {
		try {
			URL urlData = new URL(url); 
			_url = urlData.toString();
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}
	
	/**
	 * Updates the description of this Resource.
	 * @param desc the description
	 * @see Resource#getDescription()
	 */
	public void setDescription(String desc) {
		_desc = desc;
	}
	
	/**
	 * Updates the Author of this Resource.
	 * @param id the Author's database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see Resource#getAuthorID()
	 * @see Resource#setLastUpdateID(int)
	 */
	public void setAuthorID(int id) {
		validateID(_authorID, id);
		_authorID = id;
	}

	/**
	 * Updates the last modifier of this Resource.
	 * @param id the Updater's database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see Resource#getLastUpdateID()
	 * @see Resource#setAuthorID(int)
	 */
	public void setLastUpdateID(int id) {
		validateID(_lastUpdateID, id);
		_lastUpdateID = id;
	}
	
	/**
	 * Updates the creation date of this Resource.
	 * @param dt the date/time the resource was created
	 * @see Resource#getCreatedOn()
	 */
	public void setCreatedOn(Date dt) {
		_created = dt;
	}
	
	/**
	 * Updates the number of times this Resource has been visited.
	 * @param hits the number of hits
	 * @see Resource#getHits()
	 */
	public void setHits(int hits) {
		if (hits < 0)
			throw new IllegalArgumentException("Invalid Hit Count - " + hits);
		
		_hitCount = hits;
	}
	
	/**
	 * Updates wether this Resource is available to the public.
	 * @param isPublic TRUE if all users can see this Resource, otherwise FALSE
	 * @see Resource#getPublic()
	 */
	public void setPublic(boolean isPublic) {
		_public = isPublic;
	}
	
	/**
	 * Returns the Resource URL.
	 */
	public String toString() {
		return _url;
	}
	
	/**
	 * Returns the row CSS class name if displayed in a view table.
	 * @return the CSS class name
	 */
	public String getRowClassName() {
		return _public ? null : "opt2";
	}
}