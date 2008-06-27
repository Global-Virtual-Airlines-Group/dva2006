// Copyright 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

import java.util.*;

import org.deltava.beans.fleet.Video;

/**
 * A bean to store Flight Academy training video metadata.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class TrainingVideo extends Video {
	
	private Collection<String> _certs = new LinkedHashSet<String>();

	/**
	 * Creates a new Training Video bean.
	 * @param fName the file name
	 */
	public TrainingVideo(String fName) {
		super(fName);
	}
	
	/**
	 * Expands a generic video into a Training Video.
	 * @param v the existing Video bean
	 */
	public TrainingVideo(Video v) {
		super(v.getFileName());
		setName(v.getName());
		setSecurity(v.getSecurity());
		setSize(v.getSize());
		setAuthorID(v.getAuthorID());
		setCategory(v.getCategory());
		setDescription(v.getDescription());
		setDownloadCount(v.getDownloadCount());
	}

	/**
	 * Returns the Flight Academy certifications associated with this video.
	 * @return a Collection of Certification names
	 * @see TrainingVideo#addCertification(String)
	 * @see TrainingVideo#setCertifications(Collection)
	 */
	public Collection<String> getCertifications() {
		return _certs;
	}
	
	/**
	 * Adds a Flight Academy certification to this Training Video.
	 * @param certName the Certification name
	 * @see TrainingVideo#setCertifications(Collection)
	 * @see TrainingVideo#getCertifications()
	 */
	public void addCertification(String certName) {
		_certs.add(certName);
	}
	
	/**
	 * Resets and updates the list of Flight Academy certifications associated with this video.
	 * @param certNames a Collection of Certification names
	 * @see TrainingVideo#addCertification(String)
	 * @see TrainingVideo#getCertifications()
	 */
	public void setCertifications(Collection<String> certNames) {
		_certs.clear();
		if (certNames != null)
			_certs.addAll(certNames);
	}
}