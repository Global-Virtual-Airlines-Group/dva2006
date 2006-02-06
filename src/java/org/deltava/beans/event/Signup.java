// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.event;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.schedule.Airport;

/**
 * A class to hold Online Event pilot signups. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Signup implements java.io.Serializable {

    private int _eventID;
    private int _pilotID;
    private String _eqType;
    private String _remarks;
    
    private Airport _airportD;
    private Airport _airportA;
    
    /**
     * Creates a new Event Signup for a Pilot.
     * @param eventID the Event database ID
     * @param pilotID the Pilot's database ID
     * @throws IllegalArgumentException if eventID or pilotID are zero or negative
     * @see Signup#getEventID()
     * @see Signup#getPilotID()
     */
    public Signup(int eventID, int pilotID) {
       super();
       setEventID(eventID);
       setPilotID(pilotID);
    }
    
    /**
     * Returns the Event for this Signup.
     * @return the Event database ID
     * @see Signup#setEventID(int)
     */
    public int getEventID() {
        return _eventID;
    }
    
    /**
     * Returns the requested equipment type.
     * @return the equipment type
     * @see Signup#setEquipmentType(String)
     */
    public String getEquipmentType() {
        return _eqType;
    }
    
    /**
     * Returns the destination Airport for this signed-up route.
     * @return the Airport bean
     * @see Signup#getAirportD()
     * @see Signup#setAirportA(Airport)
     */
    public Airport getAirportA() {
       return _airportA;
    }
    
    /**
     * Returns the origin Airport for this signed-up route.
     * @return the Airport bean
     * @see Signup#getAirportA()
     * @see Signup#setAirportD(Airport)
     */
    public Airport getAirportD() {
       return _airportD;
    }
    
    /**
     * Returns the database ID of the signed-up pilot.
     * @return the Pilot's database ID
     * @see Signup#setPilotID(int)
     */
    public int getPilotID() {
        return _pilotID;
    }
    
    /**
     * Returns any Pilot remarks about this Signup.
     * @return the remarks
     * @see Signup#setRemarks(String)
     */
    public String getRemarks() {
        return _remarks;
    }
    
    /**
     * Updates the Event ID for this Signup.
     * @param id the Event database ID
     * @throws IllegalArgumentException if id is zero or negative
     * @see Signup#getEventID()
     */
    public void setEventID(int id) {
        DatabaseBean.validateID(_eventID, id);
        _eventID = id;
    }
    
    /**
     * Updates the Pilot ID for this Signup.
     * @param id the Pilot database ID
     * @throws IllegalArgumentException if id is zero or negative
     * @see Signup#getPilotID()
     */
    public void setPilotID(int id) {
       DatabaseBean.validateID(_pilotID, id);
       _pilotID = id;
    }
    
    /**
     * Updates the destination Airport.
     * @param a the Airport bean
     * @see Signup#setAirportD(Airport)
     * @see Signup#getAirportA()
     */
    public void setAirportA(Airport a) {
       _airportA = a;
    }
    
    /**
     * Updates the origin Airport
     * @param a the Airport bean
     * @see Signup#setAirportA(Airport)
     * @see Signup#getAirportD()
     */
    public void setAirportD(Airport a) {
       _airportD = a;
    }
    
    /**
     * Updates the requested equipment type
     * @param eqType the aircraft type
     * @see Signup#getEquipmentType()
     */
    public void setEquipmentType(String eqType) {
        _eqType = eqType;
    }
    
    /**
     * Updates the Pilot's remarks.
     * @param remarks the remarks
     * @see Signup#getRemarks()
     */
    public void setRemarks(String remarks) {
        _remarks = remarks;
    }
}