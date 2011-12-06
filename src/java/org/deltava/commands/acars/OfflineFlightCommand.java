// Copyright 2009, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.econ.*;
import org.deltava.beans.event.Event;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.crypt.MessageDigester;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import org.gvagroup.acars.*;

/**
 * A Web Site Command to allow users to submit Offline Flight Reports.
 * @author Luke
 * @version 4.1
 * @since 2.4
 */

public class OfflineFlightCommand extends AbstractCommand {
	
	private static final Logger log = Logger.getLogger(OfflineFlightCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
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
		OfflineFlight<ACARSFlightReport, ACARSRouteEntry> flight = null;
		boolean noValidate = (ctx.isUserInRole("HR") || ctx.isSuperUser()) && Boolean.valueOf(ctx.getParameter("noValidate")).booleanValue();
		try {
			if (sha == null)
				sha = new String(shaF.getBuffer(), "UTF-8").trim();
			if (xml == null)
				xml = new String(xmlF.getBuffer(), "UTF-8");
			
			// Sanity check the length
			if (xml.length() > SystemData.getInt("acars.max_offline_size", 4096000))
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
			log.error("Error parsing XML - " + e.getMessage(), (e instanceof IllegalArgumentException) ? null : e);
			ctx.setAttribute("error", e.getCause(), REQUEST);
			ctx.setMessage(e.getMessage());
			return;
		}

		// Add connection fields from the request
		ConnectionEntry ce = flight.getConnection();
		ce.setAuthorID(ctx.getUser().getID());
		ce.setRemoteHost(ctx.getRequest().getRemoteHost());
		ce.setRemoteAddr(ctx.getRequest().getRemoteAddr());
		
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
		afr.setDatabaseID(DatabaseID.PILOT, ctx.getUser().getID());
		afr.setRank(ctx.getUser().getRank());
		afr.setDate(dt.getDate());
		
		// Get the client version
		ClientInfo cInfo = new ClientInfo(ce.getVersion(), ce.getClientBuild(), ce.getBeta());
		try {
			Connection con = ctx.getConnection();
			
			// Check that the build isn't deprecated
			GetACARSBuilds abdao = new GetACARSBuilds(con);
			boolean isBuildOK = abdao.isValid(cInfo, GetACARSBuilds.AccessRole.UPLOAD);
			if (!isBuildOK) {
				ClientInfo latestBuild = null;
				if (cInfo.isBeta())
					latestBuild = abdao.getLatestBeta(cInfo.getVersion(), cInfo.getClientBuild());
				if (latestBuild == null)
					latestBuild = abdao.getLatestBuild(cInfo.getVersion());
				ctx.release();
				
				// Set message
				String msg = "ACARS Build " + cInfo.toString() + " not supported.";
				if (latestBuild != null)
					msg += " Minimum " + cInfo.getVersion() + " build is Build " + latestBuild.getClientBuild();
				
				ctx.setMessage(msg);
				return;
			}
			
			// Get the user information
			GetPilot pdao = new GetPilot(con);
			GetPilot.invalidateID(ctx.getUser().getID());
			Pilot p = pdao.get(ctx.getUser().getID());
			
			// Get the SID/STAR data
			GetNavRoute nvdao = new GetNavRoute(con);
			inf.setSID(nvdao.getRoute(afr.getAirportD(), TerminalRoute.SID, flight.getSID(), true));
			inf.setSTAR(nvdao.getRoute(afr.getAirportA(), TerminalRoute.STAR, flight.getSTAR(), true));
			
			// Create comments field
			Collection<String> comments = new LinkedHashSet<String>();
			
			// Check for Draft PIREPs by this Pilot
			GetFlightReports prdao = new GetFlightReports(con);
			List<FlightReport> dFlights = prdao.getDraftReports(p.getID(), afr.getAirportD(), afr.getAirportA(), SystemData.get("airline.db"));
			if (!dFlights.isEmpty()) {
				FlightReport fr = dFlights.get(0);
				afr.setID(fr.getID());
				afr.setDatabaseID(DatabaseID.ASSIGN, fr.getDatabaseID(DatabaseID.ASSIGN));
				afr.setDatabaseID(DatabaseID.EVENT, fr.getDatabaseID(DatabaseID.EVENT));
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
							comments.add("Not eligible for promotion: " + helper.getLastComment());
					}
				}
				
				afr.setCaptEQType(promoEQ);
			}
			
			// Check that the user has an online network ID
			OnlineNetwork network = afr.getNetwork();
			if ((network != null) && (!p.hasNetworkID(network))) {
				log.warn(p.getName() + " does not have a " + network.toString() + " ID");
				comments.add("No " + network.toString() + " ID, resetting Online Network flag");
				afr.setNetwork(null);
			} else if ((network == null) && (afr.getDatabaseID(DatabaseID.EVENT) != 0))
				afr.setDatabaseID(DatabaseID.EVENT, 0);
			
			// Check if it's an Online Event flight
			GetEvent evdao = new GetEvent(con);
			if ((afr.getDatabaseID(DatabaseID.EVENT) == 0) && (afr.hasAttribute(FlightReport.ATTR_ONLINE_MASK)))
				afr.setDatabaseID(DatabaseID.EVENT, evdao.getEvent(afr.getAirportD(), afr.getAirportA(), network));
			
			// Check that the event hasn't expired
			if (afr.getDatabaseID(DatabaseID.EVENT) != 0) {
				Event e = evdao.get(afr.getDatabaseID(DatabaseID.EVENT));
				if (e != null) {
					long timeSinceEnd = (System.currentTimeMillis() - e.getEndTime().getTime()) / 1000;
					if (timeSinceEnd > 86400) {
						log.warn("Flight logged over 24 hours after Event completion");
						afr.setDatabaseID(DatabaseID.EVENT, 0);
					}
				} else
					afr.setDatabaseID(DatabaseID.EVENT, 0);
			}
			
			// Check for historic aircraft
			GetAircraft acdao = new GetAircraft(con);
			Aircraft a = acdao.get(afr.getEquipmentType());
			if (a == null)
				throw notFoundException("Invalid equipment type - " + afr.getEquipmentType());
			
			// Check if the user is rated to fly the aircraft
			afr.setAttribute(FlightReport.ATTR_HISTORIC, a.getHistoric());
			EquipmentType eq = eqdao.get(p.getEquipmentType());
			if (!p.getRatings().contains(afr.getEquipmentType()) && !eq.getRatings().contains(afr.getEquipmentType()))
				afr.setAttribute(FlightReport.ATTR_NOTRATED, !afr.hasAttribute(FlightReport.ATTR_CHECKRIDE));

			// Check for excessive distance
			if (afr.getDistance() > a.getRange())
				afr.setAttribute(FlightReport.ATTR_RANGEWARN, true);

			// Check for excessive weight
			if ((a.getMaxTakeoffWeight() != 0) && (afr.getTakeoffWeight() > a.getMaxTakeoffWeight()))
				afr.setAttribute(FlightReport.ATTR_WEIGHTWARN, true);
			else if ((a.getMaxLandingWeight() != 0) && (afr.getLandingWeight() > a.getMaxLandingWeight()))
				afr.setAttribute(FlightReport.ATTR_WEIGHTWARN, true);
			
			// Check ETOPS
			afr.setAttribute(FlightReport.ATTR_ETOPSWARN, ETOPSHelper.validate(a, afr));
			if (afr.hasAttribute(FlightReport.ATTR_ETOPSWARN)) {
				ETOPS etopsClass = ETOPSHelper.classify(flight.getPositions());
				comments.add("ETOPS classificataion: " + etopsClass.toString());
			}
			
			// Calculate the load factor
			EconomyInfo eInfo = (EconomyInfo) SystemData.getObject(SystemData.ECON_DATA);
			if (eInfo != null) {
				LoadFactor lf = new LoadFactor(eInfo);
				double loadFactor = lf.generate(afr.getSubmittedOn());
				afr.setPassengers((int) Math.round(a.getSeats() * loadFactor));
				afr.setLoadFactor(loadFactor);
			}
			
			// Check if it's a Flight Academy flight
			GetSchedule sdao = new GetSchedule(con);
			ScheduleEntry sEntry = sdao.get(afr);
			afr.setAttribute(FlightReport.ATTR_ACADEMY, ((sEntry != null) && sEntry.getAcademy()));
			
			// Check for inflight refueling
			FuelUse fuelUse = FuelUse.validate(flight.getPositions());
			afr.setAttribute(FlightReport.ATTR_REFUELWARN, fuelUse.getRefuel());
			afr.setTotalFuel(fuelUse.getTotalFuel());

			// Check the schedule database and check the route pair
			int avgHours = sdao.getFlightTime(afr);
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
			
			// If we don't have takeoff/touchdown points from Build 97+, derive them
			if (afr.getTakeoffHeading() == -1) {
				List<ACARSRouteEntry> tdEntries = new ArrayList<ACARSRouteEntry>();
				for (Iterator<ACARSRouteEntry> i = flight.getPositions().iterator(); i.hasNext(); ) {
					ACARSRouteEntry re = i.next();
					if (re.isFlagSet(ACARSFlags.FLAG_TOUCHDOWN))
						tdEntries.add(re);
				}
					
				if (tdEntries.size() > 2) {
					int ofs = 0;
					ACARSRouteEntry entry = tdEntries.get(0);
					GeoPosition adPos = new GeoPosition(inf.getAirportD());
					while ((ofs < (tdEntries.size() - 1)) && (adPos.distanceTo(entry) < 15) && (entry.getVerticalSpeed() > 0)) {
						ofs++;
						entry = tdEntries.get(ofs);
					}

					// Trim out spurious takeoff entries
					if (ofs > 0)
						tdEntries.subList(0, ofs - 1).clear();
					if (tdEntries.size() > 2)
						tdEntries.subList(1, tdEntries.size() - 1).clear();
				}
				
				// Save the entry points
				if (tdEntries.size() > 0) {
					afr.setTakeoffLocation(tdEntries.get(0));
					afr.setTakeoffHeading(tdEntries.get(0).getHeading());
					if (tdEntries.size() > 1) {
						afr.setLandingLocation(tdEntries.get(1));
						afr.setLandingHeading(tdEntries.get(1).getHeading());
					}
				}
			}
			
			// Load the departure runway
			GetNavAirway navdao = new GetNavAirway(con);
			Runway rD = null;
			if (afr.getTakeoffHeading() > -1) {
				Runway r = navdao.getBestRunway(inf.getAirportD(), afr.getFSVersion(), afr.getTakeoffLocation(), afr.getTakeoffHeading());
				if (r != null) {
					int dist = GeoUtils.distanceFeet(r, afr.getTakeoffLocation());
					rD = new RunwayDistance(r, dist);
					if (r.getLength() < a.getTakeoffRunwayLength())
						afr.setAttribute(FlightReport.ATTR_RWYWARN, true);
				}
			}

			// Load the arrival runway
			Runway rA = null;
			if (afr.getLandingHeading() > -1) {
				Runway r = navdao.getBestRunway(afr.getAirportA(), afr.getFSVersion(), afr.getLandingLocation(), afr.getLandingHeading());
				if (r != null) {
					int dist = GeoUtils.distanceFeet(r, afr.getLandingLocation());
					rA = new RunwayDistance(r, dist);
					if (r.getLength() < a.getLandingRunwayLength())
						afr.setAttribute(FlightReport.ATTR_RWYWARN, true);
				}
			}
			
			// Validate the flight ID, otherwise set it to zero
			if (inf.getID() != 0) {
				GetACARSData addao = new GetACARSData(con);
				FlightInfo savedInf = addao.getInfo(inf.getID());
				if (savedInf == null) {
					log.warn("Invalid Flight ID - " + inf.getID());
					inf.setID(0);
				}
			}
			
			// Validate the dispatch route ID
			if (inf.getRouteID() != 0) {
				GetACARSRoute ardao = new GetACARSRoute(con);
				DispatchRoute dr = ardao.getRoute(inf.getRouteID());
				if (dr == null) {
					log.warn("Invalid Dispatch Route - " + inf.getRouteID());
					inf.setRouteID(0);
				}
			}
			
			// Validate the dispatcher
			if (inf.getDispatcherID() != 0) {
				GetUserData uddao = new GetUserData(con);
				UserData ud = uddao.get(inf.getDispatcherID());
				if (ud == null) {
					log.warn("Invalid Disaptcher - " + inf.getDispatcherID());
					inf.setDispatcherID(0);
				}
			}
			
			// Turn off auto-commit
			ctx.startTX();

			// Write the connection/info records
			SetACARSData awdao = new SetACARSData(con);
			awdao.createConnection(ce);
			awdao.createFlight(inf);
			afr.setDatabaseID(DatabaseID.ACARS, inf.getID());
			awdao.writeSIDSTAR(inf.getID(), inf.getSID());
			awdao.writeSIDSTAR(inf.getID(), inf.getSTAR());
			awdao.writeRunways(inf.getID(), rD, rA);
			if (inf.isDispatchPlan())
				awdao.writeDispatch(inf.getID(), inf.getDispatcherID(), inf.getRouteID());
			
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