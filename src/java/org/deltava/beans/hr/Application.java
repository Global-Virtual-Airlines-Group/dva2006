// Copyright 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.hr;

import org.deltava.beans.ViewEntry;

/**
 * A bean to store job posting application data.
 * @author Luke
 * @version 7.0
 * @since 3.4
 */

public class Application extends ApplicantData implements ViewEntry {
	
	public static final int NEW = 0;
	public static final int SHORTLIST = 1;
	public static final int APPROVED = 2;
	
	public static final String[] STATUS_NAMES = {"Pending", "Shortlisted", "Approved"};

	private int _authorID;
	private int _status;

	/**
	 * Creates the Application bean.
	 * @param authorID the JobPosting database ID 
	 */
	public Application(int jobID, int authorID) {
		super(jobID);
		setAuthorID(authorID);
	}

	@Override
	public int getAuthorID() {
		return _authorID;
	}

	/**
	 * Returns the Application status.
	 * @return the status code
	 */
	public int getStatus() {
		return _status;
	}
	
	/**
	 * Returns whether the Application has been shortlisted.
	 * @return TRUE if shortlisted, otherwise FALSE
	 */
	public boolean getShortlisted() {
		return (_status == SHORTLIST);
	}
	
	/**
	 * Returns whether the Application has been approved for the position.
	 * @return TRUE if approved, otherwise FALSE
	 */
	public boolean getApproved() {
		return (_status == APPROVED);
	}
	
	/**
	 * Returns the Application status name.
	 * @return the status name
	 */
	public String getStatusName() {
		return STATUS_NAMES[_status];
	}
	
	@Override
	public void setAuthorID(int id) {
		validateID(_authorID, id);
		_authorID = id;
	}

	/**
	 * Updates the application status.
	 * @param status the status code
	 * @throws IllegalArgumentException if an invalid status code is provided
	 */
	public void setStatus(int status) {
		if ((status < 0) || (status >= STATUS_NAMES.length))
			throw new IllegalArgumentException("Invalid Application status - " + status);
		
		_status = status;
	}
	
	@Override
	public String getRowClassName() {
		if (_status == NEW)
			return null;
		
		return (_status == SHORTLIST) ? "opt1" : "opt3";
	}
}