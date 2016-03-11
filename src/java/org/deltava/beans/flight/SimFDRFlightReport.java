// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import org.deltava.beans.schedule.Airline;

/**
 * A bean to store simFDR Flight Reports. 
 * @author Luke
 * @version 7.0
 * @since 7.0
 */

public class SimFDRFlightReport extends ACARSFlightReport {

    /**
     * Creates a new ACARS Flight Report object with a given flight.
     * @param a the Airline
     * @param flightNumber the Flight Number
     * @param leg the Leg Number
     * @throws NullPointerException if the Airline Code is null
     * @throws IllegalArgumentException if the Flight Report is zero or negative
     * @throws IllegalArgumentException if the Leg is less than 1 or greater than 8
     */
	public SimFDRFlightReport(Airline a, int flightNumber, int leg) {
		super(a, flightNumber, leg);
	}

	@Override
	public Recorder getFDR() {
		return Recorder.SIMFDR;
	}
}