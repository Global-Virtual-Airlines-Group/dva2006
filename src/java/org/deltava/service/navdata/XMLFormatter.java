// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.util.*;
import java.text.*;

import org.jdom2.*;
import org.deltava.beans.UseCount;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A utility class to handle XML translation for the {@link XMLClientDataService}.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

class XMLFormatter {

	// static class
	private XMLFormatter() {
		super();
	}
	
	/**
	 * Converts a collection of Gates into an XML document.
	 * @param me a Map.Entry with a Collection of Gates, keyed by Airport
	 * @return a Map.Entry with an XML document string, keyed by Airport ICAO code
	 */
	static Map.Entry<String, String> formatGate(Map.Entry<String, Collection<Gate>> me) {
		Airport a = SystemData.getAirport(me.getKey());
		if ((a == null) || StringUtils.isEmpty(a.getRegion())) return null;
		final NumberFormat df = new DecimalFormat("#0.000000");
		Document doc = new Document();
		Element re = new Element("gates");
		re.setAttribute("icao", a.getICAO());
		re.setAttribute("iata", a.getIATA());
		re.setAttribute("region", a.getRegion());
		doc.setRootElement(re);

		for (Gate g : me.getValue()) {
			Element ge = new Element("gate");
			ge.setAttribute("name", g.getName());
			ge.setAttribute("sim", g.getSimulator().toString());
			ge.setAttribute("hdg", String.valueOf(g.getHeading()));
			ge.setAttribute("lat", df.format(g.getLatitude()));
			ge.setAttribute("lng", df.format(g.getLongitude()));
			ge.setAttribute("zone", g.getZone().name());
			g.getAirlines().forEach(al -> ge.addContent(XMLUtils.createElement("airline", al.getCode(), false)));
			re.addContent(ge);
		}
		
		return Map.entry("gate_" + a.getICAO().toLowerCase(), XMLUtils.format(doc, "UTF-8"));
	}

	/**
	 * Converts a collection of Terminal Rotues into an XML document.
	 * @param me a Map.Entry with a Collection of TerminalRoutes, keyed by Airport
	 * @return a Map.Entry with an XML document string, keyed by Airport ICAO code
	 */
	static Map.Entry<String, String> formatTR(Map.Entry<String, Collection<TerminalRoute>> me) {
		Airport a = SystemData.getAirport(me.getKey());
		if ((a == null) || StringUtils.isEmpty(a.getRegion())) return null;
		final NumberFormat df = new DecimalFormat("#0.000000");
		Document doc = new Document();
		Element re = new Element("routes");
		re.setAttribute("icao", a.getICAO());
		re.setAttribute("iata", a.getIATA());
		re.setAttribute("region", a.getRegion());
		doc.setRootElement(re);
		for (TerminalRoute tr : me.getValue()) {
			Element tre = new Element(tr.getType().name().toLowerCase());
			tre.setAttribute("name", tr.getName());
			tre.setAttribute("id", tr.getCode());
			tre.setAttribute("transition", tr.getTransition());
			tre.setAttribute("runway", tr.getRunway());
			re.addContent(tre);			

			// Add the waypoint elements
			int mrkID = 0;
			for (NavigationDataBean ai : tr.getWaypoints()) {
				Element we = new Element("wp");
				we.setAttribute("code", ai.getCode());
				we.setAttribute("idx", String.valueOf(++mrkID));
				we.setAttribute("lat", df.format(ai.getLatitude()));
				we.setAttribute("lon", df.format(ai.getLongitude()));
				we.setAttribute("type", ai.getType().getName());
				if (ai.getRegion() != null)
					we.setAttribute("region", ai.getRegion());
				tre.addContent(we);
			}
		}
		
		return Map.entry("ss_" + a.getICAO().toLowerCase(), XMLUtils.format(doc, "UTF-8"));
	}
	
	/**
	 * Converts a collection of Runways into an XML document.
	 * @param me a Map.Entry with a Collection of Runways, keyed by Airport
	 * @return a Map.Entry with an XML document string, keyed by Airport ICAO code
	 */
	static Map.Entry<String, String> formatRunway(Map.Entry<String, Collection<Runway>> me) {
		Airport a = SystemData.getAirport(me.getKey());
		if ((a == null) || StringUtils.isEmpty(a.getRegion())) return null;
		final NumberFormat df = new DecimalFormat("#0.000000");
		Document doc = new Document();
		Element re = new Element("runways");
		re.setAttribute("icao", a.getICAO());
		re.setAttribute("iata", a.getIATA());
		re.setAttribute("region", a.getRegion());
		doc.setRootElement(re);
		for (Runway r : me.getValue()) {
			Element rwe = new Element("runway");			
			rwe.addContent(XMLUtils.createElement("name", r.getName()));
			rwe.setAttribute("lat", df.format(r.getLatitude()));
			rwe.setAttribute("lng", df.format(r.getLongitude()));			
			rwe.setAttribute("width", String.valueOf(r.getWidth()));
			rwe.setAttribute("hdg", String.valueOf(r.getHeading()));
			rwe.setAttribute("length", String.valueOf(r.getLength()));
			rwe.setAttribute("sim", r.getSimulator().name());
			rwe.setAttribute("sfc", r.getSurface().name());
			rwe.setAttribute("isHardSfc", String.valueOf(r.getSurface().isHard()));
			rwe.setAttribute("magVar", String.valueOf(r.getMagVar()));
			XMLUtils.addIfPresent(rwe, XMLUtils.createIfPresent("oldCode", r.getNewCode()));
			if ((r.getFrequency() != null) && (!"-".equals(r.getFrequency())))
				rwe.addContent(XMLUtils.createElement("freq", r.getFrequency()));
			if (r instanceof UseCount)
				rwe.addContent(XMLUtils.createElement("useCount", String.valueOf(((UseCount) r).getUseCount())));
				
			re.addContent(rwe);
		}
		
		return Map.entry("rwy_" + a.getICAO().toLowerCase(), XMLUtils.format(doc, "UTF-8"));		
	}
}