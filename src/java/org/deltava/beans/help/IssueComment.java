// Copyright 2006, 2010, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.help;

import java.time.Instant;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to store Help Desk Issue comments.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class IssueComment extends DatabaseBean {
	
	private final int _authorID;
	private Instant _createdOn = Instant.now();
	private boolean _faq;
	private String _body;

	/**
	 * Creates a new Comment bean.
	 * @param authorID the database ID of the author
	 * @throws IllegalArgumentException if authorID is zero or negative
	 * @see IssueComment#getAuthorID()
	 */
	public IssueComment(int authorID) {
		super();
		_authorID = authorID;
	}

	/**
	 * Returns the body text of this Comment.
	 * @return the Comment text
	 * @see IssueComment#setBody(String)
	 */
	public String getBody() {
		return _body;
	}
	
	/**
	 * Returns the database ID of the Comment author.
	 * @return the author's database ID
	 * @see IssueComment#IssueComment(int)
	 */
	public int getAuthorID() {
		return _authorID;
	}

	/**
	 * Returns whether this Comment is an FAQ answer.
	 * @return TRUE if the Comment is an FAQ answer, otherwise FALSE
	 * @see IssueComment#setFAQ(boolean)
	 */
	public boolean getFAQ() {
		return _faq;
	}
	
	/**
	 * Returns the creation date of this Comment.
	 * @return the date/time the Comment was created
	 * @see IssueComment#setCreatedOn(Instant)
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Updates the body text of this Comment.
	 * @param body the body text
	 * @see IssueComment#setBody(String)
	 */
	public void setBody(String body) {
		_body = body;
	}
	
	/**
	 * Updates the creation date of this Comment.
	 * @param dt the date/time the Comment was created
	 * @see IssueComment#getCreatedOn()
	 */
	public void setCreatedOn(Instant dt) {
		_createdOn = dt;
	}
	
	/**
	 * Updates whether this Comment is an FAQ answer.
	 * @param isFAQ TRUE if the Comment is an FAQ answer, otherwise FALSE
	 * @see IssueComment#getFAQ()
	 */
	public void setFAQ(boolean isFAQ) {
		_faq = isFAQ;
	}
	
	/**
	 * Compares two comments by comparing their Issue ID and creation dates.
	 */
	@Override
	public int compareTo(Object o) {
		IssueComment ic2 = (IssueComment) o;
		int tmpResult = super.compareTo(ic2);
		return (tmpResult == 0) ? _createdOn.compareTo(ic2._createdOn) : tmpResult;
	}
	
	/**
	 * Checks for equality by comparison.
	 */
	@Override
	public boolean equals(Object o) {
		return (o instanceof IssueComment) ? (compareTo(o) == 0) : false;
	}
}