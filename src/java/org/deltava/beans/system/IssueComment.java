// Copyright 2005, 2006, 2011, 2016 Global Virtual Airlnes Group. All Rights Reserved.
package org.deltava.beans.system;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean for storing development issue comments.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class IssueComment extends DatabaseBlobBean {
	
	private int _createdBy;
	private int _issueID;
	
	private String _name;
	private int _size;
	
	private Instant _createdOn = Instant.now();
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
	 * @see IssueComment#setCreatedOn(Instant)
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the size of the attachment.
	 * @return the size in bytes
	 * @see IssueComment#setSize(int)
	 */
	@Override
	public int getSize() {
		return isLoaded() ? super.getSize() : _size;
	}
	
	/**
	 * Returns the file extension.
	 * @return the extension, or the file name if none
	 */
	public String getExtension() {
		int pos = _name.lastIndexOf('.');
		return _name.substring(pos + 1);
	}
	
	/**
	 * Returns the name of the attachment.
	 * @return the file name
	 * @see IssueComment#setName(String)
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the file buffer.
	 * @return the buffer
	 * @throws IllegalStateException if not loaded
	 */
	public byte[] getBuffer() {
		if (!isLoaded())
			throw new IllegalStateException("Not loaded");
		
		return _buffer;
	}
	
	/**
	 * Updates the name of the attachment.
	 * @param name the file name
	 * @see IssueComment#getName()
	 */
	public void setName(String name) {
		_name = name;
	}
	
	/**
	 * Updates the size of the attachment.
	 * @param size the size in bytes
	 * @throws IllegalArgumentException if the attachment has been loaded
	 * @see IssueComment#getSize()
	 */
	public void setSize(int size) {
		if (isLoaded())
			throw new IllegalArgumentException("Attachment already loaded");
		
		_size = Math.max(0, size);
	}
	
	/**
	 * Updates the date/time this comment was created on.
	 * @param d the date/time
	 * @throws IllegalArgumentException if d is null
	 * @see IssueComment#getCreatedOn()
	 */
	public void setCreatedOn(Instant d) {
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
	@Override
	public int compareTo(Object o2) {
		IssueComment ic2 = (IssueComment) o2; 
		int tmpResult = _createdOn.compareTo(ic2.getCreatedOn());
		return (tmpResult == 0) ? super.compareTo(o2) : tmpResult;
	}
}