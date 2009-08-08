// Copyright 2005, 2006, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import org.deltava.beans.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.util.*;

/**
 * A bean to store network user information.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public abstract class NetworkUser implements Comparable<NetworkUser>, ViewEntry, MarkerMapEntry {
	
	protected static final String[] RATINGS = {"", "Observer", "Student", "Senior Student", "Senior Student", "Controller",
		"Senior Controller", "Senior Controller", "Instructor", "Senior Instructor", "Senior Instructor", "Supervisor", "Administrator"};

    public static final int PILOT = 0;
    public static final int ATC = 1;
    public static final String[] CLIENTS = {"PILOT", "ATC"};
    
    private String _callSign;
    private int _id;
    
    private String _firstName;
    private String _lastName;
    private int _rating;
    
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
     * @return the user's name
     * @see NetworkUser#getFirstName()
     * @see NetworkUser#getLastName()
     */
    public String getName() {
    	StringBuilder buf = new StringBuilder(_firstName);
    	buf.append(' ');
    	buf.append(_lastName);
        return buf.toString();
    }
    
    /**
     * Returns the user's first name.
     * @return the first name
     * @see NetworkUser#setFirstName(String)
     */
    public String getFirstName() {
    	return _firstName;
    }
    
    /**
     * Returns the user's last name.
     * @return the last name
     * @see NetworkUser#setLastName(String)
     */
    public String getLastName() {
    	return _lastName;
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
     * Returns the Controller's rating code.
     * @return the rating code
     * @see NetworkUser#getRatingName()
     * @see NetworkUser#setRating(int)
     */
    public int getRating() {
       return _rating;
    }
    
    /**
     * Returns the Controller's rating name.
     * @return the rating name
     * @see Controller#getRating()
     * @see Controller#setRating(int)
     */
    public String getRatingName() {
       return RATINGS[getRating()];
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
		setPosition(StringUtils.parse(lat, 0.0d), StringUtils.parse(lon, 0.0d));
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
     * @see NetworkUser#getID()
     */
    public void setID(int id) {
        _id = Math.max(0, id);
    }
    
    /**
     * Updates the database ID of this User.
     * @param id the database ID, or 0 if this network user is not a member
     * @see NetworkUser#getPilotID()
     */
    public void setPilotID(int id) {
    	_databaseID = Math.max(0, id);
    }
    
    /**
     * Updates the user's first name.
     * @param fName the first name
     * @throws NullPointerException if fName is null
     * @see NetworkUser#getFirstName()
     * @see NetworkUser#setLastName(String)
     */
    public void setFirstName(String fName) {
    	_firstName = StringUtils.properCase(fName);
    }
    
    /**
     * Updates the user's last name.
     * @param lName the last name
     * @throws NullPointerException if lName is null
     * @see NetworkUser#getLastName()
     * @see NetworkUser#setFirstName(String)
     */
    public void setLastName(String lName) {
    	_lastName = StringUtils.properCase(lName);
    }
    
    /**
     * Updates the network user's name, stripping off the home airport.
     * @param name the user name
     * @throws NullPointerException if name is null
     * @see NetworkUser#getName()
     */
    public void setName(String name) {
    	while (name.indexOf("  ") > -1)
    		name = name.replace("  ", " ");
    	
    	int pos = name.lastIndexOf(' ');
    	boolean oneSpace = (name.indexOf(' ') == pos);
    	if (!oneSpace) {
    		if (pos == (name.length() - 5))
    			name = name.substring(0, pos);
    		if (name.endsWith(" -"))
    			name = name.substring(0, name.length() - 2);
    	}
    	
    	// Split the data
    	pos = name.lastIndexOf(' ');
    	setLastName(name.substring(pos + 1));
    	setFirstName(name.substring(0, pos));
    }
    
    /**
     * Sets the Users's rating code.
     * @param rating the rating code
     * @throws IllegalArgumentException if rating is negative or invalid
     * @see NetworkUser#getRating()
     * @see NetworkUser#getRatingName()
     */
    public void setRating(int rating) {
       if ((rating < 0) || (rating >= RATINGS.length))
             throw new IllegalArgumentException("Invalid rating - " + rating);
       
       _rating = rating;
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
     */
    public int compareTo(NetworkUser usr2) {
        return _callSign.compareTo(usr2._callSign);
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