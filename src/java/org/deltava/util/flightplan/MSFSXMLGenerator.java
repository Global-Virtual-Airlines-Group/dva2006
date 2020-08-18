// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.flightplan;

import java.util.Collection;

import org.jdom2.*;

import org.deltava.beans.navdata.NavigationDataBean;

import org.deltava.util.*;

/**
 * An abstract Flight Plan Generator to create FSX-style XML flight plans.
 * @author Luke
 * @version 9.1
 * @since 9.1
 */

abstract class MSFSXMLGenerator extends MSFSGenerator {
	
	private final int _appMajor;
	private final int _appVersion;
	
	protected MSFSXMLGenerator(int appMajor, int appVersion) {
		super();
		_appMajor = appMajor;
		_appVersion = appVersion;
	}
	
	/**
	 * Generates an XML flight plan between two airports.
	 * @param waypoints a Collection of waypoints
	 * @return the generated XML Document
	 */
	protected Document generateDocument(Collection<NavigationDataBean> waypoints) {
		
		Document doc = new Document();
		Element re = new Element("SimBase.Document");
		re.setAttribute("Type", "AceXML");
		re.setAttribute("version", "1,0");
		re.addContent(XMLUtils.createElement("Descr", "AceXML Document"));
		doc.setRootElement(re);

		// Flight plan header
		Element be = new Element("FlightPlan.FlightPlan");
		re.addContent(be);
		be.addContent(XMLUtils.createElement("Title", _aD.getICAO() + " to " + _aA.getICAO()));
		be.addContent(XMLUtils.createElement("FPType", "IFR"));
		be.addContent(XMLUtils.createElement("CruisingAlt", _altitude));
		be.addContent(XMLUtils.createElement("DepartureID", _aD.getICAO()));
		be.addContent(XMLUtils.createElement("DepartureLLA", GeoUtils.formatFSX(_aD)));
		be.addContent(XMLUtils.createElement("DestinationID", _aA.getICAO()));
		be.addContent(XMLUtils.createElement("DestinationLLA", GeoUtils.formatFSX(_aA)));
		be.addContent(XMLUtils.createElement("Descr", _aD.getName() + " - " + _aA.getName()));
		be.addContent(XMLUtils.createElement("DepartureName", _aD.getName()));
		be.addContent(XMLUtils.createElement("DestinationName", _aA.getName()));
		if (_gateD != null)
			be.addContent(XMLUtils.createElement("DeparturePosition", _gateD.getName()));
		
		// Build version data
		Element ve = XMLUtils.createElement("AppVersion", "AppVersionMajor", String.valueOf(_appMajor));
		ve.addContent(XMLUtils.createElement("AppVersionBuild", String.valueOf(_appVersion)));
		be.addContent(ve);
		
		// Build waypoints
		for (NavigationDataBean nd : waypoints) {
			Element nde = new Element("ATCWaypoint");
			nde.setAttribute("id", nd.getCode());
			nde.addContent(XMLUtils.createElement("ATCWaypointType", nd.getType().getName()));
			nde.addContent(XMLUtils.createElement("WorldPosition", GeoUtils.formatFSX(nd) + "," + GeoUtils.formatFSElevation(0)));
			Element iie = XMLUtils.createElement("ICAO", "ICAOIdent", nd.getCode());
			if (!StringUtils.isEmpty(nd.getRegion()))
				iie.addContent(XMLUtils.createElement("ICAORegion", nd.getRegion()));
			if (nd.isInTerminalRoute()) {
				String aw = nd.getAirway();
				nde.addContent(XMLUtils.createElement("ATCAirway", aw.substring(0, aw.indexOf('.'))));
			} else if (nd.getAirway() != null)
				nde.addContent(XMLUtils.createElement("ATCAirway", nd.getAirway()));
			
			nde.addContent(iie);
			be.addContent(nde);
		}
		
		return doc;
	}
	
	@Override
	public final String getMimeType() {
		return "text/xml";
	}
}