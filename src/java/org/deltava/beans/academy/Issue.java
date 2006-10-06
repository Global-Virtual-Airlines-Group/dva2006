// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.util.StringUtils;

/**
 * A bean to store Flight Academy issues.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Issue extends DatabaseBean implements ViewEntry {
	
	public static final int OPEN = 0;
	public static final int CLOSED = 1;
	
	public static final String[] STATUS_NAMES = {"Open", "Closed"};
	
	private int _authorID;
	private int _assigneeID;
	private int _status;
	private int _commentCount;
	private int _lastCommentID;
	
	private boolean _public;
	
	private Date _createdOn;
	private Date _lastComment;
	private Date _resolvedOn;
	private String _subject;
	private String _body;
	
	private Collection<IssueComment> _comments;

	/**
	 * Creates a new Issue bean.
	 * @param subj the Issue subject
	 * @throws NullPointerException if subj is null
	 */
	public Issue(String subj) {
		super();
		setSubject(subj);
	}

	/**
	 * Returns the database ID of the Issue author.
	 * @return the author's database ID
	 * @see Issue#setAuthorID(int)
	 * @see Issue#getLastCommentAuthorID()
	 */
	public int getAuthorID() {
		return _authorID;
	}
	
	/**
	 * Returns the database ID of the Person this Issue is assigned to.
	 * @return the Assignee's database ID
	 * @see Issue#setAssignedTo(int)
	 * @see Issue#getAuthorID()
	 */
	public int getAssignedTo() {
		return _assigneeID;
	}
	
	/**
	 * Returns the database ID of the author of the last comment on this Issue.
	 * @return the last comment author's datbase ID
	 * @see Issue#setLastCommentAuthorID(int)
	 * @see Issue#getAuthorID()
	 */
	public int getLastCommentAuthorID() {
		return _lastCommentID;
	}
	
	/**
	 * Returns the Issue status.
	 * @return the status code
	 * @see Issue#setStatus(int)
	 * @see Issue#getStatusName()
	 */
	public int getStatus() {
		return _status;
	}
	
	/**
	 * Returns wether the Issue is Public.
	 * @return TRUE if the Issue is Public, otherwise FALSE
	 * @see Issue#setPublic(boolean)
	 */
	public boolean getPublic() {
		return _public;
	}

	/**
	 * Returns the Issue status name, for use in a JSP.
	 * @return the status name
	 * @see Issue#STATUS_NAMES
	 * @see Issue#getStatus()
	 * @see Issue#setStatus(int)
	 */
	public String getStatusName() {
		return STATUS_NAMES[_status];
	}
	
	/**
	 * Returns the Issue subject.
	 * @return the subject
	 * @see Issue#setSubject(String)
	 */
	public String getSubject() {
		return _subject;
	}
	
	/**
	 * Returns the Issue body text.
	 * @return the body text
	 * @see Issue#setBody(String)
	 */
	public String getBody() {
		return _body;
	}
	
	/**
	 * Returns the date/time the Issue was created on.
	 * @return the Issue creation date/time
	 * @see Issue#setCreatedOn(Date)
	 */
	public Date getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the date/time the Issue was resolved on.
	 * @return the Issue resolution date/time
	 * @see Issue#setResolvedOn(Date)
	 */
	public Date getResolvedOn() {
		return _resolvedOn;
	}
	
	/**
	 * Returns the date of the last comment. This may be null if no comments have been entered.
	 * @return the date/time of the last comment
	 * @see Issue#setLastComment(Date)
	 */
	public Date getLastComment() {
		return _lastComment;
	}
	
	/**
	 * Returns the number of Comments for this Issue.
	 * @return the number of Comments
	 * @see Issue#setCommentCount(int)
	 * @see Issue#addComment(IssueComment)
	 */
	public int getCommentCount() {
		return (_comments == null) ? _commentCount : _comments.size();
	}
	
	/**
	 * Returns all comments for this Issue.
	 * @return a Collection of IssueComment beans
	 * @see Issue#addComment(IssueComment)
	 * @see IssueComment
	 */
	public Collection<IssueComment> getComments() {
		return _comments;
	}

	/**
	 * Adds a Comment to this Issue.
	 * @param ic the IssueComment bean to add
	 * @see Issue#getComments()
	 */
	public void addComment(IssueComment ic) {
		if (_comments == null)
			_comments = new TreeSet<IssueComment>();
		
		ic.setID(getID());
		_comments.add(ic);
	}
	
	/**
	 * Updates the Issue subject.
	 * @param sbj the subject
	 * @throws NullPointerException if sbj is null
	 * @see Issue#getSubject()
	 */
	public void setSubject(String sbj) {
		_subject = sbj.trim();
	}
	
	/**
	 * Updates the Issue body text.
	 * @param body the body text
	 * @see Issue#getBody()
	 */
	public void setBody(String body) {
		_body = body;
	}
	
	/**
	 * Updates the database ID of the Issue author.
	 * @param id the author's database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see Issue#getAuthorID()
	 * @see Issue#setLastCommentAuthorID(int)
	 */
	public void setAuthorID(int id) {
		validateID(_authorID, id);
		_authorID = id;
	}
	
	/**
	 * Updates the database ID of the Issue assignee.
	 * @param id the assignee's database ID
	 * @throws IllegalArgumentException if id is negative
	 * @see Issue#getAssignedTo()
	 * @see Issue#setAuthorID(int)
	 */
	public void setAssignedTo(int id) {
		if (id != 0)
			validateID(_assigneeID, id);
		
		_assigneeID = id;
	}
	
	/**
	 * Updates the database ID of the author of the last Comment on this Issue.
	 * @param id the author's database ID
	 * @throws IllegalArgumentException if id is negative
	 * @see Issue#getLastCommentAuthorID()
	 * @see Issue#setAuthorID(int)
	 */
	public void setLastCommentAuthorID(int id) {
		if (id != 0)
			validateID(_lastCommentID, id);
		
		_lastCommentID = id;
	}
	
	/**
	 * Updates the creation date of this Issue.
	 * @param dt the date/time the Issue was created
	 * @see Issue#getCreatedOn()
	 */
	public void setCreatedOn(Date dt) {
		_createdOn = dt;
	}
	
	/**
	 * Updates the date of the last comment on this Issue.
	 * @param dt the date/time of the last comment
	 * @see Issue#getLastComment()
	 */
	public void setLastComment(Date dt) {
		_lastComment = dt;
	}
	
	/**
	 * Updates the resolution date of this Issue.
	 * @param dt the date/time the Issue was resolved
	 * @see Issue#getResolvedOn()
	 */
	public void setResolvedOn(Date dt) {
		_resolvedOn = dt;
	}
	
	/**
	 * Updates this Issue's status.
	 * @param status the status code
	 * @throws IllegalArgumentException if status is invalid
	 * @see Issue#setStatus(String)
	 * @see Issue#getStatus()
	 * @see Issue#getStatusName()
	 */
	public void setStatus(int status) {
		if ((status < 0) || (status >= STATUS_NAMES.length))
			throw new IllegalArgumentException("Invalid status - " + status);
		
		_status = status;
	}
	
	/**
	 * Updates this Issues's status.
	 * @param statusName the status code description
	 * @throws IllegalArgumentException if statusName is invalid
	 * @see Issue#setStatus(int)
	 * @see Issue#getStatusName()
	 * @see Issue#getStatus()
	 */
	public void setStatus(String statusName) {
		setStatus(StringUtils.arrayIndexOf(STATUS_NAMES, statusName)); 
	}
	
	/**
	 * Marks the Issue as Public.
	 * @param isPublic TRUE if the Issue is public, otherwise FALSE
	 * @see Issue#getPublic() 
	 */
	public void setPublic(boolean isPublic) {
		_public = isPublic;
	}
	
	/**
	 * Updates the number of Comments for this Issue.
	 * @param comments the number of Comments
	 * @throws IllegalArgumentException if comments are already populated or negative
	 * @see Issue#getCommentCount()
	 */
	public void setCommentCount(int comments) {
		if (_comments != null)
			throw new IllegalArgumentException("Comments already populated!");
		else if (comments < 0)
			throw new IllegalArgumentException("Comment count cannot be negative");
		
		_commentCount = comments;
	}

	/**
	 * Returns the CSS class name to use if displayed in a view table.
	 * @return the CSS class name
	 */
	public String getRowClassName() {
		final String[] ROW_CLASSES = {null, "opt1"};
		return ROW_CLASSES[_status];
	}
}