// Copyright 2006, 2009, 2016, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fleet;

import java.net.*;
import java.time.Instant;
import java.util.Collection;
import java.util.TreeSet;

import org.deltava.beans.*;

/**
 * A bean to store Web Resource link data.
 * @author Luke
 * @version 10.4
 * @since 1.0
 */

public class Resource extends DatabaseBean implements ViewEntry, AuthoredBean, Auditable, ExternalURL {
	
	private String _url;
	private String _title;
	private String _domain;
	private String _category;
	private int _authorID;
	private int _lastUpdateID;
	
	private final Collection<String> _certs = new TreeSet<String>();
	private boolean _ignoreCerts;
	
	private Instant _created;
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

	@Override
	public String getURL() {
		return _url;
	}

	@Override
	public String getTitle() {
		return _title;
	}
	
	/**
	 * Retruns the Resource URL domain.
	 * @return the domain
	 * @see Resource#getURL()
	 */
	public String getDomain() {
		return _domain;
	}
	
	/**
	 * Returns the Resource category.
	 * @return the category
	 * @see Resource#setCategory(String)
	 */
	public String getCategory() {
		return _category;
	}
	
	/**
	 * Returns the Author of this Resource.
	 * @return the Author's database ID
	 * @see Resource#setAuthorID(int)
	 */
	@Override
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
	 * @see Resource#setCreatedOn(Instant)
	 */
	public Instant getCreatedOn() {
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
	 * Returns whether this Resource is available to the public.
	 * @return TRUE if all users can see this Resource, otherwise FALSE
	 * @see Resource#setPublic(boolean)
	 */
	public boolean getPublic() {
		return _public;
	}
	
    /**
     * Returns whether this Resource should be shown to users not enrolled in the Flight Academy Certifications.
     * @return TRUE if visible to all, otherwise FALSE
     * @see Resource#setIgnoreCertifcations(boolean)
     */
    public boolean getIgnoreCertifications() {
    	return _ignoreCerts;
    }
	
    /**
     * Returns all Certifications linked to this Resource.
     * @return a Collection of Certification names
     * @see Resource#addCertification(String)
     * @see Resource#addCertifications(Collection)
     */
    public Collection<String> getCertifications() {
    	return _certs;
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
			_url = urlData.toExternalForm();
			String host = urlData.getHost();
			if (host.startsWith("www."))
				host = host.substring(4);
			
			_domain = host; 
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * Updates the title of the Resource.
	 * @param title the title
	 * @see Resource#getTitle()
	 */
	public void setTitle(String title) {
		_title = title;
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
	 * Updates the category of this Resource.
	 * @param cat the category
	 * @see Resource#getCategory()
	 */
	public void setCategory(String cat) {
		_category = cat;
	}
	
    /**
     * Adds a Flight Academy Certification to this Resource.
     * @param cert the Certification name
     * @see Resource#addCertifications(Collection)
     * @see Resource#getCertifications()
     */
    public void addCertification(String cert) {
    	_certs.add(cert);
    }
    
    /**
     * Clears the list of Flight Academy Certifications and replaces it with a new list.
     * @param certs a Collection of Certification names
     * @see Resource#addCertification(String)
     * @see Resource#getCertifications()
     */
    public void addCertifications(Collection<String> certs) {
    	if (certs != null) {
    		_certs.clear();
    		_certs.addAll(certs);
    	}
    }
	
	@Override
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
		_lastUpdateID = id;
	}
	
	/**
	 * Updates the creation date of this Resource.
	 * @param dt the date/time the resource was created
	 * @see Resource#getCreatedOn()
	 */
	public void setCreatedOn(Instant dt) {
		_created = dt;
	}
	
	/**
	 * Updates the number of times this Resource has been visited.
	 * @param hits the number of hits
	 * @see Resource#getHits()
	 */
	public void setHits(int hits) {
		_hitCount = hits;
	}
	
	/**
	 * Updates whether this Resource is available to the public.
	 * @param isPublic TRUE if all users can see this Resource, otherwise FALSE
	 * @see Resource#getPublic()
	 */
	public void setPublic(boolean isPublic) {
		_public = isPublic;
	}
	
    /**
     * Marks this Resource as visible to users not enrolled in the specified Flight Academy Certifications.
     * @param ignoreCerts TRUE if visible to all users, otherwise FALSE
     * @see Resource#getIgnoreCertifications()
     */
    public void setIgnoreCertifcations(boolean ignoreCerts) {
    	_ignoreCerts = ignoreCerts;
    }

	@Override
	public String getAuditID() {
		return Integer.toHexString(hashCode());
	}
	
	@Override
	public String toString() {
		return _url;
	}
	
	@Override
	public int hashCode() {
		return _url.hashCode();
	}
	
	@Override
	public String getRowClassName() {
		return _public ? null : "opt2";
	}
}