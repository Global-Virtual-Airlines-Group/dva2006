// Copyright 2005, 2006 Global Virtual Airlnes Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.*;

import org.deltava.beans.*;

/**
 * A bean for tracking Web Site/Fleet issues.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Issue extends DatabaseBean implements AuthoredBean, ViewEntry {
	
	public static final int PRIORITY_LOW = 0;
	public static final int PRIORITY_MEDIUM = 1;
	public static final int PRIORITY_HIGH = 2;
	public static final int PRIORITY_CRITICAL = 3;
	
	/**
	 * Priority Names
	 */
	public static final String[] PRIORITY = {"Low", "Medium", "High", "Critical"};
	
	public static final int AREA_WEBSITE = 0;
	public static final int AREA_FLEET = 1;
	public static final int AREA_MANUAL = 2;
	public static final int AREA_EXAMS = 3;
	public static final int AREA_ACARS = 4;
	public static final int AREA_SERVER = 5;
	public static final int AREA_SCHEDULE = 6;
	
	/**
	 * Area Names
	 */
	public static final String[] AREA = {"Web Site", "Fleet Library", "Manuals", "Examinations", "ACARS", "Server Admin", "Schedule"};
	
	public static final int STATUS_OPEN = 0;
	public static final int STATUS_FIXED = 1;
	public static final int STATUS_WORKAROUND = 2;
	public static final int STATUS_WONTFIX = 3;
	public static final int STATUS_DEFERRED = 4;
	public static final int STATUS_DUPE = 5;
	
	/**
	 * Status Names
	 */
	public static final String[] STATUS = {"Open", "Fixed", "Worked Around", "Won't Fix", "Deferred", "Duplicate"};
	
	public static final int TYPE_BUG = 0;
	public static final int TYPE_ENHANCEMENT = 1;
	
	/**
	 * Type Names
	 */
	public static final String[] TYPE = {"Bug", "Enhancement"};

	private String _subject;
	private String _description;
	
	private int _priority;
	private int _area;
	private int _issueType;
	private int _status;
	
	private Date _createdOn;
	private Date _lastCommentOn;
	private Date _resolvedOn;
	
	private int _createdBy;
	private int _assignedTo;
	
	private int _majorVersion;
	private int _minorVersion;
	
	private Set<IssueComment> _comments;
	private int _commentCount;
	
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
	    _comments = new TreeSet<IssueComment>();
	}

	/**
	 * Helper method to validate a property code.
	 */
	private void validateCode(int code, String[] names) {
		if ((code < 0) || (code >= names.length))
			throw new IllegalArgumentException("Property value cannot be negative or >= " + names.length);
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
	 * Returns the number of comments on this Issue.
	 * @return the number of comments
	 * @see Issue#setCommentCount(int)
	 */
	public int getCommentCount() {
	   return _comments.isEmpty() ? _commentCount : _comments.size();
	}
	
	/**
	 * Returns all the comments associated with this issue.
	 * @return a Collection of IssueComments
	 * @see Issue#addComment(IssueComment)
	 */
	public Collection<IssueComment> getComments() {
		return _comments;
	}
	
	/**
	 * Returns the issue priority code.
	 * @return the priority code
	 * @see Issue#getPriorityName()
	 * @see Issue#setPriority(int)
	 */
	public int getPriority() {
		return _priority;
	}
	
	/**
	 * Returns the issue priority name (for JSPs).
	 * @return the priority name
	 * @see Issue#getPriority()
	 * @see Issue#setPriority(int)
	 */
	public String getPriorityName() {
		return Issue.PRIORITY[getPriority()];
	}
	
	/**
	 * Returns the issue status code.
	 * @return the status code
	 * @see Issue#getStatusName()
	 * @see Issue#setStatus(int)
	 */
	public int getStatus() {
		return _status;
	}
	
	/**
	 * Returns the issue status name (for JSPs).
	 * @return the status name
	 * @see Issue#getStatus()
	 * @see Issue#setStatus(int)
	 */
	public String getStatusName() {
		return Issue.STATUS[getStatus()];
	}
	
	/**
	 * Returns the issue type code.
	 * @return the type code
	 * @see Issue#getTypeName()
	 * @see Issue#setType(int)
	 */
	public int getType() {
		return _issueType;
	}
	
	/**
	 * Returns the issue type name (for JSPs).
	 * @return the type name
	 * @see Issue#getType()
	 * @see Issue#setType(int)
	 */
	public String getTypeName() {
		return Issue.TYPE[getType()];
	}
	
	/**
	 * Returns the issue area code.
	 * @return the area code
	 * @see Issue#getAreaName()
	 * @see Issue#setArea(int)
	 */
	public int getArea() {
		return _area;
	}
	
	/**
	 * Returns the issue area name (for JSPs).
	 * @return the area name
	 * @see Issue#getArea()
	 * @see Issue#setArea(int)
	 */
	public String getAreaName() {
		return Issue.AREA[getArea()];
	}
	
	/**
	 * Returns the date/time this Issue was created on.
	 * @return the creation date/time
	 * @see Issue#setCreatedOn(Date)
	 */
	public Date getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the date/time the last comment for this issue was created on.
	 * @return the creation date/time of the last comment
	 * @see Issue#setLastCommentOn(Date)
	 */
	public Date getLastCommentOn() {
		return _lastCommentOn;
	}
	
	/**
	 * Returns the date/time this issue was resolved on.
	 * @return the date/time the issue was resolved on, or null
	 * @see Issue#setResolvedOn(Date)
	 */
	public Date getResolvedOn() {
		return _resolvedOn;
	}
	
	/**
	 * The database ID of the Person creating this issue.
	 * @return the database ID
	 * @see Issue#setAuthorID(int)
	 * @see org.deltava.beans.DatabaseBean#getID()
	 */
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
	 * @see Issue#getComments()
	 */
	public void addComment(IssueComment ic) {
		ic.setIssueID(getID());
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
	public void setCreatedOn(Date d) {
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
	public void setLastCommentOn(Date d) {
		if ((d != null) && (d.before(_createdOn)))
			throw new IllegalArgumentException("Last Comment date cannot be before " + _createdOn);
		
		_lastCommentOn = d;
	}
	
	/**
	 * Updates the resolution date/time for this Issue.
	 * @param d the date/time the Issue was resolved
	 * @throws IllegalArgumentException if d is null or before getCreatedOn()
	 * @see Issue#getResolvedOn()
	 */
	public void setResolvedOn(Date d) {
		if ((d != null) && (d.before(_createdOn)))
			throw new IllegalArgumentException("Resolved On date cannot be before " + _createdOn);
		
		_resolvedOn = d;
	}
	
	/**
	 * Updates the major version for this Issue.
	 * @param v the major version
	 * @throws IllegalArgumentException if v is negative
	 * @see Issue#getMajorVersion()
	 * @see Issue#setMinorVersion(int)
	 */
	public void setMajorVersion(int v) {
		if (v < 0)
			throw new IllegalArgumentException("Major Version cannot be negative");
		
		_majorVersion = v;
	}
	
	/**
	 * Updates the minor version for this Issue.
	 * @param v the minor version
	 * @throws IllegalArgumentException if v is negative
	 * @see Issue#getMinorVersion()
	 * @see Issue#setMajorVersion(int)
	 */
	public void setMinorVersion(int v) {
		if (v < 0)
			throw new IllegalArgumentException("Minor Version cannot be negative");
		
		_minorVersion = v;		
	}
	
	/**
	 * Updates the database ID of this Issue's author.
	 * @param id the database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see Issue#getAuthorID()
	 */
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
	 * Updates the number of comments on this Issue.
	 * @param count the number of comments
	 * @throws IllegalStateException if comments have already been loaded
	 * @throws IllegalArgumentException if count is negative
	 * @see Issue#getCommentCount()
	 * @see Issue#addComment(IssueComment)
	 */
	public void setCommentCount(int count) {
	   if (_comments.size() > 0) {
	      throw new IllegalStateException("Comments already loaded");
	   } else if (count < 0) {
	      throw new IllegalArgumentException("Comment Count cannot be negative");
	   }
	   
	   _commentCount = count;
	}
	
	/**
	 * Updates this Issue's type.
	 * @param pv the type code
	 * @throws IllegalArgumentException if pv is negative or invalid
	 * @see Issue#getType()
	 * @see Issue#getTypeName()
	 */
	public void setType(int pv) {
		validateCode(pv, Issue.TYPE);
		_issueType = pv;
	}
	
	/**
	 * Updates this Issue's area.
	 * @param pv the area code
	 * @throws IllegalArgumentException if pv is negative or invalid
	 * @see Issue#getArea()
	 * @see Issue#getAreaName()
	 */
	public void setArea(int pv) {
		validateCode(pv, Issue.AREA);
		_area = pv;
	}
	
	/**
	 * Updates this Issue's priority.
	 * @param pv the priority code
	 * @throws IllegalArgumentException if pv is negative or invalid
	 * @see Issue#getPriority()
	 * @see Issue#getPriorityName()
	 */
	public void setPriority(int pv) {
		validateCode(pv, Issue.PRIORITY);
		_priority = pv;
	}
	
	/**
	 * Updates this Issue's status.
	 * @param pv the status code
	 * @throws IllegalArgumentException if pv is negative or invalid
	 * @see Issue#getStatus()
	 * @see Issue#getStatusName()
	 */
	public void setStatus(int pv) {
		validateCode(pv, Issue.STATUS);
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
	
	/**
	 * Compares two Issues by comparing their database ID.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o2) {
		Issue i2 = (Issue) o2;
		return new Integer(getID()).compareTo(new Integer(i2.getID()));
	}
	
	public String getRowClassName() {
		final String[] ROW_CLASSES = {"opt1", null, "opt2", "warn", "err", "opt3"};
		return ROW_CLASSES[_status];
	}
}