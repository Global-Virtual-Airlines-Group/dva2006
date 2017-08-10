// Copyright 2005, 2006, 2007, 2010, 2011, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.hr;

import java.util.*;
import java.time.Instant;

import org.deltava.beans.*;

/**
 * A class to store Equipment Program transfer requests. Since a checkride may be required for switches to
 * additional equipment programs, this bean may also be used to track check ride workflows.
 * @author Luke
 * @version 7.5
 * @since 1.0
 */

public class TransferRequest extends DatabaseBean implements ViewEntry {

	public static final int PENDING = 1;
	public static final int ASSIGNED = 2;
	public static final int OK = 3;

	private static final String[] STATUS = { "New", "Pending Check Ride", "Check Ride Assigned", "Complete" };

	private final SortedSet<Integer> _checkRideIDs = new TreeSet<Integer>();
	private boolean _crSubmitted;
	
	private int _status;
	private final String _eqType;
	private String _acType;
	private Instant _date;
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
	 * Returns the preferred aircraft type for the Check Ride.
	 * @return the aircraft type
	 */
	public String getAircraftType() {
		return _acType;
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
	 * @see TransferRequest#setDate(Instant)
	 */
	public Instant getDate() {
		return _date;
	}

	/**
	 * Returns the database ID of the latest assigned Check Ride.
	 * @return the database ID of the assigned Check Ride, or zero if none assigned
	 * @see TransferRequest#getCheckRideIDs()
	 * @see TransferRequest#addCheckRideID(int)
	 */
	public int getLatestCheckRideID() {
		return _checkRideIDs.isEmpty() ? 0 : _checkRideIDs.last().intValue();
	}
	
	/**
	 * Returns the database IDs of all check rides associated with this Transfer Request.
	 * @return a Collection of database IDs
	 * @see TransferRequest#getLatestCheckRideID()
	 * @see TransferRequest#addCheckRideID(int)
	 */
	public Collection<Integer> getCheckRideIDs() {
		return _checkRideIDs;
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
	 * Returns whether the associated Check Ride has been submitted.
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
	public void setDate(Instant dt) {
		_date = dt;
	}
	
	/**
	 * Updates the preferr aircraft type to use for the check ride.
	 * @param acType the aircraft type
	 */
	public void setAircraftType(String acType) {
		_acType = acType;
	}

	/**
	 * Updates the database ID of the assigned Check Ride.
	 * @param id the Check Ride's database ID
	 * @throws IllegalArgumentException if id is negative
	 * @see DatabaseBean#validateID(int, int)
	 */
	public void addCheckRideID(int id) {
		validateID(0, id);
		_checkRideIDs.add(Integer.valueOf(id));
	}
	
	/**
	 * Updates whether the associated Check Ride has been submitted
	 * @param isSubmitted TRUE if the Check Ride was submitted/graded, otherwise FALSE
	 * @throws IllegalStateException if no Check Ride ID was supplied
	 * @see TransferRequest#getCheckRideSubmitted()
	 */
	public void setCheckRideSubmitted(boolean isSubmitted) {
		if (isSubmitted && _checkRideIDs.isEmpty())
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
	@Override
	public int compareTo(Object o2) {
		TransferRequest tr2 = (TransferRequest) o2;
		return _date.compareTo(tr2.getDate());
	}

	/**
	 * Returns the CSS class name used to display this in a view table.
	 * @return the CSS class name
	 */
	@Override
	public String getRowClassName() {
		if (_status == ASSIGNED)
			return _crSubmitted ? "opt3" : "opt1";
			
		return (_status == 1) ? "opt2" : null;
	}
}