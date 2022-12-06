// Copyright 2009, 2010, 2011, 2012, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;
import java.time.*;
import java.time.format.*;
import java.io.StringReader;
import java.time.temporal.ChronoField;

import org.apache.log4j.Logger;

import org.jdom2.*;
import org.jdom2.input.*;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.system.OperatingSystem;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A utility class to parse XML-format offline Flight Reports.
 * @author Luke
 * @version 10.3
 * @since 2.4
 */

@Helper(OfflineFlight.class)
public final class OfflineFlightParser {
	
	private static final Logger log = Logger.getLogger(OfflineFlightParser.class);
	
	// singleton
	private OfflineFlightParser() {
		super();
	}
	
	private static double parse(String xml) {
		String x = xml.contains(",") ? xml.replace(',', '.') : xml;
		return Double.parseDouble(x);
	}
	
	private static Instant safeParseInstant(String dt, String fmt) {
		try {
			return StringUtils.parseInstant(dt, fmt);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Parses an Offline Flight XML document.
	 * @param xml the XML to parse
	 * @return an OfflineFlight bean
	 */
	public static OfflineFlight<ACARSFlightReport, ACARSRouteEntry> create(String xml) {
		
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
		int clientVersion = StringUtils.parse(re.getAttributeValue("version"), 2);
		if (clientBuild < 80)
			clientVersion = 1;
		
		// Get aircraft information
		Element ae = re.getChild("aircraft");
		if (ae == null)
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
		OfflineFlight<ACARSFlightReport, ACARSRouteEntry> result = new OfflineFlight<ACARSFlightReport, ACARSRouteEntry>();
		
		// Build a flight data entry
		int flightID = StringUtils.parse(ie.getChildTextTrim("id"), 0);
		FlightInfo inf = new FlightInfo(flightID);
		inf.setAuthorID(StringUtils.parse(ie.getChildTextTrim("userID"), 0));
		inf.setVersion(clientVersion);
		inf.setClientBuild(clientBuild);
		inf.setOffline(true);
		inf.setAutopilotType(EnumUtils.parse(AutopilotType.class, ie.getChildTextTrim("autopilotType"), AutopilotType.DEFAULT));
		inf.setBeta(StringUtils.parse(re.getAttributeValue("beta"), 0));
		inf.setEquipmentType(ie.getChildTextTrim("equipment"));
		inf.setStartTime(StringUtils.parseInstant(ie.getChildTextTrim("startTime"), "MM/dd/yyyy HH:mm:ss"));
		inf.setEndTime(StringUtils.parseInstant(ie.getChildTextTrim("shutdownTime"), "MM/dd/yyyy HH:mm:ss"));
		inf.setAirportD(SystemData.getAirport(ie.getChildTextTrim("airportD")));
		inf.setAirportA(SystemData.getAirport(ie.getChildTextTrim("airportA")));
		inf.setAltitude(ie.getChildTextTrim("altitude"));
		inf.setRoute(ie.getChildTextTrim("route"));
		inf.setRemarks(ie.getChildTextTrim("remarks"));
		inf.setScheduleValidated(Boolean.parseBoolean(ie.getChildTextTrim("schedOK")));
		inf.setDispatcherID(StringUtils.parse(ie.getChildTextTrim("dispatcherID"), 0));
		inf.setRouteID(StringUtils.parse(ie.getChildTextTrim("dispatchRouteID"), 0));
		inf.setPassengers(StringUtils.parse(ie.getChildTextTrim("pax"), 0));
		inf.setSeats(StringUtils.parse(ie.getChildTextTrim("seats"), 0));
		inf.setLoadFactor(StringUtils.parse(ie.getChildTextTrim("loadFactor"), 0.0d));
		result.setSID(ie.getChildTextTrim("sid"));
		result.setSTAR(ie.getChildTextTrim("star"));
		
		// Load dispatcher type (180+)
		boolean isDispatch = Boolean.parseBoolean(ie.getChildTextTrim("dispatchRoute"));
		inf.setDispatcher(isDispatch ? DispatchType.DISPATCH : EnumUtils.parse(DispatchType.class, ie.getChildTextTrim("dispatcher"), DispatchType.NONE));
		result.setInfo(inf);
		
		// Load simulator and platform
		inf.setPlatform(OperatingSystem.values()[StringUtils.parse(ie.getChildTextTrim("platform"), 0)]);
		String sim = ie.getChildTextTrim("fs_ver");
		inf.setSimulator(Simulator.fromName(sim, Simulator.UNKNOWN));
		inf.setIsSim64Bit(Boolean.parseBoolean(ie.getChildTextTrim("is64Bit")));
		inf.setIsACARS64Bit(Boolean.parseBoolean(ie.getChildTextTrim("isACARS64Bit")));
		inf.setFDR(Recorder.ACARS); // need to set after sim
		if (inf.getSimulator() == Simulator.UNKNOWN)
			log.warn("Unknown simulator version - " + sim);
		
		// Load sim major/minor
		String simVersion = ie.getChildTextTrim("simVersion"); 
		if (!StringUtils.isEmpty(simVersion)) {
			int vpos = simVersion.indexOf('.');
			int major = StringUtils.parse(simVersion.substring(0, vpos), 0); int minor = StringUtils.parse(simVersion.substring(vpos + 1), 0);
			if (major != 0)
				inf.setSimulatorVersion(major, minor);
		} else if (inf.getSimulator() == Simulator.FS9)
			inf.setSimulatorVersion(9, 1);
		else if (inf.getSimulator() == Simulator.FS2002)
			inf.setSimulatorVersion(8, 0);
		
		// Create formatter
		DateTimeFormatter mdtf = new DateTimeFormatterBuilder().appendPattern("MM/dd/yyyy HH:mm:ss").appendFraction(ChronoField.MILLI_OF_SECOND, 0, 3, true).toFormatter();
		
		// Build the position entries
		Element ppe = re.getChild("positions");
		List<Element> pL = (ppe != null) ? ppe.getChildren("position") : null;
		if (pL != null) {
			for (Element pe : pL) {
				try {
					GeoLocation loc = new GeoPosition(parse(pe.getChildTextTrim("lat")), parse(pe.getChildTextTrim("lon")));
					ACARSRouteEntry pos = new ACARSRouteEntry(LocalDateTime.parse(pe.getChildTextTrim("date"), mdtf).toInstant(ZoneOffset.UTC), loc);
					pos.setVASFree(StringUtils.parse(pe.getAttributeValue("vasFree", "0"), 0));
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
					pos.setCG(Double.parseDouble(pe.getChildTextTrim("cg")));
					pos.setFuelFlow(StringUtils.parse(pe.getChildTextTrim("fuelFlow"), 0));
					pos.setPhase(FlightPhase.fromString(pe.getChildTextTrim("phase")));
					pos.setSimRate(StringUtils.parse(pe.getChildTextTrim("simRate"), 0));
					pos.setFlaps(StringUtils.parse(pe.getChildTextTrim("flaps"), 0));
					pos.setFuelRemaining(StringUtils.parse(pe.getChildTextTrim("fuel"), 0));
					pos.setWindHeading(StringUtils.parse(pe.getChildTextTrim("wHdg"), 0));
					pos.setWindSpeed(StringUtils.parse(pe.getChildTextTrim("wSpeed"), 0));
					pos.setTemperature(StringUtils.parse(pe.getChildText("temp"), 0));
					pos.setPressure(StringUtils.parse(pe.getChildText("pressure"), 0));
					pos.setVisibility(StringUtils.parse(pe.getChildTextTrim("viz"), 0.0));
					pos.setFrameRate(StringUtils.parse(pe.getChildTextTrim("frameRate"), 0));
					pos.setFlags(StringUtils.parse(pe.getChildTextTrim("flags"), 0));
					pos.setGroundOperations(StringUtils.parse(pe.getChildTextTrim("groundOps"), 0));
					pos.setNetworkConnected(Boolean.parseBoolean(pe.getChildTextTrim("networkConnected")));
					pos.setRestoreCount(StringUtils.parse(pe.getChildTextTrim("restoreCount"), 0));
					pos.setNAV1(pe.getChildTextTrim("nav1"));
					pos.setNAV2(pe.getChildTextTrim("nav2"));
					
					// Load simDate
					String sd = pe.getChildTextTrim("simDate");
					pos.setSimUTC((sd == null) ? pos.getDate() : LocalDateTime.parse(sd, mdtf).toInstant(ZoneOffset.UTC));
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
		afr.setAttribute(FlightReport.ATTR_DISPATCH, (inf.getDispatcher() == DispatchType.DISPATCH));
		afr.setAttribute(FlightReport.ATTR_SIMBRIEF, (inf.getDispatcher() == DispatchType.SIMBRIEF));
		afr.setSimulator(inf.getSimulator());
		afr.setStatus(FlightStatus.SUBMITTED);
		afr.setSubmittedOn(Instant.now());
		afr.setAirportD(inf.getAirportD());
		afr.setAirportA(inf.getAirportA());
		afr.setRestoreCount(StringUtils.parse(ie.getChildTextTrim("restoreCount"), 0));
		afr.setRemarks(inf.getRemarks());
		afr.setEquipmentType(inf.getEquipmentType());
		inf.setAirline(afr.getAirline());
		inf.setFlight(afr.getFlightNumber());
		afr.setAircraftCode(ie.getChildTextTrim("code"));
		afr.setFDE(ae.getChildTextTrim("airFile"));
		afr.setAuthor(ae.getChildTextTrim("author"));
		afr.setAircraftPath(ae.getChildTextTrim("acPath"));
		afr.setSDK(ae.getChildTextTrim("sdk"));
		afr.setTailCode(ae.getChildTextTrim("tailCode"));
		afr.setCapabilities(StringUtils.parse(ae.getAttributeValue("capabilities", "0"), 0, true));
		afr.setClientBuild(inf.getClientBuild());
		afr.setBeta(inf.getBeta());
		afr.setNetwork(EnumUtils.parse(OnlineNetwork.class, ie.getChildTextTrim("network"), null));
		
		// Check if it's a checkride
		afr.setAttribute(FlightReport.ATTR_CHECKRIDE, Boolean.parseBoolean(ie.getChildTextTrim("checkRide")));
		
		// Parse status messages
		Element msgsE = re.getChild("msgs");
		List<Element> mL = (msgsE != null) ? msgsE.getChildren("msg") : null;
		if (mL != null) {
			for (Element mE : mL) {
				Instant dt = LocalDateTime.parse(mE.getAttributeValue("time"), mdtf).toInstant(ZoneOffset.UTC);
				FlightHistoryEntry fhe = new FlightHistoryEntry(0, EnumUtils.parse(HistoryType.class, mE.getAttributeValue("type"), HistoryType.USER), afr.getAuthorID(), dt, XMLUtils.getChildText(mE, "msg"));
				afr.addStatusUpdate(fhe);
			}
		}

		// Set the times
		afr.setStartTime(StringUtils.parseInstant(ie.getChildTextTrim("startTime"), "MM/dd/yyyy HH:mm:ss"));
		afr.setTaxiTime(StringUtils.parseInstant(ie.getChildTextTrim("taxiOutTime"), "MM/dd/yyyy HH:mm:ss"));
		afr.setTakeoffTime(StringUtils.parseInstant(ie.getChildTextTrim("takeoffTime"), "MM/dd/yyyy HH:mm:ss"));
		afr.setTOCTime(safeParseInstant(ie.getChildTextTrim("tocTime"), "MM/dd/yyyy HH:mm:ss"));
		afr.setTODTime(safeParseInstant(ie.getChildTextTrim("todTime"), "MM/dd/yyyy HH:mm:ss"));
		afr.setLandingTime(StringUtils.parseInstant(ie.getChildTextTrim("landingTime"), "MM/dd/yyyy HH:mm:ss"));
		afr.setEndTime(StringUtils.parseInstant(ie.getChildTextTrim("gateTime"), "MM/dd/yyyy HH:mm:ss"));
		afr.setDepartureTime(safeParseInstant(ie.getChildTextTrim("startSimTime"), "MM/dd/yyyy HH:mm:ss"));
		afr.setArrivalTime(safeParseInstant(ie.getChildTextTrim("gateSimTime"), "MM/dd/yyyy HH:mm:ss"));

		// Set the weights/speeds
		afr.setPaxWeight(StringUtils.parse(ie.getChildTextTrim("paxWeight"), 0));
		afr.setCargoWeight(StringUtils.parse(ie.getChildTextTrim("cargoWeight"), 0));
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
		
		// Set the Takeoff/Landing N1 values, but don't fail on invalid numeric values
		try {
			afr.setTakeoffN1(parse(ie.getChildTextTrim("takeoffN1")));
			afr.setLandingN1(parse(ie.getChildTextTrim("landingN1")));
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Invalid N1 - " + nfe.getMessage());
		} catch (IllegalArgumentException iae) {
			// empty
		}
		
		// Set the takeoff position if present
		afr.setTakeoffHeading(StringUtils.parse(ie.getChildTextTrim("takeoffHeading"), -1));
		if (afr.getTakeoffHeading() > -1) {
			double lat = parse(ie.getChildTextTrim("takeoffLatitude"));
			double lng = parse(ie.getChildTextTrim("takeoffLongitude"));
			afr.setTakeoffLocation(new GeoPosition(lat, lng, Math.min(24000, StringUtils.parse(ie.getChildTextTrim("takeoffAltitude"), 0))));
		}
		
		// Set the landing position if present
		afr.setLandingHeading(StringUtils.parse(ie.getChildTextTrim("landingHeading"), -1));
		if (afr.getLandingHeading() > -1) {
			double lat = parse(ie.getChildTextTrim("landingLatitude"));
			double lng = parse(ie.getChildTextTrim("landingLongitude"));
			afr.setLandingLocation(new GeoPosition(lat, lng, Math.min(24000, StringUtils.parse(ie.getChildTextTrim("landingAltitude"), 0))));
		}

		// Load the 0X/1X/2X/4X times
		afr.setTime(0, StringUtils.parse(ie.getChildTextTrim("time0X"), 0));
		afr.setTime(1, StringUtils.parse(ie.getChildTextTrim("time1X"), 0));
		afr.setTime(2, StringUtils.parse(ie.getChildTextTrim("time2X"), 0));
		afr.setTime(4, StringUtils.parse(ie.getChildTextTrim("time4X"), 0));
		afr.setBoardTime(StringUtils.parse(ie.getChildTextTrim("timeBoard"), 0));
		afr.setDeboardTime(StringUtils.parse(ie.getChildTextTrim("timeDeboard"), 0));
		afr.setOnlineTime(StringUtils.parse(ie.getChildText("timeOnline"), 0));
		result.setFlightReport(afr);
		return result;
	}
}