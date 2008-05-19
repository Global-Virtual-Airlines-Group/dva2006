// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.Date;

import org.deltava.beans.*;

/**
 * A bean to store Dispatcher schedule entries.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class DispatchScheduleEntry extends DatabaseBean implements CalendarEntry, AuthoredBean {
	
	private int _dispatcherID;
	private Date _startTime;
	private Date _endTime;
	
	private String _comments;

	/**
	 * Creates a new service entry.
	 * @param id the dispatcher's database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 */
	public DispatchScheduleEntry(int id) {
		super();
		setAuthorID(id);
	}
	
	/**
	 * Returns the Dispatcher ID.
	 * @return the Dispatcher database ID
	 */
	public int getAuthorID() {
		return _dispatcherID;
	}
	
	/**
	 * Returns any dispatcher comments.
	 * @return the comments
	 */
	public String getComments() {
		return _comments;
	}
	
	/**
	 * Returns the start date/time.
	 */
	public Date getDate() {
		return _startTime;
	}
	
	/**
	 * Returns the start of this service time.
	 * @return the start date/time
	 * @see CalendarEntry#getDate()
	 */
	public Date getStartTime() {
		return _startTime;
	}

	/**
	 * Returns the end of this service time.
	 * @return the end date/time
	 * @see DispatchScheduleEntry#getStartTime()
	 */
	public Date getEndTime() {
		return _endTime;
	}
	
	/**
	 * Updates the Dispatcher ID.
	 * @param id the Dispatcher database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 */
	public void setAuthorID(int id) {
		DatabaseBean.validateID(_dispatcherID, id);
		_dispatcherID = id;
	}

	/**
	 * Updates the dispatcher comments.
	 * @param body the comments
	 * @see DispatchScheduleEntry#getComments()
	 */
	public void setComments(String body) {
		_comments = body;
	}
	
	/**
	 * Updates the service start time.
	 * @param dt the start date/time
	 * @throws NullPointerException if dt is null
	 */
	public void setStartTime(Date dt) {
		if (dt == null)
			throw new NullPointerException("Start date/time cannot be null");
		
		_startTime = dt;
	}
	
	/**
	 * Updates the service end time.
	 * @param dt the end date/time
	 * @see DispatchScheduleEntry#getEndTime()
	 * @throws NullPointerException if dt is null
	 * @throws IllegalArgumentException if dt is before the start time
	 */
	public void setEndTime(Date dt) {
		if (dt.before(_startTime))
			throw new IllegalArgumentException("End cannot be before start time");
		
		_endTime = dt;
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
	
	public String toString() {
		StringBuilder buf = new StringBuilder(String.valueOf(getID()));
		buf.append('-');
		buf.append(_startTime.toString());
		buf.append('-');
		buf.append(_endTime.toString());
		return buf.toString();
	}
}