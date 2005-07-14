package org.deltava.beans.assign;

import org.deltava.beans.Flight;
import org.deltava.beans.FlightReport;
import org.deltava.beans.ViewEntry;

import org.deltava.beans.schedule.Airline;

/**
 * A class to store assigned Flights.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AssignmentLeg extends Flight implements ViewEntry {
    
    /**
     * Completed PIREP values
     */
    public static final int[] PIREP_COMPLETE_STATUSES = { FlightReport.OK, FlightReport.REJECTED };
    
    private boolean _complete;

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

    /**
     * Returns if the assigned flight has been flown.
     * @return TRUE if the associated Flight Report is Approved or Rejected
     * @see org.deltava.beans.FlightReport#getStatus()
     */
    public boolean isComplete() {
        return _complete;
    }
    
    /**
     * Updates the status of the assigned flight
     * @param complete TRUE if the associated Flight report is Approved or Rejected 
     */
    public void setComplete(boolean complete) {
        _complete = complete;
    }
    
    /**
     * Returns the CSS class name if displayed in a view table.
     * @return null if complete, otherwise &quot;opt1&quot;
     */
    public String getRowClassName() {
       return _complete ? null : "opt1";
    }
}