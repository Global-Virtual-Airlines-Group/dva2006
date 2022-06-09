// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.util.*;

import org.deltava.beans.TabbedMapEntry;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.Airline;

import org.deltava.comparators.AirlineComparator;

/**
 * A bean to store airport Gate information and allow metadata editing on a map.
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

class SelectableGate extends Gate implements TabbedMapEntry {
	
	private final Collection<Airline> _airlines = new TreeSet<Airline>(new AirlineComparator(AirlineComparator.NAME));
	private final Collection<GateZone> _zones = new TreeSet<GateZone>();
	
	/**
	 * Creates a new selectable Gate from an existing Gate.
	 * @param g the Gate
	 */
	SelectableGate(Gate g) {
		super(g, g.getUseCount());
	}
	
	String getUniqueID() {
		return Integer.toHexString(getName().hashCode()).toLowerCase();
	}
	
	/**
	 * Adds selectable Airline choices.
	 * @param airlines a Collection of Airlines
	 */
	public void setAirlineOptions(Collection<Airline> airlines) {
		_airlines.clear();
		_airlines.addAll(airlines);
	}
	
	public void setZoneOptions(Collection<GateZone> zones) {
		_zones.clear();
		_zones.addAll(zones);
	}
	
	@Override
	public LinkedHashMap<String, String> getTabs() {
		LinkedHashMap<String, String> results = new LinkedHashMap<String, String>();
		results.put("Information", getInfoBox());
		
		// Build info edit screen
		StringBuilder buf = new StringBuilder(256);
		buf.append("<span class=\"small\">Zone: <select size=\"1\" name=\"zoneSelect-");
		buf.append(getUniqueID());
		buf.append("\" onChange=\"void golgotha.gate.updateZone(this)\">\n");
		
		// Render zones
		for (GateZone gz : _zones) {
			buf.append("<option value=\"");
			buf.append(gz.name());
			buf.append("\">");
			buf.append(gz.getDescription());
			buf.append("</option>");
		}
		
		buf.append("</select><br /><br />\n");
		
		// Render airline checkboxes
		for (Iterator<Airline> i = _airlines.iterator(); i.hasNext(); ) {
			Airline al = i.next();
			buf.append("<input type=\"checkbox\" name=\"gateSelect-");
			buf.append(getUniqueID());
			buf.append("\" onChange=\"void golgotha.gate.updateGateAirline(this)\" value=\"");
			buf.append(al.getCode());
			buf.append("\"> ");
			buf.append(al.getName());
			if (i.hasNext())
				buf.append("<br />\n");
		}
		
		buf.append("</span>");
		results.put("Zone/Airlines", buf.toString());
		return results;
	}
}