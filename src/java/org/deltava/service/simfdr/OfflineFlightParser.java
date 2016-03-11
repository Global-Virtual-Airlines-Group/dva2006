// Copyright 2009, 2010, 2011, 2012, 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.simfdr;

import java.util.*;

import org.apache.log4j.Logger;

import org.jdom2.*;
import org.jdom2.input.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A utility class to parse simFDR submitted flight reports.
 * @author Luke
 * @version 7.0
 * @since 7.0
 */

final class OfflineFlightParser {
	
	private static final Logger log = Logger.getLogger(OfflineFlightParser.class);

	// singleton
	private OfflineFlightParser() {
		super();
	}
	
	private static double parse(String xml) {
		return Double.parseDouble(xml);
	}
	
	/**
	 * Parses an Offline Flight XML document.
	 * @param xml the XML to parse
	 * @return an OfflineFlight bean
	 */
	static OfflineFlight<SimFDRFlightReport, ACARSRouteEntry> create(String xml) {
		
		// Get the XML
		Document doc = null;
		try {
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(new java.io.StringReader(xml));
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		
		// Get aircraft information
		Element re = doc.getRootElement();
		Element ae = re.getChild("aircraft");
		if (ae == null)
			throw new IllegalArgumentException("No Aircraft Information");

		// Get the flight information
		Element ie = re.getChild("info");
		if (ie == null)
			throw new IllegalArgumentException("No Flight Information");
		
		// Build the offline Flight
		String cs = ie.getChildTextTrim("callsign");
		OfflineFlight<SimFDRFlightReport, ACARSRouteEntry> of = new OfflineFlight<SimFDRFlightReport, ACARSRouteEntry>();
		
		// Build a flight data entry
		FlightInfo inf = new FlightInfo(0);
		inf.setVersion(StringUtils.parse(re.getAttributeValue("version"), 2));
		inf.setClientBuild(StringUtils.parse(re.getAttributeValue("build"), 1));
		inf.setBeta(StringUtils.parse(re.getAttributeValue("beta"), 0));
		inf.setAirportD(SystemData.getAirport(ie.getChildTextTrim("airportD")));
		inf.setAirportA(SystemData.getAirport(ie.getChildTextTrim("airportA")));
		inf.setAirportL(SystemData.getAirport(ie.getChildTextTrim("airportL")));
		inf.setRemoteAddr(ie.getChildTextTrim("remoteAddr"));
		inf.setRemoteHost(ie.getChildTextTrim("remoteHost"));
		inf.setFSVersion(Simulator.fromName(ie.getChildTextTrim("fs_ver")));
		inf.setFDR(Recorder.SIMFDR);
		inf.setFlightCode(cs);
		of.setInfo(inf);
		
		// Build a flight data entry
		Flight f = FlightCodeParser.parse(cs);
		SimFDRFlightReport afr = new SimFDRFlightReport(f.getAirline(), f.getFlightNumber(), f.getLeg());
		afr.setBeta(StringUtils.parse(re.getAttributeValue("beta"), 0));
		afr.setAircraftCode(ie.getChildTextTrim("equipment"));
		afr.setStartTime(StringUtils.parseDate(ie.getChildTextTrim("startTime"), "MM/dd/yyyy HH:mm:ss"));
		afr.setEndTime(StringUtils.parseDate(ie.getChildTextTrim("shutdownTime"), "MM/dd/yyyy HH:mm:ss"));
		afr.setAirportD(SystemData.getAirport(ie.getChildTextTrim("airportD")));
		afr.setAirportA(SystemData.getAirport(ie.getChildTextTrim("airportA")));
		afr.setRoute(ie.getChildTextTrim("route"));
		afr.setRemarks(ie.getChildTextTrim("remarks"));
		afr.setFSVersion(inf.getFSVersion());
		afr.setSubmittedOn(new Date());
		afr.setHasReload(Boolean.valueOf(ie.getChildTextTrim("hasRestore")).booleanValue());
		afr.setFDE(ae.getChildTextTrim("airFile"));
		afr.setSDK(ae.getChildTextTrim("sdk"));
		afr.setNetwork(OnlineNetwork.fromName(ie.getChildTextTrim("network")));
		
		// Build the position entries
		Element ppe = re.getChild("positions");
		List<Element> pL = (ppe != null) ? ppe.getChildren("position") : null;
		if (!CollectionUtils.isEmpty(pL) && (pL != null)) {
			for (Iterator<Element> i = pL.iterator(); i.hasNext();) {
				Element pe = i.next();
				try {
					GeoLocation loc = new GeoPosition(parse(pe.getChildTextTrim("lat")), parse(pe.getChildTextTrim("lon")));
					ACARSRouteEntry pos = new ACARSRouteEntry(StringUtils.parseDate(pe.getChildTextTrim("date"), "MM/dd/yyyy HH:mm:ss.SSS"), loc);
					pos.setAltitude(StringUtils.parse(pe.getChildTextTrim("msl"), 0));
					pos.setRadarAltitude(StringUtils.parse(pe.getChildTextTrim("agl"), 0));
					pos.setHeading(StringUtils.parse(pe.getChildTextTrim("hdg"), 0));
					pos.setAirSpeed(StringUtils.parse(pe.getChildTextTrim("aSpeed"), 0));
					pos.setGroundSpeed(StringUtils.parse(pe.getChildTextTrim("gSpeed"), 0));
					pos.setVerticalSpeed(StringUtils.parse(pe.getChildTextTrim("vSpeed"), 0));
					pos.setPitch(parse(pe.getChildTextTrim("pitch")));
					pos.setBank(parse(pe.getChildTextTrim("bank")));
					pos.setMach(parse(pe.getChildTextTrim("mach")));
					pos.setN1(parse(pe.getChildTextTrim("n1")));
					pos.setN2(parse(pe.getChildTextTrim("n2")));
					pos.setAOA(parse(pe.getChildTextTrim("aoa")));
					pos.setG(parse(pe.getChildTextTrim("g")));
					pos.setFuelFlow(StringUtils.parse(pe.getChildTextTrim("fuelFlow"), 0));
					pos.setPhase(StringUtils.parse(pe.getChildTextTrim("phase"), 0));
					pos.setSimRate(StringUtils.parse(pe.getChildTextTrim("simRate"), 0));
					pos.setFlaps(StringUtils.parse(pe.getChildTextTrim("flaps"), 0));
					pos.setFuelRemaining(StringUtils.parse(pe.getChildTextTrim("fuel"), 0));
					pos.setWindHeading(StringUtils.parse(pe.getChildTextTrim("wHdg"), 0));
					pos.setWindSpeed(StringUtils.parse(pe.getChildTextTrim("wSpeed"), 0));
					pos.setFrameRate(StringUtils.parse(pe.getChildTextTrim("frameRate"), 0));
					pos.setFlags(StringUtils.parse(pe.getChildTextTrim("flags"), 0));
					pos.setNAV1(pe.getChildTextTrim("nav1"));
					pos.setNAV2(pe.getChildTextTrim("nav2"));
					of.addPosition(pos);
				} catch (NumberFormatException nfe) {
					log.error("Error parsing value - " + nfe.getMessage());
				} catch (Exception e) {
					log.error("Error loading Position Report - " + e.getMessage());
				}
			}
		}
		
		// Set the times
		afr.setStartTime(StringUtils.parseDate(ie.getChildTextTrim("startTime"), "MM/dd/yyyy HH:mm:ss"));
		afr.setTaxiTime(StringUtils.parseDate(ie.getChildTextTrim("taxiOutTime"), "MM/dd/yyyy HH:mm:ss"));
		afr.setTakeoffTime(StringUtils.parseDate(ie.getChildTextTrim("takeoffTime"), "MM/dd/yyyy HH:mm:ss"));
		afr.setLandingTime(StringUtils.parseDate(ie.getChildTextTrim("landingTime"), "MM/dd/yyyy HH:mm:ss"));
		afr.setEndTime(StringUtils.parseDate(ie.getChildTextTrim("gateTime"), "MM/dd/yyyy HH:mm:ss"));
		inf.setStartTime(afr.getStartTime());
		inf.setEndTime(afr.getEndTime());

		// Set the weights/speeds
		afr.setTaxiFuel(StringUtils.parse(ie.getChildTextTrim("taxiFuel"), 0));
		afr.setTaxiWeight(StringUtils.parse(ie.getChildTextTrim("taxiWeight"), 0));
		afr.setTakeoffFuel(StringUtils.parse(ie.getChildTextTrim("takeoffFuel"), 0));
		afr.setTakeoffWeight(StringUtils.parse(ie.getChildTextTrim("takeoffWeight"), 0));
		afr.setTakeoffSpeed(StringUtils.parse(ie.getChildTextTrim("takeoffSpeed"), 0));
		afr.setLandingFuel(StringUtils.parse(ie.getChildTextTrim("landingFuel"), 0));
		afr.setLandingWeight(StringUtils.parse(ie.getChildTextTrim("landingWeight"), 0));
		afr.setLandingSpeed(StringUtils.parse(ie.getChildTextTrim("landingSpeed"), 0));
		afr.setLandingVSpeed(StringUtils.parse(ie.getChildTextTrim("landingVSpeed"), 0));
		afr.setLandingG(StringUtils.parse(ie.getChildTextTrim("landingG"), 0.0d));
		afr.setLandingCategory(ILSCategory.get(ie.getChildTextTrim("landingCat")));
		afr.setGateFuel(StringUtils.parse(ie.getChildTextTrim("gateFuel"), 0));
		afr.setGateWeight(StringUtils.parse(ie.getChildTextTrim("gateWeight"), 0));
		
		// Set the takeoff/ladning positions
		afr.setTakeoffN1(parse(ie.getChildTextTrim("takeoffN1")));
		afr.setTakeoffHeading(StringUtils.parse(ie.getChildTextTrim("takeoffHeading"), -1));
		afr.setTakeoffLocation(new GeoPosition(parse(ie.getChildTextTrim("takeoffLatitude")), parse(ie.getChildTextTrim("takeoffLongitude")), StringUtils.parse(ie.getChildTextTrim("takeoffAltitude"), 0)));
		afr.setLandingN1(parse(ie.getChildTextTrim("landingN1")));
		afr.setLandingHeading(StringUtils.parse(ie.getChildTextTrim("landingHeading"), -1));
		afr.setLandingLocation(new GeoPosition(parse(ie.getChildTextTrim("landingLatitude")), parse(ie.getChildTextTrim("landingLongitude")), StringUtils.parse(ie.getChildTextTrim("landingAltitude"), 0)));

		// Load the 0X/1X/2X/4X times
		afr.setTime(0, StringUtils.parse(ie.getChildTextTrim("time0X"), 0));
		afr.setTime(1, StringUtils.parse(ie.getChildTextTrim("time1X"), 0));
		afr.setTime(2, StringUtils.parse(ie.getChildTextTrim("time2X"), 0));
		afr.setTime(4, StringUtils.parse(ie.getChildTextTrim("time4X"), 0));
		of.setFlightReport(afr);
		return of;
	}
}