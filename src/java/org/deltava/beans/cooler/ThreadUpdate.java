// Copyright 2006, 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.cooler;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean to store Water Cooler thread history entries.
 * @author Luke
 * @version 7.4
 * @since 1.0
 */

public class ThreadUpdate extends DatabaseBean implements AuditEntry, CalendarEntry {

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

	@Override
	public Instant getDate() {
		return _date;
	}

	@Override
	public int getAuthorID() {
		return _authorID;
	}

	 @Override
	public String getDescription() {
		return _msg;
	}
	 
	 @Override
	 public String getAuditType() {
		 return "Thread";
	 }
	 
	 @Override
	 public String getAuditID() {
		 return getHexID();
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
	 * @see ThreadUpdate#getDescription()
	 */
	public void setDescription(String msg) {
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