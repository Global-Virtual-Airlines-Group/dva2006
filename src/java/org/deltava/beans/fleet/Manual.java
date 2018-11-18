// Copyright 2005, 2006, 2007, 2010, 2015, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fleet;

import java.io.File;
import java.util.*;

/**
 * A bean to store information about Manuals.
 * @author Luke
 * @version 8.4
 * @since 1.0
 */

public class Manual extends FleetEntry {
	
	private final Collection<String> _certs = new TreeSet<String>();
	
	private boolean _ignoreCerts;
	private boolean _showOnRegister;
   
    /**
     * Creates a new Manual bean.
     * @param f the File
     */
    public Manual(File f) {
        super(f);
    }

    /**
     * Returns this manual's version number. Manuals only have a major version number.
     * @see FleetEntry#getVersion()
     */
    @Override
    public String getVersion() {
        return String.valueOf(getMajorVersion());
    }
    
    /**
     * Returns whether the Manual should be shown on the Registration page.
     * @return TRUE if the Manual should be shown, otherwise FALSE
     */
    public boolean getShowOnRegister() {
    	return _showOnRegister;
    }
    
    /**
     * Returns whether this Manual should be shown to users not enrolled in the Certifications.
     * @return TRUE if visible to all, otherwise FALSE
     * @see Manual#setIgnoreCertifcations(boolean)
     */
    public boolean getIgnoreCertifications() {
    	return _ignoreCerts;
    }
    
    /**
     * Returns all Certifications linked to this Manual.
     * @return a Collection of Certification names
     * @see Manual#addCertification(String)
     * @see Manual#addCertifications(Collection)
     */
    public Collection<String> getCertifications() {
    	return _certs;
    }
    
    /**
     * Returns the document type.
     * @return a DocumentType
     */
    public DocumentType getType() {
    	return DocumentType.fromFilename(getFileName());
    }
    
    /**
     * Adds a Flight Academy Certification to this Manual.
     * @param cert the Certification name
     * @see Manual#addCertifications(Collection)
     * @see Manual#getCertifications()
     */
    public void addCertification(String cert) {
    	_certs.add(cert);
    }
    
    /**
     * Clears the list of Flight Academy Certifications and replaces it with a new list.
     * @param certs a Collection of Certification names
     * @see Manual#addCertification(String)
     * @see Manual#getCertifications()
     */
    public void addCertifications(Collection<String> certs) {
    	if (certs != null) {
    		_certs.clear();
    		_certs.addAll(certs);
    	}
    }
    
    /**
     * Marks this Manual as visible on the Registration page.
     * @param show TRUE if the manual should be displayed, otherwise FALSE
     * @see Manual#getShowOnRegister()
     */
    public void setShowOnRegister(boolean show) {
    	_showOnRegister = show;
    }
    
    /**
     * Marks this Manual as visible to users not enrolled in the specified Courses.
     * @param ignoreCerts TRUE if visible to all users, otherwise FALSE
     * @see Manual#getIgnoreCertifications()
     */
    public void setIgnoreCertifcations(boolean ignoreCerts) {
    	_ignoreCerts = ignoreCerts;
    }
    
    /**
     * Sets the manual version number.
     * @param major the version number
     */
    public final void setVersion(int major) {
        super.setVersion(major, 0, 0);
    }

    @Override
    public final void setVersion(int major, int minor, int subVersion) {
        super.setVersion(major, 0, 0);
    }
}