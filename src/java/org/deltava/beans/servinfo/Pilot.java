// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2015, 2017, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.navdata.NavigationDataBean;
import org.deltava.beans.schedule.*;

import org.deltava.util.*;

/**
 * A bean to store online Pilot information.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class Pilot extends ConnectedUser implements RoutePair {

	private int _altitude;
	private int _gSpeed;
	private int _hdg;
	private String _eqCode;
	private Airport _airportD;
	private Airport _airportA;
	private String _comments;
	private boolean _isHighlighted;
	
	private String _route;
	private final Collection<NavigationDataBean> _wps = new LinkedHashSet<NavigationDataBean>();
	
	/**
	 * Initializes the bean with a given user ID.
	 * @param id the user ID
	 * @param net the OnlineNetwork
	 */
	public Pilot(int id, OnlineNetwork net) {
		super(id, net);
	}

	@Override
	public Airport getAirportA() {
		return _airportA;
	}

	@Override
	public Airport getAirportD() {
		return _airportD;
	}

	/**
	 * Returns the current altitude.
	 * @return the altitude in feet above Mean Sea Level
	 * @see Pilot#setAltitude(int)
	 */
	public int getAltitude() {
		return _altitude;
	}

	/**
	 * Returns the User's Flight Plan comments.
	 * @return the comments
	 * @see Pilot#setComments(String)
	 */
	public String getComments() {
		return _comments;
	}

	/**
	 * Returns the User's equipment code.
	 * @return the equipment code
	 * @see Pilot#setEquipmentCode(String)
	 */
	public String getEquipmentCode() {
		return _eqCode;
	}

	/**
	 * Returns the User's ground speed.
	 * @return the ground speed in knots
	 * @see Pilot#setGroundSpeed(int)
	 */
	public int getGroundSpeed() {
		return _gSpeed;
	}
	
	/**
	 * Returns the Pilot's heading.
	 * @return the heading in degrees
	 * @see Pilot#setHeading(int)
	 */
	public int getHeading() {
		return _hdg;
	}

	@Override
	public final Type getType() {
		return Type.PILOT;
	}
	
	@Override
    public final Rating getRating() {
       return Rating.OBS;
    }

	/**
	 * Returns the Pilot's filed route.
	 * @return the route
	 * @see Pilot#setRoute(String)
	 */
	public String getRoute() {
	   return _route;
	}
	
	/**
	 * Returns if the waypoint collection has been populated.
	 * @return TRUE if the waypoints have been loaded, otherwise FALSE
	 */
	public boolean isRoutePopulated() {
		return !_wps.isEmpty();
	}
	
	/**
	 * Returns if this Pilot should be highlighted.
	 * @return TRUE if the Pilot is highlighted, otherwise FALSE
	 * @see Pilot#setHighlighted(boolean)
	 */
	public boolean isHighlighted() {
		return _isHighlighted;
	}
	
	/**
	 * Updates the Pilot's altitude.
	 * @param alt the altitude in feet above Mean Sea Level
	 * @see Pilot#getAltitude()
	 */
	public void setAltitude(int alt) {
		_altitude = Math.max(-300, Math.min(150000, alt));
	}

	/**
	 * Updates the destination Airport.
	 * @param aa the destination Airport
	 * @see Pilot#getAirportA()
	 */
	public void setAirportA(Airport aa) {
		_airportA = aa;
	}

	/**
	 * Updates the origin Airport.
	 * @param ad the origin Airport
	 * @see Pilot#getAirportD()
	 */
	public void setAirportD(Airport ad) {
		_airportD = ad;
	}

	/**
	 * Updates the Pilot's flight plan comments.
	 * @param comments the flight plan comments
	 * @see Pilot#getComments()
	 */
	public void setComments(String comments) {
		_comments = comments;
	}

	/**
	 * Updates the Pilot's equipment code.
	 * @param eqCode the equipment code
	 * @see Pilot#getEquipmentCode()
	 */
	public void setEquipmentCode(String eqCode) {
		_eqCode = eqCode;
	}

	/**
	 * Updates the Pilot's ground speed.
	 * @param gSpeed the ground speed in knots
	 * @see Pilot#getGroundSpeed()
	 */
	public void setGroundSpeed(int gSpeed) {
		_gSpeed = Math.max(-5, Math.min(4500, gSpeed));
	}

	/**
	 * Updates the Pilot's heading.
	 * @param hdg the heading in degrees
	 * @see Pilot#getHeading()
	 */
	public void setHeading(int hdg) {
		_hdg = Math.max(0, Math.min(359, hdg));
	}
	
	/**
	 * Updates the Pilot's filed waypoints.
	 * @param route a space-delimited string
	 * @throws NullPointerException if route is null
	 * @see Pilot#getRoute()
	 */
	public void setRoute(String route) {
	   _route = route.trim().toUpperCase();
	}
	
	/**
	 * Adds a point to the collection of waypoints.
	 * @param nd a NavigationDataBean
	 */
	public void addWaypoint(NavigationDataBean nd) {
		_wps.add(nd);
	}
	
	/**
	 * Adds multiple waypoints to the collection of waypoints.
	 * @param nds a Collection of NavigationDataBeans
	 */
	public void addWaypoints(Collection<NavigationDataBean> nds) {
		_wps.addAll(nds);
	}

	/**
	 * Marks this online Pilot as highlighted.
	 * @param isHighlighted TRUE if the Pilot is highlighted, otherwise FALSE
	 * @see Pilot#isHighlighted()
	 */
	public void setHighlighted(boolean isHighlighted) {
		_isHighlighted = isHighlighted;
	}
	
	@Override
    public final void setRating(Rating r) {
    	super.setRating(Rating.OBS);
    }
	
	/**
	 * Returns the flight waypoints. 
	 * @return a List of GeoLocations
	 */
	public Collection<GeoLocation> getWaypoints() {
		
		// Only generate a route if both airports have positions
		Collection<GeoLocation> route = new ArrayList<GeoLocation>();
		if ((_airportD.hasPosition()) && (_airportA.hasPosition())) {
			route.add(_airportD);
			if (!_wps.isEmpty())
				route.addAll(_wps);
			else
				route.add(_position);
			
			route.add(_airportA);
		}
	
		return route;
	}
	
	@Override
	public String getIconColor() {
		if (_isHighlighted && (getPilotID() != 0))
			return BLUE;
		else if (_isHighlighted)
			return YELLOW;
		
		return WHITE;
	}
	
	@Override
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox onlinePilot\"><span class=\"bld\">");
		buf.append(getCallsign());
		buf.append("</span> (");
		buf.append(StringUtils.stripInlineHTML(getName()));
		buf.append(")<br /><br />");
		if (isPopulated()) {
			buf.append("Flying from ");
			buf.append(_airportD.getICAO());
			buf.append(" to ");
			buf.append(_airportA.getICAO());	
		}
		
		buf.append("<br />Position: ");
		buf.append(StringUtils.format(_position, true, GeoLocation.ALL));
		buf.append("<br />Altitude: ");
		buf.append(StringUtils.format(_altitude, "#,##0"));
		buf.append(" feet<br />Speed: ");
		buf.append(StringUtils.format(_gSpeed, "#,##0"));
		buf.append(" knots<br /><br />Network ID: ");
		buf.append(String.valueOf(getID()));
		buf.append("</div>");
		return buf.toString();
	}
}