// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.assign;

import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;

/**
 * A bean to store Charter flight requests.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class CharterRequest extends DatabaseBean implements AuthoredBean, RoutePair, ViewEntry {
	
	/**
	 * Charter Request status enumeration.
	 */
	public enum RequestStatus implements EnumDescription {
		PENDING, APPROVED, REJECTED
	}

	private int _authorID;
	private int _disposalID;
	private Instant _createdOn;
	private Instant _disposedOn;
	
	private Airport _airportD;
	private Airport _airportA;
	
	private Airline _a;
	private String _eqType;
	
	private RequestStatus _status = RequestStatus.PENDING;
	
	private String _comments;
	
	@Override
	public int getAuthorID() {
		return _authorID;
	}
	
	/**
	 * Returns the request creation date.
	 * @return the creation date/time
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the request disposal date.
	 * @return the disposal date/time, or null
	 */
	public Instant getDisposedOn() {
		return _disposedOn;
	}
	
	/**
	 * Returns the database ID of the user who disposed of this request.
	 * @return the database ID, or zero if not set or auto-disposed
	 */
	public int getDisposalID() {
		return _disposalID;
	}
	
	/**
	 * Returns the request comments.
	 * @return the comments
	 */
	public String getComments() {
		return _comments;
	}
	
	/**
	 * Returns the request status.
	 * @return a RequestStatus
	 */
	public RequestStatus getStatus() {
		return _status;
	}
	
	@Override
	public Airport getAirportD() {
		return _airportD;
	}
	
	@Override
	public Airport getAirportA() {
		return _airportA;
	}
	
	/**
	 * Returns the requested Airline.
	 * @return the Airline
	 */
	public Airline getAirline() {
		return _a;
	}
	
	/**
	 * Returns the requested equipment type.
	 * @return the equipment type
	 */
	public String getEquipmentType() {
		return _eqType;
	}
	
	@Override
	public void setAuthorID(int id) {
		validateID(_authorID, id);
		_authorID = id;
	}
	
	/**
	 * Updates the request disposal ID.
	 * @param id the database ID of the user disposing the request, or zero
	 */
	public void setDisposalID(int id) {
		if (id != 0) validateID(_disposalID, id);
		_disposalID = id;
	}
	
	/**
	 * Updates the request creation date.
	 * @param dt the creation date/time
	 */
	public void setCreatedOn(Instant dt) {
		_createdOn = dt;
	}
	
	/**
	 * Updates the request disposal date.
	 * @param dt the disposal date/time
	 * @throws IllegalArgumentException if the date is before the creation date
	 */
	public void setDisposedOn(Instant dt) {
		if ((dt != null) && !dt.isAfter(_createdOn))
			throw new IllegalArgumentException("Disposal date " + dt + " cannot be before " + _createdOn);
		
		_disposedOn = dt;
	}

	/**
	 * Updates the departure Airport.
	 * @param a the Airport
	 */
	public void setAirportD(Airport a) {
		_airportD = a;
	}
	
	/**
	 * Updates the arrival Airport.
	 * @param a the Airport
	 */
	public void setAirportA(Airport a) {
		_airportA = a;
	}
	
	/**
	 * Updates the requested Airline.
	 * @param a the Airline
	 */
	public void setAirline(Airline a) {
		_a = a;
	}
	
	/**
	 * Updates the request comments.
	 * @param comments the comments
	 */
	public void setComments(String comments) {
		_comments = comments;
	}

	/**
	 * Updates the request status.
	 * @param status a RequestStatus
	 */
	public void setStatus(RequestStatus status) {
		_status = status;
	}

	/**
	 * Updates the requested equipment type.
	 * @param eqType the equipment type
	 */
	public void setEquipmentType(String eqType) {
		_eqType = eqType;
	}

	@Override
	public String getRowClassName() {
		return switch (_status) {
			case PENDING -> "opt1";
			case REJECTED -> "warn";
			default -> null;
		};
	}
}