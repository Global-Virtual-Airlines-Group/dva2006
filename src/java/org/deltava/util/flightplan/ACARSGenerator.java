// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.flightplan;

import java.util.Collection;

import org.jdom2.*;

import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.util.XMLUtils;

/**
 * A flight plan generator for ACARS dispatch sheets. 
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class ACARSGenerator extends FlightPlanGenerator {
	
	private Airport _aL;
	
	private Aircraft _ac;
	private int _paxCount;
	
	private Gate _gD;
	private Gate _gA;
	
	public void setPassengerCount(int pax) {
		_paxCount = pax;
	}
	
	public void setAircraft(Aircraft a) {
		_ac = a;
	}
	
	public void setAirportL(Airport a) {
		_aL = a;
	}
	
	public void setGateD(Gate g) {
		_gD = g;
	}
	
	public void setGateA(Gate g) {
		_gA = g;
	}

	@Override
	public String generate(Collection<NavigationDataBean> waypoints) {
		
		// Create the route Pair
		ScheduleRoute rp = new ScheduleRoute(_al, _aD, _aA); 
		
		// Build the header
		Document doc = new Document();
		Element re = new Element("acars.loadsheet");
		doc.setRootElement(re);
		
		// Add the core fields
		re.addContent(XMLUtils.createElement("airline", _al.getCode()));
		re.addContent(format(_aD, "airportD"));
		re.addContent(format(_aA, "airportA"));
		XMLUtils.addIfPresent(re, format(_aL, "airportL"));
		re.addContent(XMLUtils.createElement("flightType", rp.getFlightType().name()));
		re.addContent(XMLUtils.createElement("altitude", _altitude));
		re.addContent(XMLUtils.createElement("eqType", _ac.getName()));
		XMLUtils.addIfPresent(re, XMLUtils.createIfPresent("pax", (_paxCount < 1) ? null : String.valueOf(_paxCount)));
		XMLUtils.addIfPresent(re, format(_gD, "gateD"));
		XMLUtils.addIfPresent(re, format(_gA, "gateA"));
		
		// Add the waypoints
		Element rte = new Element("route"); re.addContent(rte);
		for (NavigationDataBean nd : waypoints) {
			Element wpe = new Element("waypoint");
			wpe.setAttribute("code", nd.getCode());
			wpe.setAttribute("lat", String.valueOf(nd.getLatitude()));
			wpe.setAttribute("lng", String.valueOf(nd.getLongitude()));
			wpe.setAttribute("type", nd.getType().name());
			XMLUtils.addIfPresent(wpe, XMLUtils.createIfPresent("region", nd.getRegion()));
			if (nd.isInTerminalRoute()) {
				String aw = nd.getAirway();
				wpe.addContent(XMLUtils.createElement("airway", aw.substring(0, aw.indexOf('.'))));
			} else
				XMLUtils.addIfPresent(wpe, XMLUtils.createIfPresent("airway", nd.getAirway()));
		}
		
		return XMLUtils.format(doc, getEncoding());
	}
	
	private static Element format(Gate g, String elementName) {
		if (g == null) return null;
		Element e = new Element(elementName);
		e.setAttribute("name", g.getName());
		e.setAttribute("icao", g.getCode());
		e.setAttribute("type", g.getGateType().name());
		e.setAttribute("lat", String.valueOf(g.getLatitude()));
		e.setAttribute("lng", String.valueOf(g.getLongitude()));
		e.addContent(XMLUtils.createElement("zone", g.getZone().name()));
		g.getAirlines().stream().map(al -> { Element ae = XMLUtils.createElement("airline", al.getName()); ae.setAttribute("code", al.getCode()); return ae; }).forEach(e::addContent);
		return e;
	}
	
	private static Element format(Airport a, String elementName) {
		if (a == null) return null;
		Element e = XMLUtils.createElement(elementName, a.getName(), true);
		e.setAttribute("icao", a.getICAO());
		e.setAttribute("iata", a.getIATA());
		e.setAttribute("lat", String.valueOf(a.getLatitude()));
		e.setAttribute("lng", String.valueOf(a.getLongitude()));
		e.setAttribute("alt", String.valueOf(a.getAltitude()));
		e.addContent(XMLUtils.createElement("country", a.getCountry().getCode()));
		e.addContent(XMLUtils.createElement("hasUSPFI", String.valueOf(a.getHasPFI())));
		e.addContent(XMLUtils.createElement("isSchengen", String.valueOf(a.getIsSchengen())));
		return e;
	}
	
	private static Element format(Runway r) {
		if (r == null) return null;
		Element e = new Element("runway");
		e.setAttribute("name", r.getName());
		e.setAttribute("icao", r.getCode());
		e.setAttribute("lat", String.valueOf(r.getLatitude()));
		e.setAttribute("lng", String.valueOf(r.getLongitude()));
		e.setAttribute("hdg", String.valueOf(r.getHeading()));
		e.setAttribute("length", String.valueOf(r.getLength()));
		e.setAttribute("sfc", r.getSurface().name());
		e.setAttribute("isHardSfc", String.valueOf(r.getSurface().isHard()));
		return e;
	}
	
	@Override
	public final String getMimeType() {
		return "text/xml";
	}
}