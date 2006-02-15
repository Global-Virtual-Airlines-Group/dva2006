// Copyright (c) 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to track Flight Academy Certification requirements. Each Certification has a number of
 * requirements that need to be completed before the Course is done.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CertificationRequirement extends DatabaseBean implements Comparable {

	private String _desc;
	
	/**
	 * Creates a Requirement bean.
	 * @param sequenceID the order number
	 * @throws IllegalArgumentException if sequenceID is zero or negative 
	 */
	public CertificationRequirement(int sequenceID) {
		super();
		setID(sequenceID);
	}
	
	/**
	 * Returns the Requirement text.
	 * @return the text
	 */
	public String getText() {
		return _desc;
	}
	
	/**
	 * Updates the requirement text.
	 * @param msg the text
	 */
	public void setText(String msg) {
		_desc = msg;
	}

	/**
	 * Compares Requirements by comparing their sequence numbers.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		CertificationRequirement cp2 = (CertificationRequirement) o;
		return new Integer(getID()).compareTo(new Integer(cp2.getID()));
	}
}