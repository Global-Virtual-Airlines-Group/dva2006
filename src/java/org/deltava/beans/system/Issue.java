// Copyright 2005, 2006, 2007, 2009, 2011, 2012, 2016, 2020 Global Virtual Airlnes Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.*;
import java.time.Instant;

import org.deltava.beans.*;

import org.deltava.util.system.SystemData;

/**
 * A bean for tracking development issues.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class Issue extends DatabaseBean implements AuthoredBean, ViewEntry {
	
	public enum IssueType implements EnumDescription {
		BUG, ENHANCEMENT;
	}
	
	private String _subject;
	private String _description;
	
	private IssuePriority _priority;
	private IssueArea _area;
	private IssueType _type;
	private IssueStatus _status = IssueStatus.OPEN; 
	private IssueSecurity _security = IssueSecurity.PUBLIC;
	
	private Instant _createdOn;
	private Instant _lastCommentOn;
	private int _commentCount;
	private Instant _resolvedOn;
	
	private int _createdBy;
	private int _assignedTo;
	
	private int _majorVersion;
	private int _minorVersion;
	
	private final Collection<AirlineInformation> _airlines = new HashSet<AirlineInformation>();
	private final Collection<IssueComment> _comments = new TreeSet<IssueComment>();
	
	/**
	 * Creates a new Issue.
	 * @param id the database ID
	 * @param subj the issue title
	 * @throws NullPointerException if subj is null
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see Issue#Issue(String)
	 * @see Issue#getSubject()
	 * @see DatabaseBean#validateID(int, int)
	 */
	public Issue(int id, String subj) {
		this(subj);
		setID(id);
	}

	/**
	 * Creates a new Issue that has not been saved to the database.
	 * @param subj the issue title
	 * @throws NullPointerException if subj is null
	 * @see Issue#Issue(int, String)
	 * @see Issue#getSubject()
	 */
	public Issue(String subj) {
	    super();
	    setSubject(subj);
	}

	/**
	 * Returns the issue title.
	 * @return the title
	 * @see Issue#setSubject(String)
	 */
	public String getSubject() {
		return _subject;
	}
	
	/**
	 * Returns the issue description.
	 * @return the description
	 * @see Issue#setDescription(String)
	 */
	public String getDescription() {
		return _description;
	}
	
	/**
	 * Returns all the comments associated with this issue.
	 * @return a Collection of IssueComments
	 * @see Issue#add(IssueComment)
	 */
	public Collection<IssueComment> getComments() {
		return _comments;
	}
	
	/**
	 * Returns the virtual airlines associated with this issue.
	 * @return a Collection of AirlineInformation beans
	 */
	public Collection<AirlineInformation> getAirlines() {
		return _airlines;
	}
	
	/**
	 * Returns the issue priority.
	 * @return the IssuePriority
	 * @see Issue#setPriority(IssuePriority)
	 */
	public IssuePriority getPriority() {
		return _priority;
	}
	
	/**
	 * Returns the issue status.
	 * @return the IssueStatus
	 * @see Issue#setStatus(IssueStatus)
	 */
	public IssueStatus getStatus() {
		return _status;
	}
	
	/**
	 * Returns the issue type.
	 * @return the IssueType
	 * @see Issue#setType(IssueType)
	 */
	public IssueType getType() {
		return _type;
	}
	
	/**
	 * Returns the issue area.
	 * @return the IssueArea
	 * @see Issue#setArea(IssueArea)
	 */
	public IssueArea getArea() {
		return _area;
	}
	
	/**
	 * Returns the Issue security level.
	 * @return the IssueSecurity
	 * @see Issue#setSecurity(IssueSecurity)
	 */
	public IssueSecurity getSecurity() {
		return _security;
	}
	
	/**
	 * Returns the date/time this Issue was created on.
	 * @return the creation date/time
	 * @see Issue#setCreatedOn(Instant)
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the date/time the last comment for this issue was created on.
	 * @return the creation date/time of the last comment
	 * @see Issue#setLastCommentOn(Instant)
	 */
	public Instant getLastCommentOn() {
		return _lastCommentOn;
	}
	
	/**
	 * Returns the date/time this issue was resolved on.
	 * @return the date/time the issue was resolved on, or null
	 * @see Issue#setResolvedOn(Instant)
	 */
	public Instant getResolvedOn() {
		return _resolvedOn;
	}
	
	/**
	 * Returns the number of comments for this Issue.
	 * @return the number of comments
	 * @see Issue#setCommentCount(int)
	 */
	public int getCommentCount() {
		return _comments.isEmpty() ? _commentCount : _comments.size();
	}

	@Override
	public int getAuthorID() {
		return _createdBy;
	}
	
	/**
	 * The database ID of the Person this issue is assigned to.
	 * @return the database ID
	 * @see Issue#setAssignedTo(int)
	 * @see org.deltava.beans.DatabaseBean#getID()
	 */
	public int getAssignedTo() {
		return _assignedTo;
	}
	
	/**
	 * Returns the major version number this Issue applies to.
	 * @return the major version
	 * @see Issue#setMajorVersion(int)
	 * @see Issue#getMinorVersion()
	 */
	public int getMajorVersion() {
		return _majorVersion;
	}
	
	/**
	 * Returns the minor version number this Issue applies to.
	 * @return the minor version
	 * @see Issue#setMinorVersion(int)
	 * @see Issue#getMajorVersion()
	 */
	public int getMinorVersion() {
		return _minorVersion;
	}
	
	/**
	 * Adds an IssueComment to this Issue. The issue ID will be automatically copied to the comment.
	 * @param ic the new comment
	 * @throws NullPointerException if ic is null
	 * @see Issue#getComments()
	 */
	public void add(IssueComment ic) {
		ic.setParentID(getID());
		_comments.add(ic);
	}
	
	/**
	 * Updates the description of this Issue.
	 * @param desc the new description
	 * @see Issue#getDescription()
	 */
	public void setDescription(String desc) {
		_description = desc;
	}
	
	/**
	 * Updates the creation date/time of this Issue.
	 * @param d the new creation date/time
	 * @throws IllegalArgumentException if d is null
	 * @see Issue#getCreatedOn()
	 */
	public void setCreatedOn(Instant d) {
		if (d == null)
			throw new IllegalArgumentException("Creation Date cannot be null");
		
		_createdOn = d;
	}
	
	/**
	 * Updates the last comment date/time for this Issue.
	 * @param d the date/time the last comment was created
	 * @throws IllegalArgumentException if d is null or before getCreatedOn()
	 * @see Issue#getLastCommentOn()
	 */
	public void setLastCommentOn(Instant d) {
		if ((d != null) && d.isBefore(_createdOn))
			throw new IllegalArgumentException("Last Comment date cannot be before " + _createdOn);
		
		_lastCommentOn = d;
	}
	
	/**
	 * Updates the resolution date/time for this Issue.
	 * @param d the date/time the Issue was resolved
	 * @throws IllegalArgumentException if d is null or before getCreatedOn()
	 * @see Issue#getResolvedOn()
	 */
	public void setResolvedOn(Instant d) {
		if ((d != null) && d.isBefore(_createdOn))
			throw new IllegalArgumentException("Resolved On date cannot be before " + _createdOn);
		
		_resolvedOn = d;
	}
	
	/**
	 * Updates the major version for this Issue.
	 * @param v the major version
	 * @see Issue#getMajorVersion()
	 * @see Issue#setMinorVersion(int)
	 */
	public void setMajorVersion(int v) {
		_majorVersion = Math.max(0, v);
	}
	
	/**
	 * Updates the minor version for this Issue.
	 * @param v the minor version
	 * @see Issue#getMinorVersion()
	 * @see Issue#setMajorVersion(int)
	 */
	public void setMinorVersion(int v) {
		_minorVersion = Math.max(0, v);		
	}
	
	/**
	 * Updates the number of comments for this Issue.
	 * @param cnt the number of comments
	 * @throws IllegalArgumentException if comments are already populated
	 * @see Issue#getCommentCount()
	 */
	public void setCommentCount(int cnt) {
		if (!_comments.isEmpty())
			throw new IllegalArgumentException("Comments already populated");
		
		_commentCount = cnt;
	}
	
	/**
	 * Associaates a virtual airline with this Issue.
	 * @param ai an AirlineInformation bean
	 */
	public void addAirline(AirlineInformation ai) {
		_airlines.add(ai);
	}
	
	/**
	 * Updates the virtual airlines associated with this Issue.
	 * @param airlineCodes a Collection of airline codes
	 */
	public void setAirlines(Collection<String> airlineCodes) {
		_airlines.clear();
		airlineCodes.stream().map(ac -> SystemData.getApp(ac)).filter(Objects::nonNull).forEach(this::addAirline);
	}

	@Override
	public void setAuthorID(int id) {
		validateID(_createdBy, id);
		_createdBy = id;
	}
	
	/**
	 * Updates the database ID of this Issue's assignee.
	 * @param id the database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see Issue#getAssignedTo()
	 */
	public void setAssignedTo(int id) {
		validateID(0, id);
		_assignedTo = id;
	}
	
	/**
	 * Updates this Issue's type.
	 * @param pv the IssueType
	 * @see Issue#getType()
	 */
	public void setType(IssueType pv) {
		_type = pv;
	}
	
	/**
	 * Updates this Issue's area.
	 * @param pv the IssueArea
	 * @see Issue#getArea()
	 */
	public void setArea(IssueArea pv) {
		_area = pv;
	}
	
	/**
	 * Updates this Issue's priority.
	 * @param pv the IssuePriority
	 * @see Issue#getPriority()
	 */
	public void setPriority(IssuePriority pv) {
		_priority = pv;
	}
	
	/**
	 * Updates this Issues's security level.
	 * @param pv the IssueSecurity
	 * @see Issue#getSecurity()
	 */
	public void setSecurity(IssueSecurity pv) {
		_security = pv;
	}
	
	/**
	 * Updates this Issue's status.
	 * @param pv the IssueStatus
	 * @see Issue#getStatus()
	 */
	public void setStatus(IssueStatus pv) {
		_status = pv;
	}
	
	/**
	 * Updates this Issue's title.
	 * @param subj the new title
	 * @throws NullPointerException if subj is null
	 * @see Issue#getSubject()
	 */
	public void setSubject(String subj) {
	  _subject = subj.trim();
	}

	@Override
	public String getRowClassName() {
		final String[] ROW_CLASSES = {"opt1", null, "opt2", "warn", "err", "opt3"};
		return ROW_CLASSES[_status.ordinal()];
	}
}