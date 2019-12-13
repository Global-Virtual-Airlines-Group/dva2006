// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;
import java.time.Instant;

/**
 * A bean to store raw schedule import status.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class ImportStatus {

	private final ScheduleSource _src;
	private final Instant _importDate;
	
	private final Collection<String> _errors = new ArrayList<String>();
	private final Collection<String> _invalidEQ = new TreeSet<String>();
	private final Collection<String> _invalidAP = new TreeSet<String>();
	private final Collection<String> _invalidAL = new TreeSet<String>();
	
	/**
	 * Creates the bean.
	 * @param src the ScheduleSource 
	 * @param dt the import date/time
	 */
	public ImportStatus(ScheduleSource src, Instant dt) {
		super();
		_src = src;
		_importDate = dt;
	}

	/**
	 * Adds an invalid equipment code.
	 * @param eq the invalid code
	 */
	public void addInvalidEquipment(String eq) {
		_invalidEQ.add(eq);
	}
	
	/**
	 * Adds an invalid airport code.
	 * @param airportCode the invalid code
	 */
	public void addInvalidAirport(String airportCode) {
		_invalidAP.add(airportCode);
	}
	
	/**
	 * Adds an invalid airline code.
	 * @param airlineCode the invalid code
	 */
	public void addInvalidAirline(String airlineCode) {
		_invalidAL.add(airlineCode);
	}
	
	/**
	 * Adds an error/status message.
	 * @param msg the message
	 */
	public void addMessage(String msg) {
		_errors.add(msg);
	}
	
	/**
	 * Returns whether any invalid data was detected.
	 * @return TRUE if there are invalid Airlines, Airports, or Equipment, otherwise FALSE
	 */
	public boolean getHasInvalidData() {
		return !(_invalidAP.isEmpty() && _invalidAL.isEmpty() && _invalidEQ.isEmpty());
	}
	
	/**
	 * Returns the schedule source.
	 * @return the ScheduleSource
	 */
	public ScheduleSource getSource() {
		return _src;
	}
	
	/**
	 * Returns the date of the schedule import.
	 * @return the import date/time
	 */
	public Instant getImportDate() {
		return _importDate;
	}
	
	/**
	 * Returns any error messages from the Schedule load.
	 * @return a Collection of error messages
	 */
	public Collection<String> getErrorMessages() {
		return _errors;
	}
	
	/**
	 * Returns any invalid IATA equipment codes encountered during the import.
	 * @return a sorted Collection of IATA equipment codes
	 */
	public Collection<String> getInvalidEquipment() {
		return _invalidEQ;
	}
	
	/**
	 * Returns any invalid IATA airport codes encountered during the import.
	 * @return a sorted Collection of IATA airport codes
	 */
	public Collection<String> getInvalidAirports() {
		return _invalidAP;
	}
	
	/**
	 * Returns any invalid IATA airline codes encountered during the import.
	 * @return a sorted Collection of IATA airline codes
	 */
	public Collection<String> getInvalidAirlines() {
		return _invalidAL;
	}
	
	@Override
	public int hashCode() { 
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		return _src.name() + "-" + _importDate.getEpochSecond();
	}
}