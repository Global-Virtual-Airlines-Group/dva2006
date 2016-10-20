// Copyright 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import org.deltava.beans.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.util.*;

/**
 * A bean to store information about users connected to an FSD server. 
 * @author Luke
 * @version 7.2
 * @since 3.4
 */

public abstract class ConnectedUser extends NetworkUser implements MarkerMapEntry {
	
	private String _callSign;
	private String _server;
	
	protected GeoPosition _position;
	
	/**
	 * Initializes the user.
	 * @param id the network ID
	 */
	public ConnectedUser(int id) {
		super(id);
	}

    /**
     * Returns the user's callsign.
     * @return the callsign
     * @see ConnectedUser#setCallsign(String)
     */
    public String getCallsign() {
        return _callSign;
    }

    @Override
	public final double getLatitude() {
		return _position.getLatitude();
	}

	@Override
	public final double getLongitude() {
		return _position.getLongitude();
	}

	/**
	 * Returns the User's current position.
	 * @return a GeoPosition bean containing latitude and longitude
	 * @see ConnectedUser#getLatitude()
	 * @see ConnectedUser#getLongitude()
	 * @see ConnectedUser#setPosition(double, double)
	 * @see ConnectedUser#setPosition(String, String)
	 */
	public GeoLocation getPosition() {
		return _position;
	}
	
	/**
	 * Returns the FSD Server this user is connected to.
	 * @return the server name
	 * @see ConnectedUser#setServer(String)
	 */
	public String getServer() {
		return _server;
	}
	
	/**
	 * Updates the User's position. This has a ServInfo hack where latitudes of -290 to -350 are mapped to
	 * latitudes in the Northern Hemisphere.
	 * @param lat the latitude in degrees
	 * @param lon the longitude in degrees
	 * @see ConnectedUser#setPosition(String, String)
	 * @see ConnectedUser#getPosition()
	 */
	public void setPosition(double lat, double lon) {
		_position = new GeoPosition(GeoUtils.normalize(lat, lon));
	}
	
	/**
	 * Updates the User's position.
	 * @param lat a String containing the latitude
	 * @param lon a String containing the longitude
	 * @see ConnectedUser#setPosition(double, double)
	 * @see ConnectedUser#getPosition()
	 */
	public void setPosition(String lat, String lon) {
		setPosition(StringUtils.parse(lat, 0.0d), StringUtils.parse(lon, 0.0d));
	}
	
    /**
     * Updates the user's callsign.
     * @param cs the callsign
     * @throws NullPointerException if cs is null
     * @see ConnectedUser#getCallsign()
     */
    public void setCallsign(String cs) {
        _callSign = cs.trim().toUpperCase();
    }
    
    /**
     * Updates the FSD Server this user is connected to.
     * @param srv the server name
     * @throws NullPointerException if srv is null
     * @see ConnectedUser#getServer()
     */
    public void setServer(String srv) {
    	_server = srv.toUpperCase().trim();
    }
}