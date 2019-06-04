// Copyright 2005, 2006, 2007, 2010, 2011, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.hr;

import java.util.*;
import java.time.Instant;

import org.deltava.beans.*;

/**
 * A class to store Equipment Program transfer requests. Since a checkride may be required for switches to
 * additional equipment programs, this bean may also be used to track check ride workflows.
 * @author Luke
 * @version 8.6
 * @since 1.0
 */

public class TransferRequest extends DatabaseBean implements ViewEntry {

	private final SortedSet<Integer> _checkRideIDs = new TreeSet<Integer>();
	private boolean _crSubmitted;
	
	private Simulator _sim = Simulator.UNKNOWN;
	
	private TransferStatus _status;
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
	 * @see TransferRequest#setStatus(TransferStatus)
	 */
	public TransferStatus getStatus() {
		return _status;
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
	 * Returns the Pilot's preferred Simulator.
	 * @return a Simulator enum
	 * @see TransferRequest#setSimulator(Simulator)
	 */
	public Simulator getSimulator() {
		return _sim;
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
	 * @param status the new TransferStatus
	 * @see TransferRequest#getStatus()
	 */
	public void setStatus(TransferStatus status) {
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
	 * Updates the Pilot's preferred simulator.
	 * @param sim a Simulator
	 * @see TransferRequest#getSimulator()
	 */
	public void setSimulator(Simulator sim) {
		_sim = sim;
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
		if (_status == TransferStatus.ASSIGNED)
			return _crSubmitted ? "opt3" : "opt1";
			
		return (_status == TransferStatus.PENDING) ? "opt2" : null;
	}
}