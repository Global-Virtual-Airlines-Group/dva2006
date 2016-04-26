// Copyright 2006, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.cooler;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean to store Water Cooler thread history entries.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class ThreadUpdate extends DatabaseBean implements AuthoredBean, CalendarEntry {

	private Instant _date = Instant.now();
	private int _authorID;
	private String _msg;

	/**
	 * Creates a new Water Cooler thread update entry.
	 * @param id the Thread database ID
	 * @throws IllegalArgumentException if id is zero or negative
	 */
	public ThreadUpdate(int id) {
		super();
		setID(id);
	}

	/**
	 * Returns the date of this thread update.
	 * @return the date/time of this update
	 * @see ThreadUpdate#setDate(Instant)
	 */
	@Override
	public Instant getDate() {
		return _date;
	}

	/**
	 * Returns the database ID of the user who created this update.
	 * @return the user's database ID
	 * @see ThreadUpdate#setAuthorID(int)
	 */
	@Override
	public int getAuthorID() {
		return _authorID;
	}

	/**
	 * Returns the thread update message.
	 * @return the message
	 * @see ThreadUpdate#setMessage(String)
	 */
	public String getMessage() {
		return _msg;
	}

	/**
	 * Updates the status update date.
	 * @param dt the date/time the status update was created
	 * @see ThreadUpdate#getDate()
	 */
	public void setDate(Instant dt) {
		_date = dt;
	}

	@Override
	public void setAuthorID(int id) {
		validateID(_authorID, id);
		_authorID = id;
	}

	/**
	 * Updates the status update message.
	 * @param msg the message
	 * @throws NullPointerException if msg is null
	 * @see ThreadUpdate#getMessage()
	 */
	public void setMessage(String msg) {
		_msg = msg.trim();
	}

	/**
	 * Compares two beans by comparing their creation dates and thread IDs.
	 */
	@Override
	public int compareTo(Object o) {
		ThreadUpdate tu2 = (ThreadUpdate) o;
		int tmpResult = _date.compareTo(tu2._date);
		return (tmpResult == 0) ? super.compareTo(tu2) : tmpResult;
	}
}