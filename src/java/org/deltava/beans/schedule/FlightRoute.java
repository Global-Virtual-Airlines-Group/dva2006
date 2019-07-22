// Copyright 2008, 2009, 2011, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * An abstract class to store common Flight Route information.
 * @author Luke
 * @version 8.6
 * @since 2.2
 */

public abstract class FlightRoute extends DatabaseBean implements RoutePair, ComboAlias {
	
	private Instant _createdOn;
	
	private Airport _airportD;
	private Airport _airportA;
	
	private String _sid;
	private String _star;
	
	private String _altitude;
	private String _routeText;
	private String _comments;

	/**
	 * Returns the creation date of this route.
	 * @return the creation date/time
	 */
	public Instant getCreatedOn() {
		return _createdOn;
	}

	@Override
	public Airport getAirportD() {
		return _airportD;
	}
	
	@Override	
	public Airport getAirportA() {
		return _airportA;
	}
	
	/**
	 * Returns the Standard Instrument Departure ID.
	 * @return the ID in NAME.TRANSITION.RUNWAY format
	 */
	public String getSID() {
		return _sid;
	}
	
	/**
	 * Returns the Standard Terminal Arrival Route ID.
	 * @return the ID in NAME.TRANSITION.RUNWAY format
	 */
	public String getSTAR() {
		return _star;
	}
	
	/**
	 * Returns the Cruise Altitude.
	 * @return the altitude
	 */
	public String getCruiseAltitude() {
		return _altitude;
	}
	
	/**
	 * Returns the dispatcher comments.
	 * @return the comments
	 */
	public String getComments() {
		return _comments;
	}
	
	/**
	 * Returns the route.
	 * @return a space-separated list of waypoints and airways
	 */
	public String getRoute() {
		return _routeText;
	}		

	/**
	 * Updates the creation date of this route.
	 * @param dt the creation date/time
	 */
	public void setCreatedOn(Instant dt) {
		_createdOn = dt;
	}
	
	/**
	 * Updates the departure Airport.
	 * @param a the Airport
	 */
	public void setAirportD(Airport a) {
		_airportD = a;	
	}
	
	/**
	 * Updates the arrival Airport.
	 * @param a the Airport
	 */
	public void setAirportA(Airport a) {
		_airportA = a;
	}
	
	/**
	 * Updates the Standard Instrument Departure ID.
	 * @param sid the SID ID
	 */
	public void setSID(String sid) {
		_sid = sid;
	}
	
	/**
	 * Updates the Standard Terminal Arrival Route ID.
	 * @param star the STAR ID
	 */
	public void setSTAR(String star) {
		_star = star;
	}
	
	/**
	 * Updates the cruise altitude for this route.
	 * @param alt the cruise altitude
	 */
	public void setCruiseAltitude(String alt) {
		_altitude = alt;
	}
	
	/**
	 * Updates the dispatcher comments.
	 * @param comments the comments
	 */
	public void setComments(String comments) {
		_comments = comments;
	}
	
	/**
	 * Sets the route text.
	 * @param routeText the route text
	 */
	public void setRoute(String routeText) {
		_routeText = routeText;
	}
	
	/**
	 * Displays a friendly route with SID/STAR name.
	 * @return the route with SID/STAR
	 */
	public String getFullRoute() {
		StringBuilder buf = new StringBuilder();
		String sid = getSID();
		if ((sid != null) && (sid.contains("."))) {
			buf.append(sid.substring(0, sid.indexOf('.')));
			buf.append(' ');
		}
		
		buf.append(getRoute());
		String star = getSTAR();
		if ((star != null) && (star.contains("."))) {
			buf.append(' ');
			buf.append(star.substring(0, star.indexOf('.')));
		}
		
		return buf.toString();
	}
	
	@Override
	public String toString() {
		return getFullRoute();
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FlightRoute))
			return false;
		
		try {
			FlightRoute r2 = (FlightRoute) o;
			return (getAirportD().equals(r2.getAirportD())) && (getAirportA().equals(r2.getAirportA())) &&
				toString().equals(r2.toString());
		} catch (Exception e) {
			return false;
		}
	}
}