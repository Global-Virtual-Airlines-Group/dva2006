// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.fleet;

import java.util.*;

/**
 * A bean to store information about Manuals.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Manual extends FleetEntry {
	
	private Collection<String> _certs;
   
    /**
     * Creates a new Manual bean.
     * @param fName the file name of the manual
     */
    public Manual(String fName) {
        super(fName);
        _certs = new TreeSet<String>();
    }

    /**
     * Returns this manual's version number. Manuals only have a major version number.
     * @see FleetEntry#getVersion()
     */
    public String getVersion() {
        return String.valueOf(getMajorVersion());
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
     * Sets the manual version number.
     * @param major the version number
     */
    public final void setVersion(int major) {
        super.setVersion(major, 0, 0);
    }

    public final void setVersion(int major, int minor, int subVersion) {
        super.setVersion(major, 0, 0);
    }
}