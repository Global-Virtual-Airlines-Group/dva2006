// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.io.Serializable;

import org.deltava.beans.ViewEntry;
import org.deltava.util.StringUtils;

/**
 * A bean to store network user information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class NetworkUser implements Comparable, Serializable, ViewEntry {

    public static final int PILOT = 0;
    public static final int ATC = 1;
    public static final String[] CLIENTS = {"PILOT", "ATC"};
    
    private String _callSign;
    private int _id;
    private String _name;
    
    private int _databaseID;
    
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
     * @throws IllegalArgumentException if id is zero or negative
     * @see NetworkUser#getID()
     */
    public void setID(int id) {
        if (id < 1)
            throw new IllegalArgumentException("Invalid network ID - " + id);
        
        _id = id;
    }
    
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
        _name = StringUtils.properCase(name.substring(0, name.lastIndexOf(' ')));
    }
    
    public static int getType(String userType) {
        for (int x = 0; x < NetworkUser.CLIENTS.length; x++) {
            if (NetworkUser.CLIENTS[x].equals(userType))
                return x;
        }
        
        return NetworkUser.PILOT;
    }
    
    /**
     * Compares this user to another network user by comparing the Network IDs.
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object o2) {
        NetworkUser usr2 = (NetworkUser) o2;
        return _callSign.compareTo(usr2.getCallsign());
    }
    
    public final boolean equals(Object o2) {
    	return (o2 instanceof NetworkUser) ? _callSign.equals(((NetworkUser) o2).getCallsign()) : false;
    }
    
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