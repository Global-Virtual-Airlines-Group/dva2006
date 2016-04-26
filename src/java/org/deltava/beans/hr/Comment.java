// Copyright 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.hr;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean to store comments about a Job Posting.
 * @author Luke
 * @version 7.0
 * @since 3.4
 */

public class Comment extends DatabaseBean implements AuthoredBean {
	
	private int _authorID;
	private Instant _created;
	
	private String _body;

	/**
	 * Creates a new comment.
	 * @param jobID the Job Posting database ID
	 * @param authorID the author's database ID
	 */
	public Comment(int jobID, int authorID) {
		super();
		setID(jobID);
		setAuthorID(authorID);
	}
	
	/**
	 * Returns the creation date of this comment.
	 * @return the creation date/time
	 */
	public Instant getCreatedOn() {
		return _created;
	}
	
	/**
	 * Returns the comment body.
	 * @return the body
	 */
	public String getBody() {
		return _body;
	}

	@Override
	public int getAuthorID() {
		return _authorID;
	}

	@Override
	public void setAuthorID(int id) {
		validateID(_authorID, id);
		_authorID = id;
	}

	/**
	 * Updates the creation datae of this comment.
	 * @param dt the creation date/time
	 */
	public void setCreatedOn(Instant dt) {
		_created = dt;
	}
	
	/**
	 * Updates the comment body.
	 * @param body the body
	 */
	public void setBody(String body) {
		_body = body;
	}
}