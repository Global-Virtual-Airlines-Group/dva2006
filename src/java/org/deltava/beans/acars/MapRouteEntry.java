// Copyright 2005, 2006, 2007, 2008, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.util.StringUtils;

import org.gvagroup.acars.ACARSFlags;

/**
 * A bean to store a snapshot of an ACARS-logged flight.
 * @author Luke
 * @version 3.3
 * @since 1.0
 */

public class MapRouteEntry extends RouteEntry implements TabbedMapEntry {
	
	private static final List<String> TAB_NAMES = Arrays.asList("Pilot", "Flight Data");

	private String _eqType;
	private String _flightNumber;
	private Airport _airportD;
	private Airport _airportA;
	private OnlineNetwork _network;
	private boolean _checkRide;
	private boolean _dispatchRoute;

	public MapRouteEntry(Date dt, GeoLocation gl, Pilot usr, String eqType) {
		super(dt, gl);
		_usr = usr;
		_eqType = eqType;
	}

	public void setCheckRide(boolean isCR) {
		_checkRide = isCR;
	}
	
	public void setDispatchPlan(boolean isDP) {
		_dispatchRoute = isDP;
	}

	public void setFlightNumber(String flightNumber) {
		_flightNumber = flightNumber;
	}

	public void setAirportD(Airport a) {
		_airportD = a;
	}
	
	public void setAirportA(Airport a) {
		_airportA = a;
	}
	
	public void setNetwork(OnlineNetwork network) {
		_network = network;
	}

	public final String getIconColor() {
		if (isFlagSet(ACARSFlags.FLAG_PAUSED) || isWarning())
			return RED;
		else if (isFlagSet(ACARSFlags.FLAG_ONGROUND))
			return WHITE;
		else if (getVerticalSpeed() > 100)
			return ORANGE;
		else if (getVerticalSpeed() < -100)
			return YELLOW;
		else 
			return BLUE;
	}
	
	public final String getInfoBox() {
		StringBuilder buf = new StringBuilder(_usr.getRank().getName());
		buf.append(", ");
		buf.append(_usr.getEquipmentType());
		buf.append("<br />");
		if (!StringUtils.isEmpty(_flightNumber)) {
			buf.append("<br />Flight <b>");
			buf.append(_flightNumber);
			buf.append("</b> - <span class=\"sec bld\">");
		}

		buf.append(_eqType);
		buf.append("</span> (Build ");
		buf.append(_clientBuild);
		if (_betaBuild > 0) {
			buf.append(" Beta ");
			buf.append(_betaBuild);
		}
		
		buf.append(")<br />From: ");
		buf.append(_airportD.getName());
		buf.append(" (");
		buf.append(_airportD.getICAO());
		buf.append(")<br />To: ");
		buf.append(_airportA.getName());
		buf.append(" (");
		buf.append(_airportA.getICAO());
		buf.append(")<br />");
		if (_network != null) {
			buf.append("Flight operated online using <span class=\"sec bld caps\">");
			buf.append(_network.toString());
			buf.append("</span><br />");
		}
		
		buf.append("<br />");
		buf.append(super.getInfoBox());
		return buf.toString();
	}

	public final List<String> getTabNames() {
		return TAB_NAMES;
	}

	public final List<String> getTabContents() {
		List<String> results = new ArrayList<String>();

		// Build Pilot information
		StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox\"><span class=\"pri bld\">");
		buf.append(_usr.getName());
		buf.append("</span>");
		if (!StringUtils.isEmpty(_usr.getPilotCode())) {
			buf.append(" (");
			buf.append(_usr.getPilotCode());
			buf.append(')');
		}
		
		buf.append("<br />");
		buf.append(_usr.getRank());
		buf.append(", ");
		buf.append(_usr.getEquipmentType());
		buf.append("<br />");
		if (!StringUtils.isEmpty(_flightNumber)) {
			buf.append("<br />Flight <b>");
			buf.append(_flightNumber);
			buf.append("</b> - <span class=\"sec bld\">");
		}

		buf.append(_eqType);
		buf.append("</span> (Build ");
		buf.append(String.valueOf(_clientBuild));
		if (_betaBuild > 0) {
			buf.append(" Beta ");
			buf.append(_betaBuild);
		}
		
		buf.append(")<br />From: ");
		buf.append(_airportD.getName());
		buf.append(" (");
		buf.append(_airportD.getICAO());
		buf.append(")<br />To: ");
		buf.append(_airportA.getName());
		buf.append(" (");
		buf.append(_airportA.getICAO());
		buf.append(")<br /><br />ACARS Flight <b>");
		buf.append(StringUtils.format(getID(), "#,##0"));
		buf.append("</b>");
		if (_checkRide || _dispatchRoute || _busy) {
			buf.append("<br />");
			if (_checkRide) buf.append("<span class=\"pri bld\">CHECK RIDE</span> ");
			if (_dispatchRoute) buf.append("<span class=\"sec bld\">USING DISPATCH</span> ");
			if (_busy) buf.append("<span class=\"error bld\">BUSY</span>");
		}
		
		buf.append("</div>");
		results.add(buf.toString());
		
		// Add Flight information
		results.add(super.getInfoBox());
		return results;
	}
}