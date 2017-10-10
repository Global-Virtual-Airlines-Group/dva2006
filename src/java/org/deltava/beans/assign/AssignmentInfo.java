// Copyright 2004, 2005, 2008, 2009, 2010, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.assign;

import java.util.*;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.RoutePair;

/**
 * A class to store Flight Assignments.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

public class AssignmentInfo extends DatabaseBean implements ViewEntry {
	
    public static final int AVAILABLE = 0;
    public static final int RESERVED = 1;
    public static final int COMPLETE = 2;
    
    /**
     * Assignment status names.
     */
    public static final String[] STATUS = {"Available", "Assigned", "Complete"};

    private final String _eqType;
    private int _pilotID;
    private int _eventID;
    private int _status;
    
    private Instant _assignedOn;
    private Instant _completedOn;
    
    private boolean _random;
    private boolean _repeating;
    private boolean _purge;
    
    private final Collection<AssignmentLeg> _assignments = new LinkedHashSet<AssignmentLeg>();
    private final Collection<FlightReport> _flights = new LinkedHashSet<FlightReport>();
    
    /**
     * Creates a new Flight Assignment for a particular Equipment Type.
     * @param eqType the equipment type code
     * @throws NullPointerException if eqType is null
     */
    public AssignmentInfo(String eqType) {
        super();
        _eqType = eqType.trim();
    }
    
    /**
     * Returns the individual Legs for this Assignment.
     * @return a List of AssignmentInfo beans
     * @see AssignmentInfo#addAssignment(AssignmentLeg)
     */
    public Collection<AssignmentLeg> getAssignments() {
        return _assignments;
    }
    
    /**
     * Returns the Flight Reports linked to this Assignment.
     * @return a List of FlightReport beans
     * @see AssignmentInfo#addFlight(FlightReport)
     */
    public Collection<FlightReport> getFlights() {
        return _flights;
    }
    
    /**
     * Returns the equipment type for this assignment.
     * @return the equipment type
     */
    public String getEquipmentType() {
        return _eqType;
    }
    
    /**
     * Returns the ID of the associated Online Event.
     * @return the Database ID of the associated Event, or zero if not linked to an event
     * @see AssignmentInfo#setEventID(int)
     */
    public int getEventID() {
        return _eventID;
    }
    
    /**
     * Returns the Pilot Database ID for this Flight Assignment.
     * @return the Database ID of the Assigned Pilot, or zero if unassigned
     * @see AssignmentInfo#setPilotID(int)
     * @see AssignmentInfo#setPilotID(Person)
     */
    public int getPilotID() {
        return _pilotID;
    }

    /**
     * Returns the status of this Assignment.
     * @return the status code
     * @see AssignmentInfo#setStatus(int)
     * @see AssignmentInfo#setStatus(String)
     */
    public int getStatus() {
        return _status;
    }
    
    /**
     * Returns the date/time this Assignment was assigned to the Pilot.
     * @return the date/time assignment was made
     * @see AssignmentInfo#setAssignDate(Instant)
     */
    public Instant getAssignDate() {
        return _assignedOn;
    }
    
    /**
     * Returns the date/time this Assignment was completed by the Pilot.
     * @return the date/time assignment was completed
     * @see AssignmentInfo#setCompletionDate(Instant)
     */
    public Instant getCompletionDate() {
        return _completedOn;
    }
    
    /**
     * Determines if all flights in this assignment are complete.
     * @return TRUE if all assignment flights are complete, otherwise FALS
     */
    public boolean isComplete() {
    	return _flights.stream().allMatch(fr -> ((fr.getStatus() == FlightReport.OK) || (fr.getStatus() == FlightReport.REJECTED)));
    }
    
    /**
     * Returns if this Assignment was randomly generated.
     * @return TRUE if the assignment was randomly created, otherwise FALSE
     * @see AssignmentInfo#setRandom(boolean)
     */
    public boolean isRandom() {
        return _random;
    }
    
    /**
     * Returns if this Assignment should be made available again when complete.
     * @return TRUE if the assignment is auto-repeating, otherwise FALSE
     * @see AssignmentInfo#setRepeating(boolean)
     */
    public boolean isRepeating() {
        return _repeating;
    }
    
    /**
     * Returns if this Assignment should be automatically purged on a Schedule reload.
     * @return TRUE if the assignment should be purged, otherwise FALSE
     * @see AssignmentInfo#setPurgeable(boolean)
     */
    public boolean isPurgeable() {
        return _purge;
    }
    
    /**
     * Adds an Assigned Flight to this Assignment.
     * @param a the assigned flight
     */
    public void addAssignment(AssignmentLeg a) {
        _assignments.add(a);
    }
    
    /**
     * Adds a Flight Report to this Assignment. This will update the Pilot ID for this Assignment if not set.
     * @param fr the flight report
     */
    public void addFlight(FlightReport fr) {
    	// Link to the pilot if one assigned
    	if (_pilotID != 0)
    		fr.setDatabaseID(DatabaseID.PILOT, _pilotID);
    	
        _flights.add(fr);
    }
    
    /**
     * Sets the associated Online Event for this Assignment.
     * @param id the Event Database ID
     * @throws IllegalArgumentException if id is negative
     */
    public void setEventID(int id) {
       if (id != 0) {
          validateID(_eventID, id);
          _eventID = id;
       }
    }
    
    /**
     * Updates the associated Pilot for this Assignment.
     * @param p the Pilot bean
     * @see AssignmentInfo#setPilotID(int)
     * @see AssignmentInfo#getPilotID()
     */
    public void setPilotID(Person p) {
        _pilotID = p.getID();
    }
    
    /**
     * Updates the associated Pilot for this Assignment.
     * @param id the Pilot Database ID
     * @throws IllegalArgumentException if id is negative
     * @see AssignmentInfo#setPilotID(Person)
     * @see AssignmentInfo#getPilotID()
     */
    public void setPilotID(int id) {
       if (id != 0) {
          validateID(_pilotID, id);
          _pilotID = id;
       }
    }
    
    /**
     * Updates the status of this Flight Assignment.
     * @param status the status code
     * @see AssignmentInfo#setStatus(String)
     * @see AssignmentInfo#getStatus()
     */
    public void setStatus(int status) {
        _status = status;
    }
    
    /**
     * Updates the status of this Flight Assignment.
     * @param status the status type name
     * @throws IllegalArgumentException if an invalid type name
     * @see AssignmentInfo#setStatus(int)
     * @see AssignmentInfo#getStatus()
     */
    public void setStatus(String status) {
        for (int x = 0; x < STATUS.length; x++) {
            if (STATUS[x].equals(status)) {
                _status = x;
                return;
            }
        }
        
        // If we got this far, it wasn't matched
        throw new IllegalArgumentException("Invalid Flight Assignment status - " + status);
    }
    
    /**
     * Marks this Assignment as randomly generated.
     * @param random TRUE if generated from Find a Flight, otherwise FALSE
     * @see AssignmentInfo#isRandom()
     */
    public void setRandom(boolean random) {
        _random = random;
    }
    
    /**
     * Marks this Assignment as repeating (automatically regenerated when completed).
     * @param repeating TRUE if automatically repeating, otherwise FALSE
     * @see AssignmentInfo#isRepeating()
     */
    public void setRepeating(boolean repeating) {
        _repeating = repeating;
    }
    
    /**
     * Marks this Assignment as purgeable when the Schedule database is updated.
     * @param canPurge TRUE if the Assignment should be purged on a Schedule update.
     * @see AssignmentInfo#isPurgeable()
     */
    public void setPurgeable(boolean canPurge) {
        _purge = canPurge;
    }
    
    /**
     * Updates the Date this Assignment was assigned to a Pilot.
     * @param dt the date/time this Assignment was assigned
     * @see AssignmentInfo#getAssignDate()
     */
    public void setAssignDate(Instant dt) {
        _assignedOn = dt;
    }
    
    /**
     * Updates the Date this Assignment was completed.
     * @param dt the date/time this Assignment was completed
     * @see AssignmentInfo#getCompletionDate()
     */
    public void setCompletionDate(Instant dt) {
        _completedOn = dt;
    }
    
    /**
     * Removes an unflown leg from this assignment.
     * @param rp the RoutePair to remove
     */
    @SuppressWarnings("unlikely-arg-type")
	public void remove(RoutePair rp) {
    	FlightReport dfr  = null;
    	for (Iterator<FlightReport> i = _flights.iterator(); i.hasNext(); ) {
    		FlightReport fr = i.next();
    		if ((fr.getStatus() == FlightReport.DRAFT) && fr.matches(rp)) {
    			dfr = fr;
    			i.remove();
    		}
    	}
    	
    	for (Iterator<AssignmentLeg> i = _assignments.iterator(); i.hasNext(); ) {
    		AssignmentLeg al = i.next();
    		if (al.matches(dfr) && al.equals(dfr))
    			i.remove();
    	}
    }
    
    /**
     * Returns the CSS class name for this assignment when displayed in a view.
     * @return the CSS class name
     */
    @Override
    public String getRowClassName() {
    	final String[] ROW_CLASSES = {null, "opt2", "opt3"};
    	return ROW_CLASSES[_status];
    }
}