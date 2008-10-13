// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.Date;

import org.deltava.beans.DatabaseBean;

/**
 * 
 * @author Luke
 * @version 2.2
 * @since 2.2
 */
public class FlightRoute extends DatabaseBean {
	
	private Date _createdOn;
	
	private Airport _airportD;
	private Airport _airportA;
	
	private String _altitude;
	private String _routeText;
	private String _comments;

	/**
	 * Creates the bean. 
	 */
	public FlightRoute() {
		super();
	}
	
	/**
	 * Returns the creation date of this route.
	 * @return the creation date/time
	 */
	public Date getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the departure Airport.
	 * @return the Airport
	 */
	public Airport getAirportD() {
		return _airportD;
	}
	
	/**
	 * Returns the arrival Airport.
	 * @return the Airport
	 */
	public Airport getAirportA() {
		return _airportA;
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
	public void setCreatedOn(Date dt) {
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
}