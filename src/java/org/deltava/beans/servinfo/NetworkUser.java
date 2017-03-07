// Copyright 2005, 2006, 2008, 2009, 2010, 2011, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import org.deltava.beans.*;
import org.deltava.util.*;

/**
 * A bean to store Online Network user information.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public abstract class NetworkUser implements java.io.Serializable, ViewEntry, Comparable<NetworkUser> {
	
	public enum Type {
		PILOT, ATC, RATING;
	}
	
    private int _id;
    private String _firstName;
    private String _lastName;
    private Rating _rating;
    
    private int _databaseID;
    
    private final OnlineNetwork _net;
    
    /**
     * Initializes the bean with a given network ID.
     * @param id the user ID
     * @param net the OnlineNetwork
     * @throws IllegalArgumentException if id is negative
     * @see NetworkUser#setID(int)
     */
    public NetworkUser(int id, OnlineNetwork net) {
        super();
        setID(id);
        _net = net;
    }

    /**
     * Returns the user type.
     * @return the Type
     */
    public abstract Type getType();
    
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
     * Returns the Online Network for this user.
     * @return an OnlineNetwork, or null if unknown
     */
    public OnlineNetwork getNetwork() {
    	return _net;
    }
    
    /**
     * Returns the user's full name.
     * @return the user's name
     * @see NetworkUser#getFirstName()
     * @see NetworkUser#getLastName()
     */
    public String getName() {
    	StringBuilder buf = (_firstName == null) ? new StringBuilder() : new StringBuilder(_firstName).append(' ');
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
     * Returns the Controller's rating.
     * @return the Rating
     * @see NetworkUser#setRating(Rating)
     */
    public Rating getRating() {
       return _rating;
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
     * @see NetworkUser#getName()
     */
    public void setName(String name) {
    	String n = name.trim();
    	while (n.indexOf("  ") > -1)
    		n = n.replace("  ", " ");
    	
    	int pos = n.lastIndexOf(' ');
    	boolean oneSpace = (n.indexOf(' ') == pos);
    	if (!oneSpace) {
    		if (pos == (n.length() - 5))
    			n = n.substring(0, pos);
    		if (n.endsWith(" -"))
    			n = n.substring(0, n.length() - 2);
    	}
    	
    	// Split the data
    	pos = n.lastIndexOf(' ');
    	setLastName(n.substring(pos + 1));
    	if (pos > -1)
    		setFirstName(n.substring(0, pos));
    }
    
    /**
     * Sets the Users's rating.
     * @param r the Rating
     * @see NetworkUser#getRating()
     */
    public void setRating(Rating r) {
       _rating = r;
    }
    
    /**
     * Compares this user to another network user by comparing the Network IDs and Networks.
     */
    @Override
    public int compareTo(NetworkUser usr2) {
        int tmpResult = Integer.valueOf(_id).compareTo(Integer.valueOf(usr2._id));
        return (tmpResult == 0) ? _net.compareTo(usr2._net) : tmpResult;
    }
    
    @Override
    public String toString() {
    	StringBuilder buf = new StringBuilder(_net.toString());
    	buf.append(':').append(_id);
    	return buf.toString();
    }
    
    @Override
    public boolean equals(Object o2) {
    	return (o2 instanceof NetworkUser) ? (compareTo((NetworkUser) o2) == 0) : false;
    }
    
    @Override
    public int hashCode() {
    	return toString().hashCode();
    }
    
    @Override
    public String getRowClassName() {
    	return (_databaseID == 0) ? null : "opt2";
    }
}