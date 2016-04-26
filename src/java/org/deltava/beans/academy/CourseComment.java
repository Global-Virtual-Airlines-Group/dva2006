// Copyright 2006, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean to store comments between Instructors and Students. 
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class CourseComment extends DatabaseBean implements AuthoredBean {
	
	private int _authorID;
	private Instant _createdOn;
	private String _text;

	/**
	 * Creates a new Comment bean.
	 * @param courseID the database ID of the Course
	 * @param authorID the database ID of the author
	 * @throws IllegalArgumentException if courseID or authorID are zero or negative
	 */
	public CourseComment(int courseID, int authorID) {
		super();
		setID(courseID);
		setAuthorID(authorID);
	}
	
	@Override
	public int getAuthorID() {
		return _authorID;
	}
	
	/**
	 * Returns the Date the comment was created on.
	 * @return the date/time of the Comment
	 * @see CourseComment#setCreatedOn(Instant)
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the Comment text
	 * @return the text
	 * @see CourseComment#setText(String)
	 */
	public String getText() {
		return _text;
	}
	
	@Override
	public void setAuthorID(int id) {
		validateID(_authorID, id);
		_authorID = id;
	}
	
	/**
	 * Updates the creation date of the Comment.
	 * @param dt the date/time the Comment was written
	 * @see CourseComment#getCreatedOn()
	 */
	public void setCreatedOn(Instant dt) {
		_createdOn = dt;
	}
	
	/**
	 * Updates the Comment text.
	 * @param msg the text
	 * @see CourseComment#getText()
	 */
	public void setText(String msg) {
		_text = msg; 
	}

	/**
	 * Compares two Comments by comparing their creation dates.
	 */
	@Override
	public int compareTo(Object o) {
		CourseComment c2 = (CourseComment) o;
		return _createdOn.compareTo(c2._createdOn);
	}
}