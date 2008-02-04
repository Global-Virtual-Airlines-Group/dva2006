// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

/**
 * A bean to store saved ACARS dispatch routes.
 * @author Luke
 * @version 2.1
 * @since 2.0
 */

public class RoutePlan extends DatabaseBean implements AuthoredBean {
	
	private int _authorID;
	private Date _createdOn;
	private Airline _a;
	private Airport _airportD;
	private Airport _airportA;
	private Airport _airportL;
	private String _sid;
	private String _star;
	private String _altitude;
	private String _comments;
	private int _useCount;
	
	private final Map<NavigationDataBean, String> _route = new LinkedHashMap<NavigationDataBean, String>(); 

	/**
	 * Creates the route bean.
	 * @param id the database ID
	 */
	public RoutePlan(int id) {
		super();
		setID(id);
	}

	public int getAuthorID() {
		return _authorID;
	}
	
	/**
	 * Returns the number of times this route has been used.
	 * @return the usage count
	 */
	public int getUseCount() {
		return _useCount;
	}
	
	/**
	 * Returns the creation date of this route.
	 * @return the creation date/time
	 */
	public Date getCreatedOn() {
		return _createdOn;
	}
	
	/**
	 * Returns the Airline for this Route.
	 * @return the Airline
	 */
	public Airline getAirline() {
		return _a;
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
	 * Returns the alternate Airport.
	 * @return the Airport, or null if none
	 */
	public Airport getAirportL() {
		return _airportL;
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
	 * @return a space-separated list of waypoints
	 */
	public String getRoute() {
		StringBuilder buf = new StringBuilder();
		for (Iterator<NavigationDataBean> i = _route.keySet().iterator(); i.hasNext(); ) {
			NavigationDataBean nd = i.next();
			buf.append(nd.getCode());
			if (i.hasNext())
				buf.append(' ');
		}
		
		return buf.toString();
	}
	
	/**
	 * Returns the waypoints on this route.
	 * @return a Collection of NavigationDataBeans
	 */
	public Collection<NavigationDataBean> getWaypoints() {
		return _route.keySet();
	}
	
	/**
	 * Returns the Airway each waypoint is on.
	 * @param nd the waypoint
	 * @return the Airway code, or null if none
	 */
	public String getAirway(NavigationDataBean nd) {
		return _route.get(nd);
	}
	
	/**
	 * Adds a waypoint to the route.
	 * @param nd the waypoint
	 * @param airway the airway it is on, or null
	 */
	public void addWaypoint(NavigationDataBean nd, String airway) {
		_route.put(nd, (airway == null) ? "" : airway);
	}

	/**
	 * Updates the creation date of this route.
	 * @param dt the creation date/time
	 */
	public void setCreatedOn(Date dt) {
		_createdOn = dt;
	}
	
	/**
	 * Updates the number of times this route has been used.
	 * @param count the usage count
	 */
	public void setUseCount(int count) {
		_useCount = Math.max(0, count);
	}
	
	/**
	 * Updates the Airline.
	 * @param a the Airline
	 */
	public void setAirline(Airline a) {
		_a = a;
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
	 * Updates the alternate Airport.
	 * @param a the Airport
	 */
	public void setAirportL(Airport a) {
		_airportL = a;
	}
	
	public void setAuthorID(int id) {
		validateID(_authorID, id);
		_authorID = id;
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
}