// Copyright 2011, 2012, 2014, 2015, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.xacars;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.time.*;
import java.sql.Connection;
import java.io.IOException;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.event.Event;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.testing.*;

import org.deltava.dao.*;

import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * The XACARS Flight Report Web Service. 
 * @author Luke
 * @version 9.0
 * @since 4.1
 */

public class XPIREPService extends XAService {
	
	private static final Logger log = Logger.getLogger(XPIREPService.class);

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		List<String> data = StringUtils.split(ctx.getParameter("DATA2"), "~");
		if (data == null)
			throw error(SC_BAD_REQUEST, "No DATA2 parameter", false);
		
		Flight f = FlightCodeParser.parse(data.get(2));
		if (f.getAirline() == null)
			f.setAirline(SystemData.getAirline(SystemData.get("airline.code")));
		
		Pilot usr = null;
		try {
			usr = authenticate(ctx, data.get(0), data.get(1));
			CacheManager.invalidate("Pilots", usr.cacheKey());
			Connection con = ctx.getConnection();
			
			// Load flight
			GetXACARS xdao = new GetXACARS(con);
			int flightID = xdao.getID(usr.getID(), f);
			XAFlightInfo inf = xdao.getFlight(flightID);
			if (flightID == 0)
				throw new InvalidDataException("Flight not Started in XACARS");
			else if (inf == null)
				throw new InvalidDataException("Invalid Flight ID - " + flightID);
			
			// Set alternate if we have it and network
			inf.setAirportL(SystemData.getAirport(data.get(8)));
			try {
				inf.setNetwork(OnlineNetwork.valueOf(data.get(16).toUpperCase()));
			} catch (IllegalArgumentException iae) {
				inf.setNetwork(null);
			}
			
			// Convert the flight information to a PIREP
			OfflineFlight<XACARSFlightReport, XARouteEntry> ofl = XACARSFlightHelper.build(inf);
			FlightInfo fi = ofl.getInfo();
			fi.setRemoteHost(ctx.getRequest().getRemoteHost());
			fi.setRemoteAddr(ctx.getRequest().getRemoteAddr());
			
			// Get XACARS version
			String ver = getProtocolVersion(ctx);
			int pos = ver.indexOf('.');
			fi.setClientBuild(StringUtils.parse(ver.substring(0, pos), 1));
			fi.setBeta(StringUtils.parse(ver.substring(pos + 1), 0));
			if (fi.getClientBuild() < SystemData.getInt("acars.xacars.protocol.major", 1))
				throw new InvalidDataException("Invalid XACARS protocol version - " + ver);
			
			// Create comments field
			Collection<String> comments = new LinkedHashSet<String>();
			
			// Check for Draft PIREPs by this Pilot
			XACARSFlightReport xfr = ofl.getFlightReport();
			xfr.setRank(usr.getRank());
			xfr.setMajorVersion(fi.getClientBuild());
			xfr.setMinorVersion(fi.getBeta());
			GetFlightReports prdao = new GetFlightReports(con);
			List<FlightReport> dFlights = prdao.getDraftReports(usr.getID(), xfr, SystemData.get("airline.db"));
			if (!dFlights.isEmpty()) {
				FlightReport fr = dFlights.get(0);
				xfr.setID(fr.getID());
				xfr.setDatabaseID(DatabaseID.ASSIGN, fr.getDatabaseID(DatabaseID.ASSIGN));
				xfr.setDatabaseID(DatabaseID.EVENT, fr.getDatabaseID(DatabaseID.EVENT));
				xfr.setAttribute(FlightReport.ATTR_CHARTER, fr.hasAttribute(FlightReport.ATTR_CHARTER));
				if (!StringUtils.isEmpty(fr.getComments()))
					comments.add(fr.getComments());
			}
			
			Duration timeS = Duration.between(inf.getStartTime(), inf.getEndTime());
			xfr.setLength((int)(timeS.getSeconds() / 360));
			
			// Check for a check ride
			GetExam exdao = new GetExam(con);
			CheckRide cr = exdao.getCheckRide(usr.getID(), xfr.getEquipmentType(), TestStatus.NEW);
			if (cr != null) {
				cr.setStatus(TestStatus.SUBMITTED);
				cr.setSubmittedOn(Instant.now());
				xfr.setAttribute(FlightReport.ATTR_CHECKRIDE, true);
			}
			
			// Check if this Flight Report counts for promotion
			GetEquipmentType eqdao = new GetEquipmentType(con);
			Collection<String> promoEQ = eqdao.getPrimaryTypes(SystemData.get("airline.db"), xfr.getEquipmentType());

			// Loop through the eq types, not all may have the same minimum promotion stage length!!
			if (promoEQ.contains(usr.getEquipmentType())) {
				FlightPromotionHelper helper = new FlightPromotionHelper(xfr);
				for (Iterator<String> i = promoEQ.iterator(); i.hasNext(); ) {
					String pType = i.next();
					EquipmentType pEQ = eqdao.get(pType, SystemData.get("airline.db"));
					boolean isOK = helper.canPromote(pEQ);
					if (!isOK) {
						i.remove();
						comments.add("Not eligible for promotion: " + helper.getLastComment());
					}
				}
				
				xfr.setCaptEQType(promoEQ);
			}
			
			// Check that the user has an online network ID
			OnlineNetwork network = xfr.getNetwork();
			if ((network != null) && (!usr.hasNetworkID(network))) {
				comments.add("SYSTEM: No " + network.toString() + " ID, resetting Online Network flag");
				xfr.setNetwork(null);
			} else if ((network == null) && (xfr.getDatabaseID(DatabaseID.EVENT) != 0)) {
				comments.add("SYSTEM: Filed offline, resetting Online Event flag");
				xfr.setDatabaseID(DatabaseID.EVENT, 0);
			}
			
			// Check if it's an Online Event flight
			GetEvent evdao = new GetEvent(con);
			if ((xfr.getDatabaseID(DatabaseID.EVENT) == 0) && (xfr.hasAttribute(FlightReport.ATTR_ONLINE_MASK))) {
				int eventID = evdao.getPossibleEvent(xfr);
				if (eventID != 0) {
					Event e = evdao.get(eventID);
					comments.add("SYSTEM: Detected participation in " + e.getName() + " Online Event");
					xfr.setDatabaseID(DatabaseID.EVENT, eventID);
				}
			}
			
			// Check that the event hasn't expired
			if (xfr.getDatabaseID(DatabaseID.EVENT) != 0) {
				Event e = evdao.get(xfr.getDatabaseID(DatabaseID.EVENT));
				if (e != null) {
					long timeSinceEnd = (System.currentTimeMillis() - e.getEndTime().toEpochMilli()) / 3600_000;
					if (timeSinceEnd > 24) {
						log.warn("Flight logged over 24 hours after Event completion");
						comments.add("SYSTEM: Flight logged over 24 hours after Event completion");
						xfr.setDatabaseID(DatabaseID.EVENT, 0);
					}
				} else
					xfr.setDatabaseID(DatabaseID.EVENT, 0);
			}
			
			// Check if the user is rated to fly the aircraft
			EquipmentType eq = eqdao.get(usr.getEquipmentType());
			if (!usr.getRatings().contains(xfr.getEquipmentType()) && !eq.getRatings().contains(xfr.getEquipmentType()))
				xfr.setAttribute(FlightReport.ATTR_NOTRATED, !xfr.hasAttribute(FlightReport.ATTR_CHECKRIDE));
			
			// Check for historic aircraft
			GetAircraft acdao = new GetAircraft(con);
			Aircraft a = acdao.get(xfr.getEquipmentType());
			if (a == null) {
				log.warn("Invalid equipment type from " + usr.getName() + " - " + xfr.getEquipmentType());
				xfr.setRemarks(xfr.getRemarks() + " (Invalid equipment: " + xfr.getEquipmentType());
				xfr.setEquipmentType(usr.getEquipmentType());
			} else {
				AircraftPolicyOptions opts = a.getOptions(SystemData.get("airline.code"));
				xfr.setAttribute(FlightReport.ATTR_HISTORIC, a.getHistoric());
				
				// Check for excessive distance
				if (xfr.getDistance() > opts.getRange())
					xfr.setAttribute(FlightReport.ATTR_RANGEWARN, true);

				// Check for excessive weight
				if ((a.getMaxTakeoffWeight() != 0) && (xfr.getTakeoffWeight() > a.getMaxTakeoffWeight()))
					xfr.setAttribute(FlightReport.ATTR_WEIGHTWARN, true);
				else if ((a.getMaxLandingWeight() != 0) && (xfr.getLandingWeight() > a.getMaxLandingWeight()))
					xfr.setAttribute(FlightReport.ATTR_WEIGHTWARN, true);
			}
			
			// Check if it's a Flight Academy flight
			GetSchedule sdao = new GetSchedule(con);
			ScheduleEntry sEntry = sdao.get(xfr);
			xfr.setAttribute(FlightReport.ATTR_ACADEMY, ((sEntry != null) && sEntry.getAcademy()));
			
			// Check the schedule database and check the route pair
			FlightTime avgHours = sdao.getFlightTime(xfr);
			if ((avgHours.getType() == RoutePairType.UNKNOWN) && !xfr.hasAttribute(FlightReport.ATTR_CHARTER))
				xfr.setAttribute(FlightReport.ATTR_ROUTEWARN, true);
			else {
				int minHours = (int) ((avgHours.getFlightTime() * 0.75) - (SystemData.getDouble("users.pirep.pad_hours", 0) * 10));
				int maxHours = (int) ((avgHours.getFlightTime() * 1.15) + (SystemData.getDouble("users.pirep.pad_hours", 0) * 10));
				if ((xfr.getLength() < minHours) || (xfr.getLength() > maxHours))
					xfr.setAttribute(FlightReport.ATTR_TIMEWARN, true);
			}
			
			// Save comments
			if (!comments.isEmpty())
				xfr.setComments(StringUtils.listConcat(comments, "\r\n"));
			
			// Load the departure runway
			GetNavRoute navdao = new GetNavRoute(con);
			if (xfr.getTakeoffHeading() > -1) {
				LandingRunways lr = navdao.getBestRunway(inf.getAirportD(), xfr.getSimulator(), xfr.getTakeoffLocation(), xfr.getTakeoffHeading());
				Runway r = lr.getBestRunway();
				if (r != null) {
					int dist = r.distanceFeet(xfr.getTakeoffLocation());
					fi.setRunwayD(new RunwayDistance(r, dist));
				}
			}

			// Load the arrival runway
			if (xfr.getLandingHeading() > -1) {
				LandingRunways lr = navdao.getBestRunway(xfr.getAirportA(), xfr.getSimulator(), xfr.getLandingLocation(), xfr.getLandingHeading());
				Runway r = lr.getBestRunway();
				if (r != null) {
					int dist = r.distanceFeet(xfr.getLandingLocation());
					fi.setRunwayA(new RunwayDistance(r, dist));
				}
			}
			
			// Parse the route
			List<String> wps = StringUtils.nullTrim(StringUtils.split(inf.getRoute(), " "));
			wps.remove(inf.getAirportD().getICAO());
			wps.remove(inf.getAirportA().getICAO());
			
			// Get the SID
			if (wps.size() > 2) {
				String name = wps.get(0);
				TerminalRoute sid = navdao.getBestRoute(inf.getAirportD(), TerminalRoute.Type.SID, TerminalRoute.makeGeneric(name), wps.get(1), fi.getRunwayD());
				if (sid != null) {
					wps.remove(0);
					fi.setSID(sid);
				}
			}
			
			// Get the STAR
			if (wps.size() > 2) {
				String name = wps.get(wps.size() - 1);
				TerminalRoute star = navdao.getBestRoute(inf.getAirportA(), TerminalRoute.Type.STAR, TerminalRoute.makeGeneric(name), wps.get(wps.size() - 2), fi.getRunwayA());
				if (star != null) {
					wps.remove(wps.size() - 1);
					fi.setSTAR(star);
				}
			}
			
			// Start transaction
			ctx.startTX();
			
			// Write the ACARS data
			SetACARSRunway awdao = new SetACARSRunway(con);
			awdao.createFlight(fi);
			awdao.writeSIDSTAR(fi.getID(), fi.getSID());
			awdao.writeSIDSTAR(fi.getID(), fi.getSTAR());
			awdao.writeRunways(fi.getID(), fi.getRunwayD(), fi.getRunwayA());
			xfr.setDatabaseID(DatabaseID.ACARS, fi.getID());
			
			// Write position data
			SetXACARS xwdao = new SetXACARS(con);
			xwdao.archive(flightID, fi.getID());
			
			// Write the PIREP and nuke the temp data
			xwdao.delete(flightID);
			SetFlightReport fwdao = new SetFlightReport(con);
			fwdao.write(xfr);
			fwdao.writeACARS(xfr, SystemData.get("airline.db"));
			if (fwdao.updatePaxCount(xfr.getID()))
				log.warn("Update Passnger count for PIREP #" + xfr.getID());
			
			// Write the check ride if necessary
			if ((cr != null) && xfr.hasAttribute(FlightReport.ATTR_CHECKRIDE)) {
				SetExam exwdao = new SetExam(con);
				cr.setFlightID(fi.getID());
				exwdao.write(cr);
			}
			
			// Commit and ACK
			ctx.commitTX();
			ctx.print("1|Flight Report Saved");
		} catch (Exception e) {
			log.error(((usr == null) ? "Anonymous" : usr.getName()) + " - " + e.getMessage(), e);
			ctx.rollbackTX();
			ctx.print("0|" + e.getMessage());
		} finally {
			ctx.release();
		}
		
		// Write response
		try {
			ctx.setContentType("text/plain", "UTF-8");
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_INTERNAL_SERVER_ERROR, "I/O Error", false);
		}
		
		return SC_OK;
	}
}