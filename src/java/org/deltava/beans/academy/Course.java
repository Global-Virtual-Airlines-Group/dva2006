// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

import java.util.*;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.ViewEntry;

/**
 * A bean to store Flight Academy Course information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Course extends DatabaseBean implements ViewEntry, Comparable {
	
	public static final int STARTED = 0;
	public static final int ABANDONED = 1;
	public static final int COMPLETE = 2;
	public static final int PENDING = 3;
	
	public static final String[] STATUS_NAMES = {"In Progress", "Abandoned", "Complete", "Pending"};
	
	private String _certName;
	private int _pilotID;
	private int _instructorID;
	private int _status;
	private int _stage;
	
	private Date _startDate;
	private Date _endDate;
	private Date _lastComment;
	
	private Map<Integer, CourseProgress> _progress;
	private SortedSet<CourseComment> _comments;

	/**
	 * Creates a new Course bean.
	 * @param name the Certification name
	 * @param pilotID the database ID of the Pilot taking the course
	 * @throws NullPointerException if name is null
	 * @throws IllegalArgumentException if pilotID is zero or negative
	 */
	public Course(String name, int pilotID) {
		super();
		setName(name);
		setPilotID(pilotID);
		_progress = new TreeMap<Integer, CourseProgress>();
		_comments = new TreeSet<CourseComment>();
	}
	
	/**
	 * Returns the certification name.
	 * @return the name
	 * @see Course#setName(String)
	 */
	public String getName() {
		return _certName;
	}
	
	/**
	 * Returns the database ID of the Pilot taking the course.
	 * @return the database ID
	 * @see Course#setPilotID(int)
	 * @see Course#getInstructorID()
	 */
	public int getPilotID() {
		return _pilotID;
	}
	
	/**
	 * Returns the database ID of the assigned Instructor.
	 * @return the Instructor's database ID
	 * @see Course#setInstructorID(int)
	 */
	public int getInstructorID() {
		return _instructorID;
	}
	
	/**
	 * Returns the status of this Course.
	 * @return the status code
	 * @see Course#setStatus(int)
	 * @see Course#getStatusName()
	 */
	public int getStatus() { 
		return _status;
	}
	
	/**
	 * Returns the stage value of this Course.
	 * @return the stage
	 * @see Course#setStage(int)
	 */
	public int getStage() {
		return _stage;
	}
	
	/**
	 * Returns the status of this Course.
	 * @return the status name
	 * @see Course#getStatus()
	 * @see Course#setStatus(int)
	 */
	public String getStatusName() {
		return STATUS_NAMES[_status];
	}
	
	/**
	 * Returns the date this Course was started.
	 * @return the start date/time
	 * @see Course#setStartDate(Date)
	 */
	public Date getStartDate() {
		return _startDate;
	}
	
	/**
	 * Returns the date this Course was completed or abandoned.
	 * @return the end date/time, or null if in progress
	 * @see Course#setEndDate(Date)
	 */
	public Date getEndDate() {
		return _endDate;
	}
	
	/**
	 * Returns the date of the last comment. <i>This may not be populated</i>.
	 * @return the date/time of the last comment, or null
	 * @see Course#setLastComment(Date)
	 */
	public Date getLastComment() {
		return _comments.isEmpty() ? _lastComment : _comments.last().getCreatedOn();
	}
	
	/**
	 * Returns any progress entries associated with this Course.
	 * @return a Collection of CourseProgress beans
	 * @see Course#addProgress(CourseProgress)
	 * @see Course#getProgressEntry(int)
	 */
	public Collection<CourseProgress> getProgress() {
		return _progress.values();
	}
	
	/**
	 * Returns a particular progress entry.
	 * @param seq the sequence number
	 * @return a CourgeProgress bean, or null if not found
	 */
	public CourseProgress getProgressEntry(int seq) {
		return _progress.get(new Integer(seq));
	}
	
	/**
	 * Returns any comments associated with this Course.
	 * @return a Collection of CourseComment beans
	 * @see Course#addComment(CourseComment)
	 */
	public Collection<CourseComment> getComments() {
		return _comments;
	}
	
	/**
	 * Adds a new Progress entry. If an existing entry is found, it is overwritten.
	 * @param cp the Progress bean
	 * @see Course#getProgress()
	 */
	public void addProgress(CourseProgress cp) {
		_progress.put(new Integer(cp.getID()), cp);
	}
	
	/**
	 * Adds a new Comment entry.
	 * @param c the Comment bean
	 * @see Course#getComments()
	 */
	public void addComment(CourseComment c) {
		_comments.add(c);
	}
	
	/**
	 * Updates the database ID of the enrolled Pilot.
	 * @param id the database ID
	 * @throws IllegalArgumentException if id is zero or negative or changes
	 * @see Course#getPilotID()
	 * @see Course#setInstructorID(int)
	 */
	public void setPilotID(int id) {
		validateID(_pilotID, id);
		_pilotID = id;
	}
	
	/**
	 * Updates the database ID of the assigned Instructor.
	 * @param id the database ID
	 * @throws IllegalArgumentException if id is negative
	 * @see Course#getInstructorID()
	 * @see Course#setPilotID(int)
	 */
	public void setInstructorID(int id) {
		if (id < 0)
			throw new IllegalArgumentException("Invalid Database ID - " + id);
		
		_instructorID = id;
	}
	
	/**
	 * Updates the certification name for this Course.
	 * @param name the name
	 * @throws NullPointerException if name is null
	 * @see Course#getName()
	 */
	public void setName(String name) {
		_certName = name.trim();
	}
	
	/**
	 * Updates this Course's completion status.
	 * @param code the status code
	 * @throws IllegalArgumentException if code is negative or invalid
	 * @see Course#getStatus()
	 * @see Course#getStatusName()
	 */
	public void setStatus(int code) {
		if ((code < 0) || (code >= STATUS_NAMES.length))
			throw new IllegalArgumentException("Invalid Status - " + code);
		
		_status = code;
	}
	
	/**
	 * Updates this Course's stage.
	 * @param stage the stage number
	 * @throws IllegalArgumentException if stage is zero or negative
	 * @see Course#getStage()
	 */
	public void setStage(int stage) {
		if (stage < 1)
			throw new IllegalArgumentException("Invalid stage - " + stage);
		
		_stage = stage;
	}
	
	/**
	 * Updates the start date for this Course.
	 * @param dt the start date/time
	 * @see Course#getStartDate()
	 */
	public void setStartDate(Date dt) {
		_startDate = dt;
	}
	
	/**
	 * Updates the completion date for this Course.
	 * @param dt the end date/time
	 * @throws IllegalArgumentException if dt is before startDate
	 * @see Course#getEndDate()
	 */
	public void setEndDate(Date dt) {
		if ((dt != null) && (dt.before(_startDate)))
			throw new IllegalArgumentException("Invalid End Date - " + dt);
		
		_endDate = dt;
	}
	
	/**
	 * Updates the date of the last commnet. <i>This may not be populated</i>.
	 * @param dt the date/time of the last comment
	 * @see Course#getLastComment()
	 */
	public void setLastComment(Date dt) {
		_lastComment = dt;
	}

	/**
	 * Returns the CSS row class name if displayed in a view table.
	 * @return the CSS class name
	 */
	public String getRowClassName() {
		final String[] CLASS_NAMES = {"opt1", "warn", null, "opt2"};
		return CLASS_NAMES[_status];
	}

	/**
	 * Compares two Courses by comparing their start date/times and Pilot IDs.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		Course c2 = (Course) o;
		int tmpResult = new Integer(_pilotID).compareTo(new Integer(c2._pilotID));
		return (tmpResult == 0) ? _startDate.compareTo(c2._startDate) : tmpResult;
	}
	
	/**
	 * Returns the Certification name.
	 */
	public String toString() {
		return _certName;
	}
}