// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.TerminalRoute;
import org.deltava.beans.schedule.*;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.crypt.MessageDigester;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import org.gvagroup.acars.ACARSClientInfo;
import org.gvagroup.common.SharedData;

/**
 * A Web Site Command to allow users to submit Offline Flight Reports.
 * @author Luke
 * @version 2.7
 * @since 2.4
 */

public class OfflineFlightCommand extends AbstractCommand {
	
	private static final Logger log = Logger.getLogger(OfflineFlightCommand.class);
	private static final int MAX_XML_SIZE = 4096 * 1024;

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get command result and check for post
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/acars/offlineSubmit.jsp");
		
		// Check for a ZIP archive
		String sha = null; String xml = null;
		FileUpload zipF = ctx.getFile("zip");
		if (zipF != null) {
			try {
				ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipF.getBuffer()));
				ZipEntry ze = zis.getNextEntry();
				while ((ze != null) && ((sha == null) || (xml == null))) {
					String name = ze.getName().toLowerCase();
					ByteArrayOutputStream out = new ByteArrayOutputStream(102400);
					byte[] buffer = new byte[16384];
					int bytesRead = zis.read(buffer);
					while (bytesRead > -1) {
						out.write(buffer, 0, bytesRead);
						bytesRead = zis.read(buffer);
					}
						
					if (name.endsWith(".xml"))
						xml = new String(out.toByteArray(), "UTF-8");
					else if (name.endsWith(".sha"))
						sha = new String(out.toByteArray(), "UTF-8").trim();
					
					ze = zis.getNextEntry();
				}
			
				zis.close();
			} catch (Exception e) {
				ctx.setMessage("Cannot process ZIP file - " + e.getMessage());
				result.setSuccess(true);
				return;
			}
		}
		
		// Get the XML and SHA
		FileUpload xmlF = ctx.getFile("xml");
		FileUpload shaF = ctx.getFile("hashCode");
		if ((xmlF == null) && (xml == null)) {
			result.setSuccess(true);
			return;
		}
		
		// Check for SHA
		if ((shaF == null) && (sha == null)) {
			ctx.setMessage("No SHA-256 signature");
			result.setSuccess(true);
			return;
		}
		
		// Convert the files to strings
		OfflineFlight flight = null;
		boolean noValidate = (ctx.isUserInRole("HR") || ctx.isSuperUser()) && Boolean.valueOf(ctx.getParameter("noValidate")).booleanValue();
		try {
			if (sha == null)
				sha = new String(shaF.getBuffer(), "UTF-8").trim();
			if (xml == null)
				xml = new String(xmlF.getBuffer(), "UTF-8");
			
			// Sanity check the length
			if (xml.length() > MAX_XML_SIZE)
				throw new IllegalArgumentException("XML too large - " + StringUtils.format(xml.length(), ctx.getUser().getNumberFormat()) + " bytes");
		
			// Validate the SHA
			if (!noValidate) {
				xml = xml.substring(0, xml.length() - 2);
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
			ctx.setAttribute("error", e.getCause(), REQUEST);
			ctx.setMessage(e.getMessage());
			return;
		}

		// Add connection fields from the request
		ConnectionEntry ce = flight.getConnection();
		ce.setPilotID(ctx.getUser().getID());
		ce.setRemoteHost(ctx.getRequest().getRemoteHost());
		ce.setRemoteAddr(ctx.getRequest().getRemoteAddr());
		
		// Check that the build isn't deprecated
		int minBuild = Integer.MAX_VALUE; int build = ce.getClientBuild();
		ACARSClientInfo cInfo = (ACARSClientInfo) SharedData.get(SharedData.ACARS_CLIENT_BUILDS);
		if (build < 80)
			minBuild = cInfo.getMinimumBuild("v1.4");
		else if (build < 100)
			minBuild = cInfo.getMinimumBuild("v2.2");
		else
			minBuild = cInfo.getMinimumBuild("v3.0");
		
		if (build < minBuild) {
			String msg = "ACARS Build " + build + " not supported";
			if (minBuild > 0)
				msg += " Minimum build is Build " + minBuild;	
			ctx.setMessage(msg);
			return;
		}
		
		// Check for BETA
		if (ce.getBeta() > 0) {
			int minBeta = cInfo.getMinimumBetaBuild(ce.getClientBuild());
			if ((ce.getBeta() < minBeta) || (minBeta == 0)) {
				String msg = "ACARS Build " + build + " Beta " + ce.getBeta() + " deprecated";
				ctx.setMessage(msg);
				return;	
			}
		}
		
		// Convert the PIREP date into the user's local time zone
		FlightInfo inf = flight.getInfo();
		DateTime dt = new DateTime(inf.getEndTime());
		dt.convertTo(ctx.getUser().getTZ());
		
		// If the date/time is too far in the future, reject
		Calendar cld = CalendarUtils.getInstance(null, false, 1);
		if (cld.getTime().before(dt.getUTC()) && !noValidate) {
			String msg = "PIREP too far in future - " + StringUtils.format(dt.getUTC(), "MM/dd/yyyy");
			ctx.setAttribute("error", msg, REQUEST);
			ctx.setMessage(msg);
			return;
		}

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
			
			// Create comments field
			Collection<String> comments = new ArrayList<String>();
			
			// Check for Draft PIREPs by this Pilot
			GetFlightReports prdao = new GetFlightReports(con);
			List<FlightReport> dFlights = prdao.getDraftReports(p.getID(), afr.getAirportD(), afr.getAirportA(), SystemData.get("airline.db"));
			if (!dFlights.isEmpty()) {
				FlightReport fr = dFlights.get(0);
				afr.setID(fr.getID());
				afr.setDatabaseID(FlightReport.DBID_ASSIGN, fr.getDatabaseID(FlightReport.DBID_ASSIGN));
				afr.setDatabaseID(FlightReport.DBID_EVENT, fr.getDatabaseID(FlightReport.DBID_EVENT));
				afr.setAttribute(FlightReport.ATTR_CHARTER, fr.hasAttribute(FlightReport.ATTR_CHARTER));
				if (!StringUtils.isEmpty(fr.getComments()))
					comments.add(fr.getComments());
			}
			
			// Check if this Flight Report counts for promotion
			GetEquipmentType eqdao = new GetEquipmentType(con);
			Collection<String> promoEQ = eqdao.getPrimaryTypes(SystemData.get("airline.db"), afr.getEquipmentType());

			// Loop through the eq types, not all may have the same minimum promotion stage length!!
			if (promoEQ.contains(p.getEquipmentType())) {
				FlightPromotionHelper helper = new FlightPromotionHelper(afr);
				for (Iterator<String> i = promoEQ.iterator(); i.hasNext(); ) {
					String pType = i.next();
					EquipmentType pEQ = eqdao.get(pType, SystemData.get("airline.db"));
					boolean isOK = helper.canPromote(pEQ);
					if (!isOK) {
						i.remove();
						if (!StringUtils.isEmpty(helper.getLastComment()))
							comments.add(helper.getLastComment());
					}
				}
				
				afr.setCaptEQType(promoEQ);
			}
			
			// Check if it's an Online Event flight
			OnlineNetwork network = afr.getNetwork();
			if ((afr.getDatabaseID(FlightReport.DBID_EVENT) == 0) && (afr.hasAttribute(FlightReport.ATTR_ONLINE_MASK))) {
				GetEvent evdao = new GetEvent(con);
				afr.setDatabaseID(FlightReport.DBID_EVENT, evdao.getEvent(afr.getAirportD(), afr.getAirportA(), network));
			}
			
			// Check that the user has an online network ID
			if ((network != null) && (!p.hasNetworkID(network))) {
				log.warn(p.getName() + " does not have a " + network.toString() + " ID");
				comments.add("No " + network.toString() + " ID, resetting Online Network flag");
				afr.setNetwork(null);
			}
			
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
			
			// Save comments
			if (!comments.isEmpty())
				afr.setComments(StringUtils.listConcat(comments, "\r\n"));
			
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