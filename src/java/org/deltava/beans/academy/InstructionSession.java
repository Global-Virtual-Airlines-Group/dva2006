// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

import java.util.Date;

import org.deltava.beans.*;

/**
 * A bean used to store Flight Academy instruction session information. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InstructionSession extends DatabaseBean implements Comparable, ViewEntry, Instruction {
	
	public static final int SCHEDULED = 0;
	public static final int CANCELED = 1;
	public static final int COMPLETE = 2;
	
	public static final String[] STATUS_NAMES = {"Scheduled", "Canceled", "Completed"};

	private String _courseName;
	private int _instructorID;
	private int _pilotID;
	private int _courseID;
	
	private Date _startTime;
	private Date _endTime;
	
	private int _status;
	private boolean _noShow;
	
	private String _remarks;
	
	/**
	 * Creates a new Instruction Session bean.
	 * @param id the database ID
	 * @param courseID the database ID of the Course
	 * @throws IllegalArgumentException if id is negative or courseID is zero or negative
	 */
	public InstructionSession(int id, int courseID) {
		super();
		setCourseID(courseID);
		if (id != 0)
			setID(id);
	}
	
	/**
	 * Returns the Course name. <i>This may not be populated</i>.
	 * @return the course name
	 * @see InstructionSession#setName(String)
	 */
	public String getName() {
		return _courseName;
	}
	
	/**
	 * Returns the Instructor assigned to this Session.
	 * @return the Instructor's database ID
	 * @see InstructionSession#setInstructorID(int)
	 * @see InstructionSession#getPilotID()
	 */
	public int getInstructorID() {
		return _instructorID;
	}
	
	/**
	 * Returns the Pilot participating in this Session.
	 * @return the Pilot's database ID
	 * @see InstructionSession#setPilotID(int)
	 * @see InstructionSession#getInstructorID()
	 */
	public int getPilotID() {
		return _pilotID;
	}
	
	/**
	 * Returns the Flight Academy Course ID.
	 * @return the database ID of the Course
	 * @see InstructionSession#setCourseID(int)
	 */
	public int getCourseID() {
		return _courseID; 
	}
	
	/**
	 * Returns the status of this Session.
	 * @return the status code
	 * @see InstructionSession#setStatus(int)
	 * @see InstructionSession#getStatusName()
	 */
	public int getStatus() {
		return _status;
	}
	
	/**
	 * Returns the status of this Session.
	 * @return the status name
	 * @see InstructionSession#setStatus(int)
	 * @see InstructionSession#getStatusName()
	 */
	public String getStatusName() {
		return STATUS_NAMES[_status];
	}
	
	/**
	 * Returns the start time of this Session.
	 * @return the start date/time
	 * @see InstructionSession#setStartTime(Date)
	 * @see CalendarEntry#getDate()
	 * @see InstructionSession#getEndTime()
	 */
	public Date getStartTime() {
		return _startTime;
	}
	
	/**
	 * Returns the end time of this Session.
	 * @return the end date/time
	 * @see InstructionSession#setEndTime(Date)
	 * @see InstructionSession#getStartTime()
	 */
	public Date getEndTime() {
		return _endTime;
	}
	
	/**
	 * Returns wether the Pilot missed the Session.
	 * @return TRUE if the Pilot did not attend, otherwise FALSE
	 * @see InstructionSession#setNoShow(boolean)
	 */
	public boolean getNoShow() {
		return _noShow;
	}
	
	/**
	 * Returns any remarks about the Session.
	 * @return the remarks
	 * @see InstructionSession#setComments(String)
	 */
	public String getComments() {
		return _remarks;
	}
	
	/**
	 * Updates the Course name.
	 * @param name the name
	 * @throws NullPointerException if name is null
	 * @see InstructionSession#getName()
	 */
	public void setName(String name) {
		_courseName = name.trim();
	}
	
	/**
	 * Updates the Instructor assigned to this session.
	 * @param id the Instructor database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see InstructionSession#getInstructorID()
	 * @see InstructionSession#setPilotID(int)
	 */
	public void setInstructorID(int id) {
		if (id < 1)
            throw new IllegalArgumentException("Instructor ID cannot be zero or negative");
		
		_instructorID = id;
	}
	
	/**
	 * Updates the Pilot participating in this session.
	 * @param id the Pilot database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see InstructionSession#getPilotID()
	 * @see InstructionSession#setInstructorID(int)
	 */
	public void setPilotID(int id) {
		validateID(_pilotID, id);
		_pilotID = id;
	}

	/**
	 * Updates the Flight Academy Course..
	 * @param id the Course database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see InstructionSession#getCourseID()
	 */
	public void setCourseID(int id) {
		validateID(_courseID, id);
		_courseID = id;
	}
	
	/**
	 * Updates the Session's status.
	 * @param status the status code
	 * @throws IllegalStateException if status is negative or invalid
	 * @see InstructionSession#getStatus()
	 * @see InstructionSession#getStatusName()
	 */
	public void setStatus(int status) {
		if ((status < 0) || (status > STATUS_NAMES.length))
			throw new IllegalArgumentException("Invalid Session status - " + status);
		
		_status = status;
	}
	
	/**
	 * Marks the Pilot as a &quot;no-show&quot;.
	 * @param noShow TRUE if the Pilot did not show up, otherwise FALSE
	 * @see InstructionSession#getNoShow()
	 */
	public void setNoShow(boolean noShow) {
		_noShow = noShow;
	}
	
	/**
	 * Updates the start time for this Session.
	 * @param dt the start date/time
	 * @see InstructionSession#getStartTime()
	 * @see CalendarEntry#getDate()
	 * @see InstructionSession#setEndTime(Date)
	 */
	public void setStartTime(Date dt) {
		_startTime = dt;
	}
	
	/**
	 * Updates the end time for this Session.
	 * @param dt the end date/time
	 * @throws IllegalArgumentException if dt is before startDate
	 * @see InstructionSession#getEndTime()
	 * @see InstructionSession#setStartTime(Date)
	 */
	public void setEndTime(Date dt) {
		if ((dt != null) && (dt.before(_startTime)))
			throw new IllegalArgumentException("Invalid End Time - " + dt);
		
		_endTime = dt;
	}
	
	/**
	 * Updates the remarks for this Session.
	 * @param txt the remarks
	 * @see InstructionSession#getComments()
	 */
	public void setComments(String txt) {
		_remarks = txt;
	}

	/**
	 * Comapres two sessions by comparing their start times.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		InstructionSession is2 = (InstructionSession) o;
		int tmpResult = _startTime.compareTo(is2._startTime);
		return (tmpResult == 0) ? super.compareTo(is2) : tmpResult;
	}

	/**
	 * Returns the CSS row class name if displayed in a view table.
	 * @return the CSS class name
	 */
	public String getRowClassName() {
		return _noShow ? "warn" : ((_status == CANCELED) ? "opt1" : null);
	}

	/**
	 * Returns the date if rendered within a calendar view.
	 * @return the start date/time
	 * @see InstructionSession#getStartTime()
	 */
	public Date getDate() {
		return _startTime;
	}
}