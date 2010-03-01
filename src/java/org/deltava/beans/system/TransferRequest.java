// Copyright 2005, 2006, 2007, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.Date;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.ViewEntry;

/**
 * A class to store Equipment Program transfer requests. Since a checkride may be required for switches to
 * additional equipment programs, this bean may also be used to track check ride workflows.
 * @author Luke
 * @version 3.0
 * @since 1.0
 */

public class TransferRequest extends DatabaseBean implements ViewEntry {

	public static final int PENDING = 1;
	public static final int ASSIGNED = 2;
	public static final int OK = 3;

	private static final String[] STATUS = { "New", "Pending Check Ride", "Check Ride Assigned", "Complete" };

	private int _checkRideID;
	private boolean _crSubmitted;
	
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
	 * Returns wether the associated Check Ride has been submitted.
	 * @return TRUE if the Check Ride was submitted or graded, otherwise FALSE
	 * @see TransferRequest#setCheckRideSubmitted(boolean)
	 */
	public boolean getCheckRideSubmitted() {
		return _crSubmitted;
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
	 * @see DatabaseBean#validateID(int, int)
	 */
	public void setCheckRideID(int id) {
		if (id != 0) {
			validateID((_status == PENDING) ? 0 : _checkRideID, id);
			_checkRideID = id;
		}
	}
	
	/**
	 * Updates wether the associated Check Ride has been submitted
	 * @param isSubmitted TRUE if the Check Ride was submitted/graded, otherwise FALSE
	 * @throws IllegalStateException if no Check Ride ID was supplied
	 * @see TransferRequest#getCheckRideSubmitted()
	 */
	public void setCheckRideSubmitted(boolean isSubmitted) {
		if (isSubmitted && (_checkRideID == 0))
			throw new IllegalStateException("No Check Ride ID");
		
		_crSubmitted = isSubmitted;
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
		if (getStatus() == ASSIGNED)
			return _crSubmitted ? "opt3" : "opt1";
			
		String[] ROW_CLASSES = { null, "opt2", null, null };
		return ROW_CLASSES[getStatus()];
	}
}