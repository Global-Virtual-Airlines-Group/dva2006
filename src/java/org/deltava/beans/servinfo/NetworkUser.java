// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import org.deltava.beans.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.util.*;

/**
 * A bean to store network user information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class NetworkUser implements java.io.Serializable, Comparable, ViewEntry, MapEntry {

    public static final int PILOT = 0;
    public static final int ATC = 1;
    public static final String[] CLIENTS = {"PILOT", "ATC"};
    
    private String _callSign;
    private int _id;
    private String _name;
    
    private int _databaseID;
	protected GeoPosition _position;
    
    /**
     * Initializes the bean with a given network ID.
     * @param id the user ID
     * @throws IllegalArgumentException if id is negative
     * @see NetworkUser#setID(int)
     */
    public NetworkUser(int id) {
        super();
        setID(id);
    }

    public abstract int getType();
    
    /**
     * Returns the user's callsign.
     * @return the callsign
     * @see NetworkUser#setCallsign(String)
     */
    public String getCallsign() {
        return _callSign;
    }
    
    /**
     * Returns the Database ID of this network user.
     * @return the database ID, or 0 if this user is not a member
     * @see NetworkUser#setPilotID(int)
     */
    public int getPilotID() {
    	return _databaseID;
    }
    
    /**
     * Returns the user's network ID.
     * @return the network ID
     * @see NetworkUser#setID(int)
     */
    public int getID() {
        return _id;
    }
    
    /**
     * Returns the user's full name.
     * @return the name
     * @see NetworkUser#setName(String)
     */
    public String getName() {
        return _name;
    }
    
	/**
	 * Returns the User's current latitude.
	 * @return the latitude in degrees
	 * @see NetworkUser#getLongitude()
	 * @see NetworkUser#getPosition()
	 * @see NetworkUser#setPosition(double, double)
	 * @see NetworkUser#setPosition(String, String)
	 */
	public final double getLatitude() {
		return _position.getLatitude();
	}
	
	/**
	 * Returns the User's current longitude.
	 * @return the longitude in degrees
	 * @see NetworkUser#getLatitude() 
	 * @see NetworkUser#getPosition()
	 * @see NetworkUser#setPosition(double, double)
	 * @see NetworkUser#setPosition(String, String)
	 */
	public final double getLongitude() {
		return _position.getLongitude();
	}

	/**
	 * Returns the User's current position.
	 * @return a GeoPosition bean containing latitude and longitude
	 * @see NetworkUser#getLatitude()
	 * @see NetworkUser#getLongitude()
	 * @see NetworkUser#setPosition(double, double)
	 * @see NetworkUser#setPosition(String, String)
	 */
	public GeoLocation getPosition() {
		return _position;
	}

	/**
	 * Updates the User's position. This has a ServInfo hack where latitudes of -290 to -350 are mapped to
	 * latitudes in the Northern Hemisphere.
	 * @param lat the latitude in degrees
	 * @param lon the longitude in degrees
	 * @see NetworkUser#setPosition(String, String)
	 * @see NetworkUser#getPosition()
	 */
	public void setPosition(double lat, double lon) {
		_position = new GeoPosition(GeoUtils.normalize(lat, lon));
	}

	/**
	 * Updates the User's position.
	 * @param lat a String containing the latitude
	 * @param lon a String containing the longitude
	 * @see NetworkUser#setPosition(double, double)
	 * @see NetworkUser#getPosition()
	 */
	public void setPosition(String lat, String lon) {
		try {
			setPosition(Double.parseDouble(lat), Double.parseDouble(lon));
		} catch (NumberFormatException nfe) {
			_position = new GeoPosition(0, 0);
		}
	}
	
    /**
     * Updates the user's callsign.
     * @param cs the callsign
     * @throws NullPointerException if cs is null
     * @see NetworkUser#getCallsign()
     */
    public void setCallsign(String cs) {
        _callSign = cs.toUpperCase();
    }
    
    /**
     * Updates the user's network ID.
     * @param id the network ID
     * @throws IllegalArgumentException if id is negative
     * @see NetworkUser#getID()
     */
    public void setID(int id) {
        if (id < 0)
            throw new IllegalArgumentException("Invalid network ID - " + id);
        
        _id = id;
    }
    
    /**
     * Updates the database ID of this User.
     * @param id the database ID, or 0 if this network user is not a member
     * @throws IllegalArgumentException if id is negative
     * @see NetworkUser#getPilotID()
     */
    public void setPilotID(int id) {
    	if (id < 0)
    		throw new IllegalArgumentException("Invalid Pilot ID - " + id);
    	
    	_databaseID = id;
    }
    
    /**
     * Updates the network user's name, stripping off the home airport.
     * @param name the user name
     * @see NetworkUser#getName()
     */
    public void setName(String name) {
    	if (name.lastIndexOf(' ') != -1)
    		name = name.substring(0, name.lastIndexOf(' '));
    	
        _name = StringUtils.properCase(name);
    }
    
    /**
     * Returns the user type from a text string obtained from a ServInfo data feed.
     * @param userType the user Type (PILOT or ATC)
     * @return the user type code
     */
    public static int getType(String userType) {
       int result = StringUtils.arrayIndexOf(CLIENTS, userType);
       return (result == -1) ? PILOT : result;
    }
    
    /**
     * Compares this user to another network user by comparing the Network IDs.
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object o2) {
        NetworkUser usr2 = (NetworkUser) o2;
        return _callSign.compareTo(usr2.getCallsign());
    }
    
    /**
     * Checks equality by comparing callsigns.
     */
    public final boolean equals(Object o2) {
    	return (o2 instanceof NetworkUser) ? _callSign.equals(((NetworkUser) o2).getCallsign()) : false;
    }
    
    /**
     * Returns the callsign's hash code.
     */
    public final int hashCode() {
    	return _callSign.hashCode();
    }
    
    /**
     * Returns the row class name, which is set if the Network user is a Pilot.
     */
    public String getRowClassName() {
    	return (_databaseID == 0) ? null : "opt2";
    }
}