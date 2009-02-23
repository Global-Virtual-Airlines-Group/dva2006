// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;
import java.io.StringReader;

import org.apache.log4j.Logger;

import org.jdom.*;
import org.jdom.input.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.schedule.*;

import org.deltava.util.system.SystemData;

/**
 * A utility class to parse XML-format offline Flight Reports.
 * @author Luke
 * @version 2.4
 * @since 2.4
 */

public class OfflineFlightParser {
	
	private static final Logger log = Logger.getLogger(OfflineFlightParser.class);

	private OfflineFlightParser() {
		super();
	}
	
	private static double parse(String xml) {
		if (xml.contains(","))
			xml = xml.replace(',', '.');
		
		return Double.parseDouble(xml);
	}
	
	/**
	 * Parses an Offline Flight XML document.
	 * @param xml the XML to parse
	 * @return an OfflineFlight bean
	 */
	public static OfflineFlight create(String xml) {
		
		// Get the XML
		Document doc = null;
		try {
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(new StringReader(xml));
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		
		// Parse out the XML and get the build
		Element re = doc.getRootElement();
		int clientBuild = StringUtils.parse(re.getAttributeValue("build"), 76);
		
		// Get aircraft information
		Element ae = re.getChild("aircraft");
		if ((ae == null) && (clientBuild > 92))
			throw new IllegalArgumentException("No Aircraft Information");

		// Get the flight information
		Element ie = re.getChild("info");
		if (ie == null)
			throw new IllegalArgumentException("No Flight Information");
		
		// Build the flight entry
		int flight = StringUtils.parse(ie.getChildTextTrim("flight"), 1);
		int leg = StringUtils.parse(ie.getChildTextTrim("leg"), 1);
		Airline al = SystemData.getAirline(ie.getChildTextTrim("airline"));
		if (al == null)
			al = SystemData.getAirline(SystemData.get("airline.code"));
		
		// Create the resulting bean
		OfflineFlight result = new OfflineFlight();
		
		// Build a connection entry
		ConnectionEntry ce = new ConnectionEntry(IDGenerator.generate());
		ce.setStartTime(new Date());
		ce.setClientBuild(clientBuild);
		ce.setBeta(StringUtils.parse(re.getAttributeValue("beta"), 0));
		result.setConnection(ce);
		
		// Build a flight data entry
		FlightInfo inf = new FlightInfo(ce.getID());
		inf.setEquipmentType(ie.getChildTextTrim("equipment"));
		inf.setStartTime(StringUtils.parseDate(ie.getChildTextTrim("startTime"), "MM/dd/yyyy HH:mm:ss"));
		inf.setEndTime(StringUtils.parseDate(ie.getChildTextTrim("shutdownTime"), "MM/dd/yyyy HH:mm:ss"));
		inf.setAirportD(SystemData.getAirport(ie.getChildTextTrim("airportD")));
		inf.setAirportA(SystemData.getAirport(ie.getChildTextTrim("airportA")));
		inf.setAltitude(ie.getChildTextTrim("altitude"));
		inf.setRoute(ie.getChildTextTrim("route"));
		inf.setRemarks(ie.getChildTextTrim("remarks"));
		inf.setFSVersion(StringUtils.parse(ie.getChildTextTrim("fs_ver"), 2004));
		inf.setOffline(Boolean.valueOf(ie.getChildTextTrim("offline")).booleanValue());
		inf.setScheduleValidated(Boolean.valueOf(ie.getChildTextTrim("schedOK")).booleanValue());
		result.setSID(ie.getChildTextTrim("sid"));
		result.setSTAR(ie.getChildTextTrim("star"));
		result.setInfo(inf);
		
		// Build the position entries
		Element ppe = re.getChild("positions");
		List pL = (ppe != null) ? ppe.getChildren("position") : null;
		if (!CollectionUtils.isEmpty(pL)) {
			for (Iterator i = pL.iterator(); i.hasNext();) {
				Element pe = (Element) i.next();
				String dt = pe.getChildTextTrim("date");
				if (dt.indexOf('.') == -1)
					dt = dt + ".000";

				// Build a position entry
				try {
					GeoLocation loc = new GeoPosition(parse(pe.getChildTextTrim("lat")), parse(pe.getChildTextTrim("lon")));
					RouteEntry pos = new RouteEntry(StringUtils.parseDate(dt, "MM/dd/yyyy HH:mm:ss.SSS"), loc);
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
					result.addPosition(pos);
				} catch (NumberFormatException nfe) {
					log.error("Error parsing value - " + nfe.getMessage());
				} catch (Exception e) {
					log.error("Error loading Position Report - " + e.getMessage());
				}
			}
		}
		
		// Build the PIREP entry
		ACARSFlightReport afr = new ACARSFlightReport(al, flight, leg);
		afr.setAttribute(FlightReport.ATTR_ACARS, true);
		afr.setFSVersion(inf.getFSVersion());
		afr.setStatus(FlightReport.SUBMITTED);
		afr.setSubmittedOn(afr.getDate());
		afr.setAirportD(inf.getAirportD());
		afr.setAirportA(inf.getAirportA());
		afr.setHasReload(Boolean.valueOf(ie.getChildTextTrim("hasRestore")).booleanValue());
		afr.setRemarks("OFFLINE PIREP: " + inf.getRemarks());
		afr.setEquipmentType(inf.getEquipmentType());
		inf.setFlightCode(afr.getFlightCode());
		afr.setAircraftCode(ie.getChildTextTrim("code"));
		if (ae != null)
			afr.setFDE(ae.getChildTextTrim("fde"));
		
		// Check if it's a checkride
		afr.setAttribute(FlightReport.ATTR_CHECKRIDE, Boolean.valueOf(ie.getChildTextTrim("checkRide")).booleanValue());

		// Get the online network
		String network = ie.getChildTextTrim("network").toUpperCase();
		if (OnlineNetwork.VATSIM.equals(network))
			afr.setAttribute(FlightReport.ATTR_VATSIM, true);
		else if (OnlineNetwork.IVAO.equals(network))
			afr.setAttribute(FlightReport.ATTR_IVAO, true);
		
		// Set the times
		afr.setStartTime(StringUtils.parseDate(ie.getChildTextTrim("startTime"), "MM/dd/yyyy HH:mm:ss"));
		afr.setTaxiTime(StringUtils.parseDate(ie.getChildTextTrim("taxiOutTime"), "MM/dd/yyyy HH:mm:ss"));
		afr.setTakeoffTime(StringUtils.parseDate(ie.getChildTextTrim("takeoffTime"), "MM/dd/yyyy HH:mm:ss"));
		afr.setLandingTime(StringUtils.parseDate(ie.getChildTextTrim("landingTime"), "MM/dd/yyyy HH:mm:ss"));
		afr.setEndTime(StringUtils.parseDate(ie.getChildTextTrim("gateTime"), "MM/dd/yyyy HH:mm:ss"));

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
		afr.setGateFuel(StringUtils.parse(ie.getChildTextTrim("gateFuel"), 0));
		afr.setGateWeight(StringUtils.parse(ie.getChildTextTrim("gateWeight"), 0));
		
		// Set the Takeoff/Landing N1 values, but don't fail on invalid numeric values
		try {
			afr.setTakeoffN1(parse(ie.getChildTextTrim("takeoffN1")));
			afr.setLandingN1(parse(ie.getChildTextTrim("landingN1")));
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Invalid N1 - " + nfe.getMessage());
		} catch (IllegalArgumentException iae) {
			// empty
		}

		// Load the 0X/1X/2X/4X times
		afr.setTime(0, StringUtils.parse(ie.getChildTextTrim("time0X"), 0));
		afr.setTime(1, StringUtils.parse(ie.getChildTextTrim("time1X"), 0));
		afr.setTime(2, StringUtils.parse(ie.getChildTextTrim("time2X"), 0));
		afr.setTime(4, StringUtils.parse(ie.getChildTextTrim("time4X"), 0));
		result.setFlightReport(afr);
		return result;
	}
}