// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.navdata.TerminalRoute;
import org.deltava.beans.schedule.Aircraft;
import org.deltava.beans.schedule.ScheduleEntry;
import org.deltava.beans.testing.CheckRide;
import org.deltava.beans.testing.Test;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.crypt.MessageDigester;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to allow users to submit Offline Flight Reports.
 * @author Luke
 * @version 2.4
 * @since 2.4
 */

public class OfflineFlightCommand extends AbstractCommand {
	
	private static final Logger log = Logger.getLogger(OfflineFlightCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get command result and check for post
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/acars/offlineSubmit.jsp");
		
		// Get the XML and SHA
		FileUpload xmlF = ctx.getFile("xml");
		FileUpload shaF = ctx.getFile("hashCode");
		if (xmlF == null) {
			result.setSuccess(true);
			return;
		}
		
		// Convert the files to strings
		OfflineFlight flight = null; 
		try {
			String sha = new String(shaF.getBuffer(), "UTF-8").trim();
			String xml = new String(xmlF.getBuffer(), "UTF-8");
			xml = xml.substring(0, xml.length() - 2);
		
			// Validate the SHA
			boolean noValidate = (ctx.isUserInRole("HR") || ctx.isSuperUser()) && Boolean.valueOf(ctx.getParameter("noValidate")).booleanValue();
			if (!noValidate) {
				MessageDigester md = new MessageDigester(SystemData.get("security.hash.acars.algorithm"));
				md.salt(SystemData.get("security.hash.acars.salt"));
				String calcHash = MessageDigester.convert(md.digest(xml.getBytes()));
				if (!calcHash.equals(sha)) {
					log.warn("ACARS Hash mismatch - expected " + sha + ", calculated " + calcHash);
					ctx.setAttribute("hashFailure", Boolean.TRUE, REQUEST);
					return;
				}
			}
		
			// Parse the flight
			flight = OfflineFlightParser.create(xml);
		} catch (Exception e) {
			log.error("Error parsing XML - " + e.getMessage(), e);
			ctx.setMessage(e.getMessage());
			return;
		}

		// Add connection fields from the request
		ConnectionEntry ce = flight.getConnection();
		ce.setPilotID(ctx.getUser().getID());
		ce.setRemoteHost(ctx.getRequest().getRemoteHost());
		ce.setRemoteAddr(ctx.getRequest().getRemoteAddr());
		
		// Convert the PIREP date into the user's local time zone
		FlightInfo inf = flight.getInfo();
		DateTime dt = new DateTime(inf.getEndTime());
		dt.convertTo(ctx.getUser().getTZ());

		// Add PIREP fields from the request
		ACARSFlightReport afr = flight.getFlightReport();
		afr.setDatabaseID(FlightReport.DBID_PILOT, ctx.getUser().getID());
		afr.setRank(ctx.getUser().getRank());
		afr.setDate(dt.getDate());
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the user information
			GetPilot pdao = new GetPilot(con);
			GetPilot.invalidateID(ctx.getUser().getID());
			Pilot p = pdao.get(ctx.getUser().getID());
			
			// Get the SID/STAR data
			GetNavRoute nvdao = new GetNavRoute(con);
			inf.setSID(nvdao.getRoute(afr.getAirportD(), TerminalRoute.SID, flight.getSID()));
			inf.setSTAR(nvdao.getRoute(afr.getAirportA(), TerminalRoute.STAR, flight.getSTAR()));
			
			// Check for Draft PIREPs by this Pilot
			GetFlightReports prdao = new GetFlightReports(con);
			List<FlightReport> dFlights = prdao.getDraftReports(p.getID(), afr.getAirportD(), afr.getAirportA(),
					SystemData.get("airline.db"));
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
				else if ((a.getMaxLandingWeight() != 0) && (afr.getLandingWeight() > a.getMaxLandingWeight()))
					afr.setAttribute(FlightReport.ATTR_WEIGHTWARN, true);
			}
			
			// Check if it's a Flight Academy flight
			GetSchedule sdao = new GetSchedule(con);
			ScheduleEntry sEntry = sdao.get(afr);
			afr.setAttribute(FlightReport.ATTR_ACADEMY, ((sEntry != null) && sEntry.getAcademy()));

			// Check the schedule database and check the route pair
			int avgHours = sdao.getFlightTime(afr.getAirportD(), afr.getAirportA());
			if ((avgHours == 0) && !inf.isScheduleValidated())
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
			if (!CollectionUtils.isEmpty(flight.getPositions()))
				awdao.writePositions(inf.getID(), flight.getPositions());
			
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
			ctx.setAttribute("pirep", afr, REQUEST);
			
			// Commit
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}