// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

import java.util.Date;

import org.deltava.beans.*;

/**
 * A bean to block off a Flight Academy Instructor's time.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InstructionBusy extends DatabaseBean implements Comparable, CalendarEntry, ViewEntry, InstructorBean {

	private Date _startTime;
	private Date _endTime;

	private String _comments;

	/**
	 * Initializes the bean.
	 * @param instructorID the Instructor's database ID
	 * @throws IllegalArgumentException if instructorID is zero or negative
	 */
	public InstructionBusy(int instructorID) {
		super();
		setID(instructorID);
	}

	/**
	 * Returns any comments associated with this entry.
	 * @return the comments
	 */
	public String getComments() {
		return _comments;
	}

	/**
	 * Returns the start of this busy time.
	 * @return the start date/time
	 * @see CalendarEntry#getDate()
	 * @see InstructionBusy#getEndTime()
	 */
	public Date getStartTime() {
		return _startTime;
	}

	/**
	 * Returns the end of this busy time.
	 * @return the end date/time
	 * @see InstructionBusy#getStartTime()
	 */
	public Date getEndTime() {
		return _endTime;
	}

	/**
	 * Returns the start date/time.
	 */
	public Date getDate() {
		return _startTime;
	}
	
	/**
	 * Returns the Instructor database ID.
	 */
	public int getInstructorID() {
		return getID();
	}

	/**
	 * Updates the start time for this busy period.
	 * @param dt the start date/time
	 * @see InstructionBusy#getStartTime()
	 * @see CalendarEntry#getDate()
	 * @see InstructionBusy#setEndTime(Date)
	 */
	public void setStartTime(Date dt) {
		_startTime = dt;
	}

	/**
	 * Updates the end time for this busy period.
	 * @param dt the end date/time
	 * @throws IllegalArgumentException if dt is before startDate
	 * @see InstructionBusy#getEndTime()
	 * @see InstructionBusy#setStartTime(Date)
	 */
	public void setEndTime(Date dt) {
		if ((dt != null) && (dt.before(_startTime)))
			throw new IllegalArgumentException("Invalid End Time - " + dt);

		_endTime = dt;
	}

	/**
	 * Updates the remarks for this busy time.
	 * @param txt the comments
	 * @see InstructionBusy#getComments()
	 */
	public void setComments(String txt) {
		_comments = txt;
	}

	/**
	 * Returns the CSS row class name if displayed in a view table.
	 * @return the CSS class name
	 */
	public String getRowClassName() {
		return "warn";
	}
	
	/**
	 * Comapres two sessions by comparing their start times.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		CalendarEntry ce2 = (CalendarEntry) o;
		int tmpResult = _startTime.compareTo(ce2.getDate());
		return (tmpResult == 0) ? super.compareTo(ce2) : tmpResult;
	}
}