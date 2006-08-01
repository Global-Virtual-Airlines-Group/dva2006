// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.cooler;

import java.util.Date;

import org.deltava.beans.*;

/**
 * A bean to store Water Cooler thread history entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ThreadUpdate extends DatabaseBean implements AuthoredBean {

	private Date _date;
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
		_date = new Date();
	}

	/**
	 * Returns the date of this thread update.
	 * @return the date/time of this update
	 * @see ThreadUpdate#setDate(Date)
	 */
	public Date getDate() {
		return _date;
	}

	/**
	 * Returns the database ID of the user who created this update.
	 * @return the user's database ID
	 * @see ThreadUpdate#setAuthorID(int)
	 */
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
	public void setDate(Date dt) {
		_date = dt;
	}

	/**
	 * Updates the thread update author.
	 * @param id the database ID of the author
	 * @throws IllegalArgumentException if id is zero or negative
	 * @see ThreadUpdate#getAuthorID()
	 */
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
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		ThreadUpdate tu2 = (ThreadUpdate) o;
		int tmpResult = _date.compareTo(tu2._date);
		return (tmpResult == 0) ? new Integer(getID()).compareTo(new Integer(tu2.getID())) : tmpResult;
	}
}