// Copyright 2005, 2006 Global Virtual Airlnes Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.Date;

import org.deltava.beans.*;

/**
 * A bean for storing web site issue comments.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class IssueComment extends DatabaseBean implements AuthoredBean {
	
	private int _createdBy;
	private int _issueID;
	
	private Date _createdOn;
	private String _comments;
	
	/**
	 * Creates a new Issue Comment.
	 * @param id the database ID
	 * @param comments the comment text
	 * @throws NullPointerException if comments is null
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see IssueComment#IssueComment(String)
	 * @see IssueComment#getComments()
	 * @see DatabaseBean#validateID(int, int)
	 */
	public IssueComment(int id, String comments) {
		this(comments);
		setID(id);
	}
	
	/**
	 * Creates a new Issue Comment.
	 * @param comments the comment text
	 * @throws NullPointerException if comments is null
	 * @see IssueComment#IssueComment(int, String)
	 * @see IssueComment#getComments()
	 */
	public IssueComment(String comments) {
		super();
		_comments = comments.trim();
		_createdOn = new Date();
	}
	
	/**
	 * Returns the database ID of the comment author.
	 * @return the database ID
	 * @see IssueComment#setAuthorID(int)
	 * @see org.deltava.beans.Person#getID()
	 */
	public int getAuthorID() {
		return _createdBy;
	}
	
	/**
	 * Returns the ID of the parent issue.
	 * @return the Issue ID
	 * @see IssueComment#setIssueID(int)
	 * @see Issue#getID()
	 */
	public int getIssueID() {
		return _issueID;
	}
	
	/**
	 * Returns the comment text.
	 * @return the comments
	 */
	public String getComments() {
		return _comments;
	}
	
	/**
	 * Returns the date/time this comment was created on.
	 * @return the date/time the comment was created
	 * @see IssueComment#setCreatedOn(Date)
	 */
	public Date getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Updates the date/time this comment was created on.
	 * @param d the date/time
	 * @throws IllegalArgumentException if d is null
	 * @see IssueComment#getCreatedOn()
	 */
	public void setCreatedOn(Date d) {
		if (d == null)
			throw new IllegalArgumentException("Creation Date cannot be null");
		
		_createdOn = d;
	}
	
	/**
	 * Updates the database ID of this comment's author.
	 * @param id the database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see IssueComment#getAuthorID()
	 * @see DatabaseBean#validateID(int, int)
	 * @see org.deltava.beans.Person#getID()
	 */
	public void setAuthorID(int id) {
		validateID(_createdBy, id);
		_createdBy = id;
	}
	
	/**
	 * Updates the database ID of the parent issue.
	 * @param id the database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see IssueComment#getIssueID()
	 * @see DatabaseBean#validateID(int, int)
	 * @see Issue#getID()
	 */
	public void setIssueID(int id) {
		validateID(_issueID, id);
		_issueID = id;
	}

	/**
	 * Compares this comment to another by comparing the creation dates.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o2) {
		IssueComment ic2 = (IssueComment) o2; 
		return _createdOn.compareTo(ic2.getCreatedOn());
	}
}