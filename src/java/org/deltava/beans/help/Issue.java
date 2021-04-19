// Copyright 2006, 2016, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.help;

import java.util.*;
import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean to store Help Desk Issues.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class Issue extends DatabaseBean implements Auditable, AuthoredBean, ViewEntry {
	
	private int _authorID;
	private int _assigneeID;
	private IssueStatus _status;
	private int _commentCount;
	private int _lastCommentID;
	
	private boolean _public;
	private boolean _faq;
	
	private Instant _createdOn;
	private Instant _lastComment;
	private Instant _resolvedOn;
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

	@Override
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
	 * @return the IssueStatus
	 * @see Issue#setStatus(IssueStatus)
	 */
	public IssueStatus getStatus() {
		return _status;
	}
	
	/**
	 * Returns whether the Issue is Public.
	 * @return TRUE if the Issue is Public, otherwise FALSE
	 * @see Issue#setPublic(boolean)
	 */
	public boolean getPublic() {
		return _public || _faq;
	}
	
	/**
	 * Returns whether the Issue is in the FAQ.
	 * @return TRUE if the Issue is in the FAQ, otherwise FALSE
	 * @see Issue#setFAQ(boolean)
	 */
	public boolean getFAQ() {
		return _faq;
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
	 * @see Issue#setCreatedOn(Instant)
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the date/time the Issue was resolved on.
	 * @return the Issue resolution date/time
	 * @see Issue#setResolvedOn(Instant)
	 */
	public Instant getResolvedOn() {
		return _resolvedOn;
	}
	
	/**
	 * Returns the date of the last comment. This may be null if no comments have been entered.
	 * @return the date/time of the last comment
	 * @see Issue#setLastComment(Instant)
	 */
	public Instant getLastComment() {
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
		return (_comments == null) ? new HashSet<IssueComment>() : _comments;
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

	@Override
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
		validateID(0, id);
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
	public void setCreatedOn(Instant dt) {
		_createdOn = dt;
	}
	
	/**
	 * Updates the date of the last comment on this Issue.
	 * @param dt the date/time of the last comment
	 * @see Issue#getLastComment()
	 */
	public void setLastComment(Instant dt) {
		_lastComment = dt;
	}
	
	/**
	 * Updates the resolution date of this Issue.
	 * @param dt the date/time the Issue was resolved
	 * @see Issue#getResolvedOn()
	 */
	public void setResolvedOn(Instant dt) {
		_resolvedOn = dt;
	}
	
	/**
	 * Updates this Issue's status.
	 * @param status the IssueStatus
	 * @see Issue#getStatus()
	 */
	public void setStatus(IssueStatus status) {
		_status = status;
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
	 * Marks the Issue as part of the FAQ.
	 * @param isFAQ TRUE if the Issue is part of the FAQ, otherwise FALSE
	 * @see Issue#getFAQ()
	 */
	public void setFAQ(boolean isFAQ) {
		_faq = isFAQ;
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
	
	@Override
	public String getAuditID() {
		return getHexID();
	}
	
	@Override
	public boolean isCrossApp() {
		return false;
	}

	@Override
	public String getRowClassName() {
		final String[] ROW_CLASSES = {null, "opt2", "opt1"};
		return _faq ? "opt3" : ROW_CLASSES[_status.ordinal()];
	}
}