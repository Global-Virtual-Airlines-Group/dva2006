// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.system;

import java.util.Date;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.ViewEntry;

/**
 * A class to store Equipment Program transfer requests. Since a checkride may be required for switches to
 * additional equipment programs, this bean may also be used to track check ride workflows.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TransferRequest extends DatabaseBean implements Comparable, ViewEntry {

	public static final int NEW = 0;
	public static final int PENDING = 1;
	public static final int OK = 2;

	public static final String[] STATUS = { "New", "Pending Check Ride", "Complete" };

	private int _checkRideID;
	private int _status;
	private String _eqType;
	private Date _date;
	private boolean _ratingOnly;

	/**
	 * Create a new Transfer Request.
	 * @param pilotID the Pilot's Database ID
	 * @param eqType the Equipment Program to transfer <i>to</i>
	 * @throws IllegalArgumentException if pilotID is zero or negative
	 * @throws NullPointerException if eqType is null
	 */
	public TransferRequest(int pilotID, String eqType) {
		super();
		setID(pilotID);
		_eqType = eqType.trim();
	}

	/**
	 * Returns the Equipment Program for this Transfer Request.
	 * @return the equipment program name
	 */
	public String getEquipmentType() {
		return _eqType;
	}

	/**
	 * Returns the creation date of this Transfer Request.
	 * @return the date/time this request was created
	 * @see TransferRequest#setDate(Date)
	 */
	public Date getDate() {
		return _date;
	}

	/**
	 * Returns the database ID of an assigned Check Ride.
	 * @return the database ID of the assigned Check Ride, or zero if none assigned
	 * @see TransferRequest#setCheckRideID(int)
	 */
	public int getCheckRideID() {
		return _checkRideID;
	}

	/**
	 * Returns the status of this Transfer Request.
	 * @return the status code
	 * @see TransferRequest#setStatus(int)
	 * @see TransferRequest#getStatusName()
	 */
	public int getStatus() {
		return _status;
	}

	/**
	 * Returns the status of this Transfer Request.
	 * @return the status name
	 * @see TransferRequest#getStatus()
	 * @see TransferRequest#setStatus(int)
	 */
	public String getStatusName() {
		return STATUS[getStatus()];
	}
	
	/**
	 * Returns if this Transfer Request is for an additional rating only.
	 * @return TRUE if the rating is requested, not a program switch, otherwise FALSE
	 * @see TransferRequest#setRatingOnly(boolean)
	 */
	public boolean getRatingOnly() {
		return _ratingOnly;
	}

	/**
	 * Updates the creation date of this Transfer Request.
	 * @param dt the date/time this request was created
	 * @see TransferRequest#getDate()
	 */
	public void setDate(Date dt) {
		_date = dt;
	}

	/**
	 * Updates the database ID of the assigned Check Ride.
	 * @param id the Check Ride's database ID
	 * @throws IllegalArgumentException if id is negative
	 */
	public void setCheckRideID(int id) {
		if (id != 0) {
			validateID(_checkRideID, id);
			_checkRideID = id;
		}
	}

	/**
	 * Updates the status of this Transfer Request.
	 * @param status the new status code
	 * @throws IllegalArgumentException if status is negative or invalid
	 * @see TransferRequest#getStatus()
	 * @see TransferRequest#getStatusName()
	 */
	public void setStatus(int status) {
		if ((status < 0) || (status >= STATUS.length))
			throw new IllegalArgumentException("Invalid Transfer Request status - " + status);

		_status = status;
	}
	
	/**
	 * Updates if this Transfer Request is for an additional rating, instead of a program switch.
	 * @param ratingOnly TRUE if an additional rating only, otherwise FALSE
	 * @see TransferRequest#getRatingOnly()
	 */
	public void setRatingOnly(boolean ratingOnly) {
		_ratingOnly = ratingOnly;
	}

	/**
	 * Compares two Transfer Requests by comparing their dates.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o2) {
		TransferRequest tr2 = (TransferRequest) o2;
		return _date.compareTo(tr2.getDate());
	}

	/**
	 * Returns the CSS class name used to display this in a view table.
	 * @return the CSS class name
	 */
	public String getRowClassName() {
		String[] ROW_CLASSES = { null, "opt2", null };
		return ROW_CLASSES[getStatus()];
	}
}