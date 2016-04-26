// Copyright 2006, 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean to track Flight Academy Course requirements. Each Certification has a number of
 * requirements that need to be completed before the Course is done.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class CourseProgress extends CertificationRequirement implements AuthoredBean, ViewEntry {
	
	private int _courseID;
	private int _authorID;
	private boolean _complete;
	private Instant _completedOn;

	/**
	 * Creates a new Course Progress entry.
	 * @param courseID the database ID of the Course
	 * @param sequenceID the order number
	 * @throws IllegalArgumentException if courseID or sequenceID are zero or negative
	 */
	public CourseProgress(int courseID, int sequenceID) {
		super(sequenceID);
		setCourseID(courseID);
	}
	
	/**
	 * Returns whether this requirement has been completed.
	 * @return TRUE if the requirement is complete, otherwise FALSE
	 * @see CourseProgress#setComplete(boolean)
	 */
	public boolean getComplete() {
		return _complete;
	}
	
	/**
	 * Returns the completion date for this entry.
	 * @return the date/time this was marked complete, otherwise FALSE
	 * @see CourseProgress#setCompletedOn(Instant)
	 */
	public Instant getCompletedOn() {
		return _completedOn;
	}
	
	/**
	 * Returns the Course ID.
	 * @return the database ID of the Course
	 * @see CourseProgress#setCourseID(int)
	 */
	public int getCourseID() {
		return _courseID;
	}
	
	@Override
	public int getAuthorID() {
		return _authorID;
	}
	
	/**
	 * Updates the Course ID for this Progress entry.
	 * @param id the database ID of the Course
	 * @throws IllegalArgumentException if id is negative
	 * @see CourseProgress#getCourseID()
	 */
	public void setCourseID(int id) {
		if (id > 0)
			DatabaseBean.validateID(_courseID, id);
		
		_courseID = id;
	}

	@Override
	public void setAuthorID(int id) {
		if (id < 0)
			throw new IllegalArgumentException("Invalid Author ID - " + id);
		
		_authorID = id;
	}

	/**
	 * Updates whether this requirement has been completed.
	 * @param done TRUE if the requirement is complete, otherwise FALSE
	 * @see CourseProgress#getComplete()
	 */
	public void setComplete(boolean done) {
		_complete = done;
	}
	
	/**
	 * Updates the complation date of this requirement.
	 * @param dt the date/time it was completed or null if incomplete
	 * @see CourseProgress#getCompletedOn()
	 */
	public void setCompletedOn(Instant dt) {
		_completedOn = dt;
		_complete = (dt != null);
	}
	
	/**
	 * Returns the CSS row class name if rendered in a view table.
	 * @return the CSS class name
	 */
	@Override
	public String getRowClassName() {
		return _complete ? "opt3" : null;
	}

	/**
	 * Checks for equality by comparing sequence numbers.
	 */
	@Override
	public boolean equals(Object o) {
		return (o instanceof CourseProgress) ? (compareTo(o) == 0) : false;
	}
}