// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.event;

import org.deltava.beans.DatabaseBlobBean;
import org.deltava.beans.schedule.Airport;

/**
 * A class to store information about a Flight Plan for an Online Event.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FlightPlan extends DatabaseBlobBean {

    public static final int MSFS = 0;
    public static final int FSBUILD = 1;
    public static final int FSNAV = 2;
    public static final int SB3 = 3;
    
    /**
     * File extensions for the flight plan types.
     */
    public static final String[] PLAN_EXT = {"pln", "fsp", "fsn", "sfp"};
    
    /**
     * Flight plan type names.
     */
    public static final String[] PLAN_TYPE = {"Flight Simulator", "FSBuild", "FSNavigator", "Squawkbox 3.x"};
    
    private int _type;
    
    private Airport _airportD;
    private Airport _airportA;
    
    /**
     * Creates a new Flight Plan object.
     */
    public FlightPlan(int type) {
        super();
        setType(type);
    }
    
    /**
     * Returns the flight plan type code.
     * @return the plan type code
     * @see FlightPlan#PLAN_TYPE
     * @see FlightPlan#setType(int)
     * @see FlightPlan#setType(String)
     * @see FlightPlan#getTypeName()
     */
    public int getType() {
        return _type;
    }
    
    /**
     * Returns the flight plan type name.
     * @return the type name
     * @see FlightPlan#getType()
     */
    public String getTypeName() {
    	return PLAN_TYPE[getType()];
    }

    /**
     * Returns the arrival Airport for this Flight Plan.
     * @return the Arrival airport object
     * @see FlightPlan#setAirportA(Airport)
     * @see FlightPlan#getAirportD()
     */
    public Airport getAirportA() {
        return _airportA;
    }
    
    /**
     * Returns the departure Airport for this Flight Plan.
     * @return the Departure airport object
     * @see FlightPlan#setAirportD(Airport)
     * @see FlightPlan#getAirportA()
     */
    public Airport getAirportD() {
        return _airportD;
    }
    
    /**
     * Computes the name of the flight plan file. This is done by using the code of th departure and arrival airports,
     * separated by a dash, with the proper extension of the flight plan type. Because we use getCode() the user
     * preferences can control IATA or ICAO code.
     * @return the flight plan filename
     * @throws NullPointerException if either airport is not set
     * @see Airport#getIATA()
     * @see FlightPlan#PLAN_EXT
     */
    public String getFileName() {
        StringBuilder buf = new StringBuilder(_airportD.getIATA());
        buf.append('-');
        buf.append(_airportA.getIATA());
        buf.append('.');
        buf.append(PLAN_EXT[_type]);
        return buf.toString();
    }

    /**
     * Updates the Arrival airport.
     * @param a the new arrival Airport object
     * @see FlightPlan#getAirportA()
     * @see FlightPlan#setAirportD(Airport)
     */
    public void setAirportA(Airport a) {
        _airportA = a;
    }
    
    /**
     * Updates the Departure airport.
     * @param a the new departure Airport object
     * @see FlightPlan#getAirportD()
     * @see FlightPlan#setAirportA(Airport)
     */
    public void setAirportD(Airport a) {
        _airportD = a;
    }
    
    /**
     * Updates the Flight Plan type with a code. Note that this method does not validate the contents of the buffer
     * data to ensure that it conforms to the specified type.
     * @param type the type code
     * @throws IllegalArgumentException if the type code is not found in PLAN_TYPES
     * @see FlightPlan#setType(String)
     * @see FlightPlan#getType()
     */
    public void setType(int type) {
        if ((type < 0) || (type >= PLAN_TYPE.length))
            throw new IllegalArgumentException("Invalid Flight Plan Type - " + type);
        
        _type = type;
    }
    
    /**
     * Updates the Flight Plan type.  Note that this method does not validate the contents of the buffer
     * data to ensure that it conforms to the specified type.
     * @param type the Flight Plan type
     * @throws IllegalArgumentException if the type is not found in PLAN_TYPES
     * @see FlightPlan#setType(int)
     * @see FlightPlan#getType()
     */
    public void setType(String type) {
        for (int x = 0; x < PLAN_TYPE.length; x++) {
            if (PLAN_TYPE[x].equals(type)) {
                setType(x);
                return;
            }
        }
        
        // If we got this far, it's an unknown type
        throw new IllegalArgumentException("Invalid Flight Plan Type - " + type);
    }
    
    /**
     * Returns the hash code of the ID and filename.
     */
    public int hashCode() {
    	StringBuilder buf = new StringBuilder(String.valueOf(getID()));
    	buf.append(getFileName());
    	return buf.toString().hashCode();
    }
    
    /**
     * Compares two flight plans by comparing their database ID and file names. 
     */
    public int compareTo(Object o) {
    	FlightPlan fp2 = (FlightPlan) o;
    	int tmpResult = super.compareTo(fp2);
    	return (tmpResult == 0) ? getFileName().compareTo(fp2.getFileName()) : tmpResult;
    }
    
    public boolean equals(Object o) {
    	return (o instanceof FlightPlan) ? (compareTo(o) == 0) : false;
    }
}