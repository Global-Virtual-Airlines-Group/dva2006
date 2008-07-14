// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.apache.log4j.Logger;

import org.jdom.*;
import org.jdom.input.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.testing.*;
import org.deltava.beans.navdata.TerminalRoute;

import org.deltava.crypt.MessageDigester;

import org.deltava.dao.*;

import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to handle posting of offline ACARS Flight Reports.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class FlightReportService extends WebService {

	private static final Logger log = Logger.getLogger(FlightReportService.class);

	private double parse(String xml) {
		if (xml.contains(","))
			xml = xml.replace(',', '.');
		
		return Double.parseDouble(xml);
	}
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the SHA hash and XML
		String xml = ctx.getParameter("xml");
		String sha = ctx.getParameter("hashCode");
		if (xml == null)
			throw error(SC_BAD_REQUEST, "No Flight Information");

		// Validate the SHA
		MessageDigester md = new MessageDigester(SystemData.get("security.hash.acars.algorithm"));
		md.salt(SystemData.get("security.hash.acars.salt"));
		String calcHash = MessageDigester.convert(md.digest(xml.getBytes()));
		if (!calcHash.equals(sha)) {
			log.warn("ACARS Hash mismatch - expected " + sha + ", calculated " + calcHash);
			throw error(SC_BAD_REQUEST, "SHA mismatch");
		}

		// Get the XML
		Document doc = null;
		try {
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(new StringReader(xml));
		} catch (Exception e) {
			throw error(SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}

		// Parse out the XML and get the build
		Element re = doc.getRootElement();
		int clientBuild = StringUtils.parse(re.getAttributeValue("build"), 76);
		
		// Get aircraft information
		Element ae = re.getChild("aircraft");
		if ((ae == null) && (clientBuild > 92))
			throw error(SC_BAD_REQUEST, "No Aircraft Information");

		// Get the flight information
		Element ie = re.getChild("info");
		if (ie == null)
			throw error(SC_BAD_REQUEST, "No Flight Information");

		// Build the flight entry
		Airline al = SystemData.getAirline(ie.getChildTextTrim("airline"));
		int flight = StringUtils.parse(ie.getChildTextTrim("flight"), 1);
		int leg = StringUtils.parse(ie.getChildTextTrim("leg"), 1);

		// Build a connection entry
		ConnectionEntry ce = new ConnectionEntry(IDGenerator.generate());
		ce.setPilotID(ctx.getUser().getID());
		ce.setStartTime(new Date());
		ce.setRemoteHost(ctx.getRequest().getRemoteHost());
		ce.setRemoteAddr(ctx.getRequest().getRemoteAddr());
		ce.setClientBuild(clientBuild);
		ce.setBeta(StringUtils.parse(re.getAttributeValue("beta"), 0));

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

		// Build the position entries
		Element ppe = re.getChild("positions");
		List pL = (ppe != null) ? ppe.getChildren("position") : null;
		Collection<RouteEntry> positions = new ArrayList<RouteEntry>();
		if (!CollectionUtils.isEmpty(pL)) {
			for (Iterator i = pL.iterator(); i.hasNext();) {
				Element pe = (Element) i.next();
				String dt = pe.getChildTextTrim("date");
				if (dt.indexOf('.') == -1)
					dt = dt + ".000";

				// Build a position entry
				try {
					RouteEntry pos = new RouteEntry(StringUtils.parseDate(dt, "MM/dd/yyyy HH:mm:ss.SSS"), 
							parse(pe.getChildTextTrim("lat")), parse(pe.getChildTextTrim("lon")));
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
					positions.add(pos);
				} catch (NumberFormatException nfe) {
					log.error("Error parsing value - " + nfe.getMessage());
				} catch (Exception e) {
					log.error("Error loading Position Report - " + e.getMessage());
				}
			}
		}

		// Convert the PIREP date into the user's local time zone
		DateTime dt = new DateTime(new Date());
		dt.convertTo(ctx.getUser().getTZ());

		// Build the PIREP entry
		ACARSFlightReport afr = new ACARSFlightReport(al, flight, leg);
		afr.setAttribute(FlightReport.ATTR_ACARS, true);
		afr.setDatabaseID(FlightReport.DBID_PILOT, ctx.getUser().getID());
		afr.setFSVersion(inf.getFSVersion());
		afr.setRank(ctx.getUser().getRank());
		afr.setStatus(FlightReport.SUBMITTED);
		afr.setDate(dt.getDate());
		afr.setSubmittedOn(afr.getDate());
		afr.setAirportD(inf.getAirportD());
		afr.setAirportA(inf.getAirportA());
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
		else if (OnlineNetwork.FPI.equals(network))
			afr.setAttribute(FlightReport.ATTR_FPI, true);

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

		try {
			Connection con = ctx.getConnection();

			// Get the user information
			GetPilot pdao = new GetPilot(con);
			GetPilot.invalidateID(ctx.getUser().getID());
			Pilot p = pdao.get(ctx.getUser().getID());
			
			// Get the SID/STAR data
			GetNavRoute nvdao = new GetNavRoute(con);
			inf.setSID(nvdao.getRoute(afr.getAirportD(), TerminalRoute.SID, ie.getChildTextTrim("sid")));
			inf.setSTAR(nvdao.getRoute(afr.getAirportA(), TerminalRoute.STAR, ie.getChildTextTrim("star")));

			// Check for Draft PIREPs by this Pilot
			GetFlightReports prdao = new GetFlightReports(con);
			List<FlightReport> dFlights = prdao.getDraftReports(p.getID(), afr.getAirportD(), afr.getAirportA(), SystemData.get("airline.db"));
			if (!dFlights.isEmpty()) {
				FlightReport fr = dFlights.get(0);
				afr.setID(fr.getID());
				afr.setDatabaseID(FlightReport.DBID_ASSIGN, fr.getDatabaseID(FlightReport.DBID_ASSIGN));
				afr.setDatabaseID(FlightReport.DBID_EVENT, fr.getDatabaseID(FlightReport.DBID_EVENT));
				afr.setAttribute(FlightReport.ATTR_CHARTER, fr.hasAttribute(FlightReport.ATTR_CHARTER));
				afr.setComments(fr.getComments());
			}

			// Check if this Flight Report counts for promotion
			GetEquipmentType eqdao = new GetEquipmentType(con);
			Collection<String> promoEQ = eqdao.getPrimaryTypes(SystemData.get("airline.db"), afr.getEquipmentType());
			if (promoEQ.contains(p.getEquipmentType()))
				afr.setCaptEQType(promoEQ);

			// Check if the user is rated to fly the aircraft
			EquipmentType eq = eqdao.get(p.getEquipmentType());
			if (!p.getRatings().contains(afr.getEquipmentType()) && !eq.getRatings().contains(afr.getEquipmentType()))
				afr.setAttribute(FlightReport.ATTR_NOTRATED, !afr.hasAttribute(FlightReport.ATTR_CHECKRIDE));

			// Check for historic aircraft
			GetAircraft acdao = new GetAircraft(con);
			Aircraft a = acdao.get(afr.getEquipmentType());
			afr.setAttribute(FlightReport.ATTR_HISTORIC, (a != null) && (a.getHistoric()));
			if (a == null) {
				log.warn("Invalid equipment type from " + p.getName() + " - " + afr.getEquipmentType());
				afr.setRemarks(afr.getRemarks() + " (Invalid equipment: " + afr.getEquipmentType());
				afr.setEquipmentType(p.getEquipmentType());
			} else {
				// Check for excessive distance
				if (afr.getDistance() > a.getRange())
					afr.setAttribute(FlightReport.ATTR_RANGEWARN, true);
				
				// Check for excessive weight
				if ((a.getMaxTakeoffWeight() != 0) && (afr.getTakeoffWeight() > a.getMaxTakeoffWeight())) 
					afr.setAttribute(FlightReport.ATTR_WEIGHTWARN, true);
				else if ((a.getMaxLandingWeight()  != 0) && (afr.getLandingWeight() > a.getMaxLandingWeight()))
					afr.setAttribute(FlightReport.ATTR_WEIGHTWARN, true);
			}

			// Check if it's a Flight Academy flight
			GetSchedule sdao = new GetSchedule(con);
			ScheduleEntry sEntry = sdao.get(afr);
			afr.setAttribute(FlightReport.ATTR_ACADEMY, ((sEntry != null) && sEntry.getAcademy()));

			// Check the schedule database and check the route pair
			boolean schedValidated = Boolean.valueOf(ie.getChildTextTrim("schedOK")).booleanValue();
			int avgHours = sdao.getFlightTime(afr.getAirportD(), afr.getAirportA());
			if ((avgHours == 0) && (!schedValidated))
				afr.setAttribute(FlightReport.ATTR_ROUTEWARN, true);
			else if (avgHours > 0) {
				int minHours = (int) ((avgHours * 0.75) - (SystemData.getDouble("users.pirep.pad_hours", 0) * 10));
				int maxHours = (int) ((avgHours * 1.15) + (SystemData.getDouble("users.pirep.pad_hours", 0) * 10));
				if ((afr.getLength() < minHours) || (afr.getLength() > maxHours))
					afr.setAttribute(FlightReport.ATTR_TIMEWARN, true);
			}

			// Turn off auto-commit
			ctx.startTX();

			// Write the connection/info records
			SetACARSData awdao = new SetACARSData(con);
			awdao.createConnection(ce);
			awdao.createFlight(inf);
			awdao.writeSIDSTAR(inf.getID(), inf.getSID());
			awdao.writeSIDSTAR(inf.getID(), inf.getSTAR());
			afr.setDatabaseID(FlightReport.DBID_ACARS, inf.getID());

			// Dump the positions
			if (!CollectionUtils.isEmpty(positions))
				awdao.writePositions(inf.getID(), positions);

			// Update the checkride record (don't assume pilots check the box, because they don't)
			GetExam exdao = new GetExam(con);
			CheckRide cr = exdao.getCheckRide(p.getID(), afr.getEquipmentType(), Test.NEW);
			if (cr != null) {
				cr.setFlightID(inf.getID());
				cr.setSubmittedOn(new Date());
				cr.setStatus(Test.SUBMITTED);

				// Update the checkride
				SetExam wdao = new SetExam(con);
				wdao.write(cr);
			} else
				afr.setAttribute(FlightReport.ATTR_CHECKRIDE, false);

			// Write the PIREP
			SetFlightReport fwdao = new SetFlightReport(con);
			fwdao.write(afr);
			fwdao.writeACARS(afr, SystemData.get("airline.db"));

			// Commit
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Return back success
		return SC_OK;
	}

	/**
	 * Returns wether this web service requires authentication.
	 * @return TRUE always
	 */
	public final boolean isSecure() {
		return true;
	}
}