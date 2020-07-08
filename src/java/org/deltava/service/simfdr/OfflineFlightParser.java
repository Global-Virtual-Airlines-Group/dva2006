// Copyright 2009, 2010, 2011, 2012, 2014, 2015, 2016, 2017, 2018, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.simfdr;

import java.util.*;
import java.time.*;
import java.util.stream.Collectors;

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
 * @version 9.0
 * @since 7.0
 */

final class OfflineFlightParser {
	
	private static final Logger log = Logger.getLogger(OfflineFlightParser.class);

	// singleton
	private OfflineFlightParser() {
		super();
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
		
		// Get the flight information
		Element re = doc.getRootElement();
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
		inf.setIsACARS64Bit(Boolean.valueOf(re.getAttributeValue("is64Bit")).booleanValue());
		inf.setBeta(StringUtils.parse(re.getAttributeValue("beta"), 0));
		inf.setAirportD(SystemData.getAirport(ie.getChildTextTrim("airportD")));
		inf.setAirportA(SystemData.getAirport(ie.getChildTextTrim("airportA")));
		inf.setAirportL(SystemData.getAirport(ie.getChildTextTrim("airportL")));
		inf.setRemoteAddr(ie.getChildTextTrim("remoteAddr"));
		inf.setRemoteHost(ie.getChildTextTrim("remoteHost"));
		inf.setFlightCode(cs);
		inf.setRoute(ie.getChildTextTrim("route"));
		inf.setAutopilotType(EnumUtils.parse(AutopilotType.class, ie.getChildTextTrim("autopilotType"), AutopilotType.DEFAULT));
		of.setInfo(inf);
		
		// Load simulator
		String sim = ie.getChildTextTrim("sim");
		inf.setSimulator(Simulator.fromName(sim, Simulator.UNKNOWN));
		inf.setFDR(Recorder.SIMFDR); // needs to be set after simulator
		inf.setSimulatorVersion(StringUtils.parse(ie.getChildTextTrim("simMajor"), 0), StringUtils.parse(ie.getChildTextTrim("simMinor"), 0));
		if (inf.getSimulator() == Simulator.UNKNOWN)
			log.warn("Unknown simulator version - " + sim);
		
		// Build a flight data entry
		Flight f = FlightCodeParser.parse(cs); 
		if (f.getAirline() == null) 
			f.setAirline(SystemData.getAirline(SystemData.get("airline.code")));

		SimFDRFlightReport afr = new SimFDRFlightReport(f.getAirline(), f.getFlightNumber(), f.getLeg());
		afr.setAttribute(FlightReport.ATTR_SIMFDR, true);
		afr.setStatus(FlightStatus.SUBMITTED);
		afr.setBeta(StringUtils.parse(re.getAttributeValue("beta"), 0));
		afr.setStartTime(StringUtils.parseEpoch(ie.getChildTextTrim("startTime")));
		afr.setEndTime(StringUtils.parseEpoch(ie.getChildTextTrim("shutdownTime")));
		afr.setAirportD(inf.getAirportD());
		afr.setAirportA(inf.getAirportA());
		afr.setRoute(ie.getChildTextTrim("route"));
		afr.setRemarks(ie.getChildTextTrim("remarks"));
		afr.setSimulator(inf.getSimulator());
		afr.setDate(Instant.now());
		afr.setSubmittedOn(afr.getDate());
		afr.setHasReload(Boolean.valueOf(ie.getChildTextTrim("hasRestore")).booleanValue());
		afr.setFDE(ie.getChildTextTrim("airFile"));
		afr.setSDK(ie.getChildTextTrim("sdk"));
		afr.setCapabilities(StringUtils.parse(ie.getAttributeValue("capabilities", "0"), 0, true));
		afr.setNetwork(OnlineNetwork.fromName(ie.getChildTextTrim("network")));
		afr.setRemarks(ie.getChildTextTrim("remarks"));
		afr.setClientBuild(inf.getClientBuild());
		afr.setBeta(inf.getBeta());
		
		// Get the equipment type plus IATA codes
		Element eqe = ie.getChild("aircraft");
		afr.setAircraftCode(eqe.getAttributeValue("icao"));
		List<Element> ice = eqe.getChildren("iata");
		afr.setIATACodes(StringUtils.listConcat(ice.stream().map(e -> e.getAttributeValue("code")).collect(Collectors.toSet()), ","));
		
		// Build the position entries
		Element ppe = re.getChild("positions");
		List<Element> pL = (ppe != null) ? ppe.getChildren("position") : null;
		if (!CollectionUtils.isEmpty(pL) && (pL != null)) {
			for (Element pe : pL) {
				try {
					of.addPosition(parsePosition(pe));
				} catch (Exception e) {
					log.error("Error loading Position Report - " + e.getMessage(), e);
				}
			}
		}
		
		// Set the times
		afr.setStartTime(StringUtils.parseEpoch(ie.getChildTextTrim("startTime")));
		afr.setTaxiTime(StringUtils.parseEpoch(ie.getChildTextTrim("taxiOutTime")));
		afr.setTakeoffTime(StringUtils.parseEpoch(ie.getChildTextTrim("takeoffTime")));
		afr.setLandingTime(StringUtils.parseEpoch(ie.getChildTextTrim("landingTime")));
		afr.setEndTime(StringUtils.parseEpoch(ie.getChildTextTrim("endTime")));
		afr.setDepartureTime(StringUtils.parseEpoch(ie.getChildTextTrim("startSimTime")));
		afr.setArrivalTime(StringUtils.parseEpoch(ie.getChildTextTrim("gateSimTime")));
		inf.setStartTime(afr.getStartTime());
		inf.setEndTime(afr.getEndTime());

		// Set the weights/speeds
		afr.setTaxiFuel(StringUtils.parse(ie.getChildTextTrim("taxiFuel"), 0));
		afr.setTaxiWeight(StringUtils.parse(ie.getChildTextTrim("taxiWeight"), 0));
		afr.setTakeoffFuel(StringUtils.parse(ie.getChildTextTrim("takeoffFuel"), 0));
		afr.setTakeoffWeight(StringUtils.parse(ie.getChildTextTrim("takeoffWeight"), 0));
		afr.setLandingFuel(StringUtils.parse(ie.getChildTextTrim("landingFuel"), 0));
		afr.setLandingG(StringUtils.parse(ie.getChildTextTrim("landingG"), 0.0d));
		afr.setLandingCategory(ILSCategory.get(ie.getChildTextTrim("landingCat")));
		afr.setGateFuel(StringUtils.parse(ie.getChildTextTrim("gateFuel"), 0));
		afr.setGateWeight(StringUtils.parse(ie.getChildTextTrim("gateWeight"), 0));
		afr.setPaxWeight(StringUtils.parse(ie.getChildTextTrim("paxWeight"), 0));
		afr.setCargoWeight(StringUtils.parse(ie.getChildTextTrim("cargoWeight"), 0));
		afr.setPassengers(StringUtils.parse(ie.getChildTextTrim("pax"), 0));
		
		// Set the takeoff position
		afr.setTakeoffN1(Double.parseDouble(ie.getChildTextTrim("takeoffN1")));
		afr.setTakeoffSpeed(StringUtils.parse(ie.getChildTextTrim("takeoffSpeed"), 0));
		Element tpe = ie.getChild("takeoff");
		if (tpe != null) {
			afr.setTakeoffLocation(new GeoPosition(Double.parseDouble(tpe.getAttributeValue("lat")), Double.parseDouble(tpe.getAttributeValue("lng")), StringUtils.parse(tpe.getAttributeValue("alt"), 0)));
			afr.setTakeoffHeading(StringUtils.parse(tpe.getAttributeValue("hdg"), -1));
		}
		
		// Set the landing position
		afr.setLandingN1(Double.parseDouble(ie.getChildTextTrim("landingN1")));
		afr.setLandingWeight(StringUtils.parse(ie.getChildTextTrim("landingWeight"), 0));
		afr.setLandingSpeed(StringUtils.parse(ie.getChildTextTrim("landingSpeed"), 0));
		afr.setLandingVSpeed(StringUtils.parse(ie.getChildTextTrim("landingVSpeed"), 0));
		Element lpe = ie.getChild("landing");
		if (lpe != null) {
			afr.setLandingLocation(new GeoPosition(Double.parseDouble(lpe.getAttributeValue("lat")), Double.parseDouble(lpe.getAttributeValue("lng")), StringUtils.parse(lpe.getAttributeValue("alt"), 0)));
			afr.setLandingHeading(StringUtils.parse(lpe.getAttributeValue("hdg"), -1));
		}
		
		// Load the 0X/1X/2X/4X times
		afr.setTime(0, StringUtils.parse(ie.getChildTextTrim("time0X"), 0));
		afr.setTime(1, StringUtils.parse(ie.getChildTextTrim("time1X"), 0));
		afr.setTime(2, StringUtils.parse(ie.getChildTextTrim("time2X"), 0));
		afr.setTime(4, StringUtils.parse(ie.getChildTextTrim("time4X"), 0));
		of.setFlightReport(afr);
		return of;
	}
	
	/**
	 * Parses a simFDR XML position entry.
	 * @param pe the position Element
	 * @return an ACARSRouteEntry
	 */
	static ACARSRouteEntry parsePosition(Element pe) {
		
		GeoLocation loc = new GeoPosition(Double.parseDouble(pe.getAttributeValue("lat")), Double.parseDouble(pe.getAttributeValue("lng")));
		ACARSRouteEntry pos = new ACARSRouteEntry(StringUtils.parseEpoch(pe.getAttributeValue("date")), loc);
		pos.setVASFree(StringUtils.parse(pe.getAttributeValue("vasFree", "0"), 0));
		pos.setSimUTC(StringUtils.parseEpoch(pe.getAttributeValue("simDate")));
		if (pos.getSimUTC() == null)
			pos.setSimUTC(pos.getDate());
		
		pos.setAltitude(StringUtils.parse(pe.getChildTextTrim("msl"), 0));
		pos.setRadarAltitude(StringUtils.parse(pe.getChildTextTrim("agl"), 0));
		pos.setHeading(StringUtils.parse(pe.getChildTextTrim("hdg"), 0));
		pos.setAirSpeed(StringUtils.parse(pe.getChildTextTrim("aSpeed"), 0));
		pos.setGroundSpeed(StringUtils.parse(pe.getChildTextTrim("gSpeed"), 0));
		pos.setVerticalSpeed(StringUtils.parse(pe.getChildTextTrim("vSpeed"), 0));
		pos.setPitch(Double.parseDouble(pe.getChildTextTrim("pitch")));
		pos.setBank(Double.parseDouble(pe.getChildTextTrim("bank")));
		pos.setMach(Double.parseDouble(pe.getChildTextTrim("mach")));
		pos.setN1(Double.parseDouble(pe.getChildTextTrim("avgN1")));
		pos.setN2(Double.parseDouble(pe.getChildTextTrim("avgN2")));
		pos.setAOA(Double.parseDouble(pe.getChildTextTrim("aoa")));
		pos.setG(Double.parseDouble(pe.getChildTextTrim("g")));
		pos.setCG(Double.parseDouble(pe.getChildTextTrim("cg")));
		pos.setFuelFlow(StringUtils.parse(pe.getChildTextTrim("fuelFlow"), 0));
		pos.setPhase(FlightPhase.fromString(pe.getChildTextTrim("phase")));
		pos.setSimRate(StringUtils.parse(pe.getChildTextTrim("simRate"), 0));
		pos.setFlaps(StringUtils.parse(pe.getChildTextTrim("flaps"), 0));
		pos.setFuelRemaining(StringUtils.parse(pe.getChildTextTrim("totalFuel"), 0));
		pos.setWindHeading(StringUtils.parse(pe.getChildTextTrim("windHdg"), 0));
		pos.setWindSpeed(StringUtils.parse(pe.getChildTextTrim("windSpeed"), 0));
		pos.setPressure(StringUtils.parse(pe.getChildTextTrim("pressure"), 0));
		pos.setVisibility(StringUtils.parse(pe.getChildTextTrim("viz"), 0.0d));
		pos.setTemperature(StringUtils.parse(pe.getChildTextTrim("temp"), 0));
		pos.setFrameRate(StringUtils.parse(pe.getChildTextTrim("frameRate"), 0));
		pos.setFlags(StringUtils.parse(pe.getChildTextTrim("flags"), 0));
		pos.setGroundOperations(StringUtils.parse(pe.getChildTextTrim("groundOps"), 0));
		pos.setNetworkConnected(Boolean.valueOf(pe.getChildTextTrim("networkConnected")).booleanValue());
		pos.setNAV1(pe.getChildTextTrim("nav1"));
		pos.setNAV2(pe.getChildTextTrim("nav2"));
		pos.setADF1(pe.getChildTextTrim("adf1"));
		return pos;
	}
}