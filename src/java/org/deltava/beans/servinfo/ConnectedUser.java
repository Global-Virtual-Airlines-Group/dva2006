// Copyright 2010, 2016, 2017, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.time.Instant;

import org.deltava.beans.*;

import org.deltava.util.*;

/**
 * A bean to store information about users connected to an FSD server. 
 * @author Luke
 * @version 10.0
 * @since 3.4
 */

public abstract class ConnectedUser extends NetworkUser implements MarkerMapEntry {
	
	private String _callSign;
	private String _server;
	private Instant _loginTime;
	
	protected GeoLocation _position;
	
	/**
	 * Initializes the user.
	 * @param id the network ID
	 * @param net the OnlineNetwork
	 */
	public ConnectedUser(int id, OnlineNetwork net) {
		super(id, net);
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
	 * Returns the date/time of the user's connection.
	 * @return the login date/time
	 */
	public Instant getLoginTime() {
		return _loginTime;
	}

	/**
	 * Returns the User's current position.
	 * @return a GeoPosition bean containing latitude and longitude
	 * @see ConnectedUser#getLatitude()
	 * @see ConnectedUser#getLongitude()
	 * @see ConnectedUser#setPosition(double, double)
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
	 * @see ConnectedUser#getPosition()
	 */
	public void setPosition(double lat, double lon) {
		_position = GeoUtils.normalize(lat, lon);
	}
	
	/**
	 * Updates the date/time of the user's last connection.
	 * @param dt the connection date/time
	 */
	public void setLoginTime(Instant dt) {
		_loginTime = dt;
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