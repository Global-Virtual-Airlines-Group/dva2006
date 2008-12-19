// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.event;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.schedule.Airport;

/**
 * A class to hold Online Event pilot signups. 
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class Signup extends DatabaseBean {

    private int _routeID;
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
     * @see Signup#getPilotID()
     */
    public Signup(int eventID, int pilotID) {
       super();
       setID(eventID);
       setPilotID(pilotID);
    }
    
    /**
     * Returns the Route for this Signup.
     * @return the Route database ID
     * @see Signup#setRouteID(int)
     */
    public int getRouteID() {
        return _routeID;
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
     * Updates the Route ID for this Signup.
     * @param id the Route database ID
     * @throws IllegalArgumentException if id is zero or negative
     * @see Signup#getRouteID()
     */
    public void setRouteID(int id) {
    	if (id < 1)
            throw new IllegalArgumentException("Database ID cannot be zero or negative");
        
        _routeID = id;
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
    
    /**
     * Compares two signups by comparing their event, route and pilot IDs.
     */
    public int compareTo(Object o) {
    	Signup s2 = (Signup) o;
    	int tmpResult = super.compareTo(s2);
    	if (tmpResult == 0)
    		tmpResult = Integer.valueOf(_routeID).compareTo(Integer.valueOf(s2._routeID));
    	if (tmpResult == 0)
    		tmpResult = new Integer(_pilotID).compareTo(new Integer(s2._pilotID));
    	
    	return tmpResult;
    }
}