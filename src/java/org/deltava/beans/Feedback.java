// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.time.Instant;

/**
 * A bean to store feedback about a particular item.
 * @author Luke
 * @version 11.6
 * @since 11.6
 */

public class Feedback extends DatabaseBean implements AuthoredBean, ViewEntry {
	
	private int _authorID;
	private Instant _createdOn;
	private int _score;
	
	private final String _type;
	private String _comments;

	/**
	 * Creates the bean.
	 * @param id the database ID of the feedback item
	 * @param c the feedback item class 
	 */
	public Feedback(int id, Class<?> c) {
		super();
		setID(id);
		_type = c.getSimpleName();
	}

	@Override
	public int getAuthorID() {
		return _authorID;
	}
	
	/**
	 * Returns the feedback item type.
	 * @return the type
	 */
	public String getType() {
		return _type;
	}

	/**
	 * Returns the feedback date.
	 * @return the date/time the feedback was received
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the feedback score.
	 * @return the score from 0 to 10
	 */
	public int getScore() {
		return _score;
	}
	
	/**
	 * Returns the feedback comments.
	 * @return the comments
	 */
	public String getComments() {
		return _comments;
	}

	@Override
	public void setAuthorID(int id) {
		validateID(_authorID, id);
		_authorID = id;
	}
	
	/**
	 * Updates the feedback date.
	 * @param dt the date/time the feedback was received
	 */
	public void setCreatedOn(Instant dt) {
		_createdOn = dt;
	}
	
	/**
	 * Updates the feedback comments.
	 * @param comments the comments
	 */
	public void setComments(String comments) {
		_comments = comments;
	}
	
	/**
	 * Updates the score.
	 * @param s a score between 0 and 10
	 */
	public void setScore(int s) {
		_score = Math.max(0, Math.min(10, s));
	}
	
	@Override
	public String getRowClassName() {
		if (_score < 5) return "err";
		if (_score < 8) return "warn";
		return null;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(getID()).append('-').append(_authorID);
		return buf.toString();
	}
}