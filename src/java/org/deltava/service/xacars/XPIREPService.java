// Copyright 2011, 2012, 2014, 2015, 2016, 2017, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.xacars;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.time.*;
import java.sql.Connection;
import java.io.IOException;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.testing.*;

import org.deltava.dao.*;

import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * The XACARS Flight Report Web Service. 
 * @author Luke
 * @version 10.1
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
			
			// Check for Draft PIREPs by this Pilot
			XACARSFlightReport xfr = ofl.getFlightReport();
			xfr.setRank(usr.getRank());
			xfr.setMajorVersion(fi.getClientBuild());
			xfr.setMinorVersion(fi.getBeta());
			xfr.addStatusUpdate(xfr.getAuthorID(), HistoryType.LIFECYCLE, "Submitted via XACARS");
			GetFlightReports prdao = new GetFlightReports(con);
			List<FlightReport> dFlights = prdao.getDraftReports(usr.getID(), xfr, ctx.getDB());
			if (!dFlights.isEmpty()) {
				FlightReport fr = dFlights.get(0);
				xfr.setID(fr.getID());
				xfr.setDatabaseID(DatabaseID.ASSIGN, fr.getDatabaseID(DatabaseID.ASSIGN));
				xfr.setDatabaseID(DatabaseID.EVENT, fr.getDatabaseID(DatabaseID.EVENT));
				xfr.setAttribute(FlightReport.ATTR_CHARTER, fr.hasAttribute(FlightReport.ATTR_CHARTER));
				if (!StringUtils.isEmpty(fr.getComments()))
					xfr.setComments(fr.getComments());
			}
			
			Duration timeS = Duration.between(inf.getStartTime(), inf.getEndTime());
			xfr.setLength((int)(timeS.getSeconds() / 360));
			
			// Get the flight submission helper
			FlightSubmissionHelper fsh = new FlightSubmissionHelper(con, xfr, usr);
			fsh.setAirlineInfo(SystemData.get("airline.code"), ctx.getDB());
			
			// Check for draft flight reports
			fsh.checkFlightReports();
			
			// Check for a check ride
			GetExam exdao = new GetExam(con);
			CheckRide cr = exdao.getCheckRide(usr.getID(), xfr.getEquipmentType(), TestStatus.NEW);
			if (cr != null) {
				cr.setStatus(TestStatus.SUBMITTED);
				cr.setSubmittedOn(Instant.now());
				xfr.setAttribute(FlightReport.ATTR_CHECKRIDE, true);
			}
			
			// Check if the pilot is rated in the equipment type
			fsh.checkRatings();
			fsh.checkAircraft();
			
			// Check Online status, and Online Event
			fsh.checkOnlineNetwork();
			fsh.checkOnlineEvent();
			
			// Check for a Flight Tour
			fsh.checkTour();
			
			// Check the schedule database and check the route pair
			fsh.checkSchedule();

			// Load the departure runway
			GetNavRoute navdao = new GetNavRoute(con);
			if (xfr.getTakeoffHeading() > -1) {
				LandingRunways lr = navdao.getBestRunway(inf.getAirportD(), xfr.getSimulator(), xfr.getTakeoffLocation(), xfr.getTakeoffHeading());
				Runway r = lr.getBestRunway();
				if (r != null)
					fi.setRunwayD(new RunwayDistance(r, r.distanceFeet(xfr.getTakeoffLocation())));
			}

			// Load the arrival runway
			if (xfr.getLandingHeading() > -1) {
				LandingRunways lr = navdao.getBestRunway(xfr.getAirportA(), xfr.getSimulator(), xfr.getLandingLocation(), xfr.getLandingHeading());
				Runway r = lr.getBestRunway();
				if (r != null)
					fi.setRunwayA(new RunwayDistance(r, r.distanceFeet(xfr.getLandingLocation())));
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
			fwdao.writeACARS(xfr, ctx.getDB());
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