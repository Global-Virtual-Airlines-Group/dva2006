// Copyright 2006, 2007, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to track Flight Academy Certification requirements. Each Certification has a number of
 * requirements that need to be completed before the Course is done.
 * @author Luke
 * @version 3.4
 * @since 1.0
 */

public class CertificationRequirement extends DatabaseBean {

	private String _examName;
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
	 * Returns the examination associated with this certification requirement.
	 * @return the examination name, or null if none
	 */
	public String getExamName() {
		return _examName;
	}
	
	/**
	 * Returns the Requirement text.
	 * @return the text
	 */
	public String getText() {
		return _desc;
	}
	
	/**
	 * Sets the examination associated with this certification requirement.
	 * @param examName the examination name
	 */
	public void setExamName(String examName) {
		_examName = examName;
	}
	
	/**
	 * Updates the requirement text.
	 * @param msg the text
	 */
	public void setText(String msg) {
		_desc = msg;
	}
}