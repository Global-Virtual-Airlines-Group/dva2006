// Copyright 2010, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.hr;

import java.util.*;
import java.time.Instant;

import org.deltava.beans.*;

import org.deltava.util.StringUtils;

/**
 * A bean to store information about a job posting.
 * @author Luke
 * @version 7.0
 * @since 3.4
 */

public class JobPosting extends DatabaseBean implements ViewEntry {
	
	private String _title;
	private int _status;
	
	private int _hireManagerID;
	
	private Instant _createdOn;
	private Instant _closeDate;
	
	private int _minLegs;
	private int _minAge;
	private boolean _staffOnly;
	
	private String _summary;
	private String _desc;
	
	public static final int OPEN = 0;
	public static final int CLOSED = 1;
	public static final int SHORTLIST = 2;
	public static final int SELECTED = 3;
	public static final int COMPLETE = 4;
	
	public static final String[] STATUS_NAMES = {"Open", "Closed", "Shortlisted", "Selected", "Complete"};
	private static final String[] ROW_CLASSES = {null, "opt2", "opt3", "opt4", "opt1"};
	
	private final Collection<Comment> _comments = new ArrayList<Comment>();
	private final Collection<Application> _apps = new ArrayList<Application>();
	private int _appCount;

	/**
	 * Creates a new job posting.
	 * @param title the job title
	 */
	public JobPosting(String title) {
		super();
		setTitle(title);
	}
	
	/**
	 * Returns the job title.
	 * @return the title
	 */
	public String getTitle() {
		return _title;
	}
	
	/**
	 * Returns the job summary, a brief description of the position.
	 * @return the summary
	 */
	public String getSummary() {
		return _summary;
	}
	
	/**
	 * Returns the job posting status.
	 * @return the status code
	 */
	public int getStatus() {
		return _status;
	}
	
	/**
	 * Returns the job posting status name for display in a JSP.
	 * @return the status name 
	 */
	public String getStatusName() {
		return STATUS_NAMES[_status];
	}
	
	/**
	 * Returns the date the job posting was created on.
	 * @return the creation date/time
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the date the job posting is closed to applications.
	 * @return the closing date/time
	 */
	public Instant getClosesOn() {
		return _closeDate;
	}
	
	/**
	 * Returns the minimum number of legs required to apply for this position.
	 * @return the number of legs
	 */
	public int getMinLegs() {
		return _minLegs;
	}
	
	/**
	 * Returns the minimum number of days since joining required to apply for this position.
	 * @return the number of days since joining
	 */
	public int getMinAge() {
		return _minAge;
	}
	
	/**
	 * Returns the database ID of the hiring manager. 
	 * @return the datbase ID
	 */
	public int getHireManagerID() {
		return _hireManagerID;
	}
	
	/**
	 * Returns whether this job posting is only visible to existing staff members.
	 * @return TRUE if only visible to the staff, otherwise FALSE
	 */
	public boolean getStaffOnly() {
		return _staffOnly;
	}
	
	/**
	 * Returns the posting description.
	 * @return the description
	 */
	public String getDescription() {
		return _desc;
	}
	
	/**
	 * Returns the applications for this job posting.
	 * @return a Collection of Application beans
	 */
	public Collection<Application> getApplications() {
		return new ArrayList<Application>(_apps);
	}
	
	/**
	 * Returns the comments for this job posting.
	 * @return a Collection of Comment beans
	 */
	public Collection<Comment> getComments() {
		return _comments;
	}
	
	/**
	 * Returns the selected Applications, if any.
	 * @return a Collection of Application beans
	 */
	public Collection<Application> getSelectedApplications() {
		Collection<Application> apps = new ArrayList<Application>();
		for (Application a : _apps) {
			if (a.getApproved())
				apps.add(a);
		}
		
		return apps;
	}
	
	/**
	 * Returns the number of Applications for this posting.
	 * @return the number of applications
	 */
	public int getAppCount() {
		return _appCount;
	}

	/**
	 * Adds an application to this Job posting.
	 * @param a an Application bean
	 */
	public void add(Application a) {
		_apps.add(a);
		_appCount = _apps.size();
	}
	
	/**
	 * Adds a comment to this Job posting.
	 * @param c a Comment bean
	 */
	public void add(Comment c) {
		_comments.add(c);
	}

	/**
	 * Sets the date this posting was created.
	 * @param dt the creation date/time
	 */
	public void setCreatedOn(Instant dt) {
		_createdOn = dt;
	}
	
	/**
	 * Sets the date this posting closes.
	 * @param dt the closing date/time
	 */
	public void setClosesOn(Instant dt) {
		if ((dt != null) && dt.isBefore(_createdOn))
			throw new IllegalArgumentException("Close date before creation date");
		
		_closeDate = dt;
	}
	
	/**
	 * Updates the posting status.
	 * @param status the status code
	 */
	public void setStatus(int status) {
		if ((status < 0) || (status > STATUS_NAMES.length))
			throw new IllegalArgumentException("Invalid Status - " + status);
		
		_status = status;
	}
	
	/**
	 * Updates the posting status.
	 * @param status the status name
	 */
	public void setStatus(String status) {
		setStatus(StringUtils.arrayIndexOf(STATUS_NAMES, status));
	}

	/**
	 * Updates the minimum number of legs required to apply for this position.
	 * @param legs the minimum number of legs
	 */
	public void setMinLegs(int legs) {
		_minLegs = Math.max(0, legs);
	}

	/**
	 * Updates the minimum number of days since joining required to apply for this position.
	 * @param age the minimum number of days since joining
	 */
	public void setMinAge(int age) {
		_minAge = Math.max(0, age);
	}
	
	/**
	 * Marks this job posting as only visible to staff members.
	 * @param staffOnly TRUE if visible to staff members only, otherwise FALSE
	 */
	public void setStaffOnly(boolean staffOnly) {
		_staffOnly = staffOnly;
	}
	
	/**
	 * Sets the number of applications to this posting.
	 * @param cnt the number of applications
	 * @throws IllegalStateException if applications have already been added
	 */
	public void setAppCount(int cnt) {
		if (!_apps.isEmpty())
			throw new IllegalStateException("Applications already populated");
		
		_appCount = Math.max(0, cnt);
	}
	
	/**
	 * Sets the database ID of the hiring manager.
	 * @param id the database ID of the Hiring manager
	 */
	public void setHireManagerID(int id) {
		validateID(0, id);
		_hireManagerID = id;
	}
	
	/**
	 * Updates the job title.
	 * @param title the title
	 * @throws NullPointerException if title is null
	 */
	public void setTitle(String title) {
		_title = title.trim();
	}
	
	/**
	 * Updates the job summary.
	 * @param summary the summary
	 * @throws NullPointerException if summary is null
	 */
	public void setSummary(String summary) {
		_summary = summary.trim();
	}
	
	/**
	 * Updates the posting description.
	 * @param desc the description
	 */
	public void setDescription(String desc) {
		_desc = desc;
	}

	@Override
	public String getRowClassName() {
		if ((_status == OPEN) && _staffOnly) 
			return "warn";
		
		return ROW_CLASSES[_status];
	}
}