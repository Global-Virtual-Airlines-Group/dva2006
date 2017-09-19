// Copyright 2006, 2007, 2010, 2012, 2014, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

import java.util.*;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.testing.CheckRide;

/**
 * A bean to store Flight Academy Course information.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

public class Course extends DatabaseBean implements ViewEntry {
	
	private String _certName;
	private String _code;
	private int _pilotID;
	private int _instructorID;
	private Status _status;
	private int _stage;
	private int _rideCount;
	
	private Instant _startDate;
	private Instant _endDate;
	private Instant _lastComment;
	
	private final Map<Integer, CheckRide> _checkRides = new TreeMap<Integer, CheckRide>();
	private final Map<Integer, CourseProgress> _progress = new TreeMap<Integer, CourseProgress>();
	private final SortedSet<CourseComment> _comments = new TreeSet<CourseComment>();

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
	 * Returns the certification code.
	 * @return the code
	 * @see Course#setCode(String)
	 */
	public String getCode() {
		return _code;
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
	 * @see Course#setStatus(Status)
	 */
	public Status getStatus() { 
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
	 * Returns the date this Course was started.
	 * @return the start date/time
	 * @see Course#setStartDate(Instant)
	 */
	public Instant getStartDate() {
		return _startDate;
	}
	
	/**
	 * Returns the date this Course was completed or abandoned.
	 * @return the end date/time, or null if in progress
	 * @see Course#setEndDate(Instant)
	 */
	public Instant getEndDate() {
		return _endDate;
	}
	
	/**
	 * Returns the date of the last comment. <i>This may not be populated</i>.
	 * @return the date/time of the last comment, or null
	 * @see Course#setLastComment(Instant)
	 */
	public Instant getLastComment() {
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
		return _progress.get(Integer.valueOf(seq));
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
	 * Returns the number of Check Rides required for this Course.
	 * @return the number of Check Rides
	 * @see Course#setRideCount(int)
	 */
	public int getRideCount() {
		return _rideCount;
	}
	
	/**
	 * Returns the index of the next required Check Ride.
	 * @return the Check Ride index
	 */
	public int getNextCheckRide() {
		int idx = 1;
		for (Map.Entry<Integer, CheckRide> me : _checkRides.entrySet()) {
			CheckRide cr = me.getValue();
			if (cr.getPassFail() && (cr.getIndex() == idx))
				idx++;
		}
		
		return Math.min(idx, _rideCount);
	}
	
	/**
	 * Returns the most recent check ride for this Course.
	 * @return a CheckRide object, or null if none
	 * @see Course#addCheckRide(CheckRide)
	 */
	public Collection<CheckRide> getCheckRides() {
		return _checkRides.values();
	}
	
	/**
	 * Adds a new Progress entry. If an existing entry is found, it is overwritten.
	 * @param cp the Progress bean
	 * @see Course#getProgress()
	 */
	public void addProgress(CourseProgress cp) {
		_progress.put(Integer.valueOf(cp.getID()), cp);
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
	 * Updates the Certification code.
	 * @param code the code
	 * @throws NullPointerException if code is null
	 * @see Course#getCode()
	 */
	public void setCode(String code) {
		_code = code.toUpperCase();
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
	 * @see Course#getInstructorID()
	 * @see Course#setPilotID(int)
	 */
	public void setInstructorID(int id) {
		_instructorID = Math.max(0, id);
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
	 * Updates the number of Check Rides required for this Course.
	 * @param cr the number of Check Rides
	 * @see Course#getRideCount()
	 */
	public void setRideCount(int cr) {
		_rideCount = Math.max(0, cr); 
	}
	
	/**
	 * Updates this Course's completion status.
	 * @param s the Status
	 * @see Course#getStatus()
	 */
	public void setStatus(Status s) {
		_status = s;
	}
	
	/**
	 * Updates this Course's stage.
	 * @param stage the stage number
	 * @see Course#getStage()
	 */
	public void setStage(int stage) {
		_stage = Math.max(1, stage);
	}
	
	/**
	 * Updates the start date for this Course.
	 * @param dt the start date/time
	 * @see Course#getStartDate()
	 */
	public void setStartDate(Instant dt) {
		_startDate = dt;
	}
	
	/**
	 * Updates the completion date for this Course.
	 * @param dt the end date/time
	 * @throws IllegalArgumentException if dt is before startDate
	 * @see Course#getEndDate()
	 */
	public void setEndDate(Instant dt) {
		if ((dt != null) && dt.isBefore(_startDate))
			throw new IllegalArgumentException("Invalid End Date - " + dt);
		
		_endDate = dt;
	}
	
	/**
	 * Updates the date of the last commnet. <i>This may not be populated</i>.
	 * @param dt the date/time of the last comment
	 * @see Course#getLastComment()
	 */
	public void setLastComment(Instant dt) {
		_lastComment = dt;
	}
	
	/**
	 * Sets the most recent Check Ride for this Course.
	 * @param cr the CheckRide object, or null
	 * @see Course#getCheckRides()
	 */
	public void addCheckRide(CheckRide cr) {
		if ((cr.getIndex() < 1) || (cr.getIndex() > _rideCount))
			throw new IllegalArgumentException("Invalid Check Ride number - " + cr.getIndex());
		
		Integer idx = Integer.valueOf(cr.getIndex());
		if (_checkRides.containsKey(idx)) {
			CheckRide cr2 = _checkRides.get(idx);
			if (cr.getDate().isAfter(cr2.getDate()))
				_checkRides.put(idx, cr);
		} else
			_checkRides.put(idx, cr);
	}

	/**
	 * Returns the CSS row class name if displayed in a view table.
	 * @return the CSS class name
	 */
	@Override
	public String getRowClassName() {
		final String[] CLASS_NAMES = {"opt1", "warn", null, "opt2"};
		return CLASS_NAMES[_status.ordinal()];
	}

	/**
	 * Compares two Courses by comparing their start date/times and Pilot IDs.
	 * @see Comparable#compareTo(Object)
	 */
	@Override
	public int compareTo(Object o) {
		Course c2 = (Course) o;
		int tmpResult = Integer.compare(_pilotID, c2._pilotID);
		return (tmpResult == 0) ? _startDate.compareTo(c2._startDate) : tmpResult;
	}
	
	@Override
	public String toString() {
		return _certName;
	}
}