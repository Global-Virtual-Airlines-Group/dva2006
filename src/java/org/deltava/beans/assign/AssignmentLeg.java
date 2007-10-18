// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.assign;

import org.deltava.beans.Flight;

import org.deltava.beans.schedule.Airline;

/**
 * A class to store assigned Flights.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AssignmentLeg extends Flight {
    
    /**
     * Creates a new Assigned Flight.
     * @param a the Airline
     * @param fNumber the flight number
     * @param leg the leg number
     * @see Flight#Flight(Airline, int, int)
     */
    public AssignmentLeg(Airline a, int fNumber, int leg) {
        super(a, fNumber, leg);
    }
    
    /**
     * Creates a new Assigned Flight from an existing Flight (usually a ScheduleEntry).
     * @param f the existing Flight
     */
    public AssignmentLeg(Flight f) {
    	super(f.getAirline(), f.getFlightNumber(), f.getLeg());
    	setEquipmentType(f.getEquipmentType());
    	setAirportD(f.getAirportD());
    	setAirportA(f.getAirportA());
    }

    /**
     * DISABLED property.
     * @throws UnsupportedOperationException
     */
    public final int getLength() {
        throw new UnsupportedOperationException();
    }
}