// Copyright 2010, 2016, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.hr;

import org.deltava.beans.ViewEntry;

/**
 * A bean to store job posting application data.
 * @author Luke
 * @version 10.2
 * @since 3.4
 */

public class Application extends ApplicantData implements ViewEntry {
	
	private int _authorID;
	private ApplicantStatus _status = ApplicantStatus.PENDING;

	/**
	 * Creates the Application bean.
	 * @param jobID the Job database ID
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
	 * @return the ApplicantStatus
	 */
	public ApplicantStatus getStatus() {
		return _status;
	}
	
	/**
	 * Returns whether the Application has been shortlisted.
	 * @return TRUE if shortlisted, otherwise FALSE
	 */
	public boolean getShortlisted() {
		return (_status == ApplicantStatus.SHORTLIST);
	}
	
	/**
	 * Returns whether the Application has been approved for the position.
	 * @return TRUE if approved, otherwise FALSE
	 */
	public boolean getApproved() {
		return (_status == ApplicantStatus.APPROVED);
	}
	
	@Override
	public void setAuthorID(int id) {
		validateID(_authorID, id);
		_authorID = id;
	}

	/**
	 * Updates the application status.
	 * @param status the ApplicantStatus
	 */
	public void setStatus(ApplicantStatus status) {
		_status = status;
	}
	
	@Override
	public String getRowClassName() {
		if (_status == ApplicantStatus.PENDING) return null;
		return (_status == ApplicantStatus.SHORTLIST) ? "opt1" : "opt3";
	}
}