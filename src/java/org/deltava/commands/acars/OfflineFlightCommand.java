// Copyright 2009, 2010, 2011, 2012, 2014, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.sql.Connection;
import java.time.Instant;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.academy.*;
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
import org.deltava.comparators.GeoComparator;

import org.deltava.util.*;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to allow users to submit Offline Flight Reports.
 * @author Luke
 * @version 7.3
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
	@SuppressWarnings("null")
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get command result and check for post
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/acars/offlineSubmit.jsp");
		
		// Check for a ZIP archive
		String sha = null; String xml = null;
		FileUpload zipF = ctx.getFile("zip");
		if (zipF != null) {
			try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipF.getBuffer()))) {
				byte[] buffer = new byte[32768];
				ZipEntry ze = zis.getNextEntry();
				while ((ze != null) && ((sha == null) || (xml == null))) {
					String name = ze.getName().toLowerCase();
					try (ByteArrayOutputStream out = new ByteArrayOutputStream(102400)) {
						int bytesRead = zis.read(buffer);
						while (bytesRead > -1) {
							out.write(buffer, 0, bytesRead);
							bytesRead = zis.read(buffer);
						}
						
						if (name.endsWith(".xml"))
							xml = new String(out.toByteArray(), "UTF-8");
						else if (name.endsWith(".sha"))
							sha = new String(out.toByteArray(), "UTF-8").trim();
					}
					
					ze = zis.getNextEntry();
				}
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
			ctx.setMessage("No Cryptographic signature");
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
				String algo = SystemData.get("security.hash.acars.algorithm"); int pos = sha.indexOf(':');
				if (pos != -1) {
					algo = sha.substring(0, pos);
					sha = sha.substring(pos + 1);
				}
				
				MessageDigester md = new MessageDigester(algo);
				md.salt(SystemData.get("security.hash.acars.salt"));
				String calcHash = MessageDigester.convert(md.digest(xml.getBytes()));
				if (!calcHash.equals(sha)) {
					log.warn("ACARS Signature mismatch - expected " + sha + ", calculated " + calcHash);
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

		// Convert the PIREP date into the user's local time zone
		FlightInfo inf = flight.getInfo();
		inf.setAuthorID(ctx.getUser().getID());
		inf.setRemoteHost(ctx.getRequest().getRemoteHost());
		inf.setRemoteAddr(ctx.getRequest().getRemoteAddr());
		
		// If the date/time is too far in the future, reject
		Instant maxDate = Instant.now().plusSeconds(86400);
		if (maxDate.isBefore(inf.getEndTime()) && !noValidate) {
			String msg = "PIREP too far in future - " + StringUtils.format(inf.getEndTime(), "MM/dd/yyyy");
			ctx.setAttribute("error", msg, REQUEST);
			ctx.setMessage(msg);
			return;
		}

		// Add PIREP fields from the request
		ACARSFlightReport afr = flight.getFlightReport();
		afr.setDatabaseID(DatabaseID.PILOT, inf.getAuthorID());
		afr.setRank(ctx.getUser().getRank());
		afr.setDate(inf.getEndTime());
		
		// Get the client version
		ClientInfo cInfo = new ClientInfo(inf.getVersion(), inf.getClientBuild(), inf.getBeta());
		try {
			Connection con = ctx.getConnection();
			
			// Check that the build isn't deprecated
			GetACARSBuilds abdao = new GetACARSBuilds(con);
			boolean isBuildOK = abdao.isValid(cInfo, AccessRole.UPLOAD);
			ClientInfo latestBuild = null;
			if (cInfo.isBeta())
				latestBuild = abdao.getLatestBeta(cInfo);
			if (latestBuild == null)
				latestBuild = abdao.getLatestBuild(cInfo);
			
			if (!isBuildOK) {
				String msg = "ACARS Build " + cInfo.toString() + " not supported.";
				if (latestBuild != null) {
					msg += " Minimum ACARS " + cInfo.getVersion() + ".x build is Build " + latestBuild.getClientBuild() + "-";
					msg += (cInfo.getBeta() == 0) ? "-release" : ("b" + cInfo.getBeta());
				}
				
				ctx.setMessage(msg);
				return;
			}
			
			// If we're not the latest, warn
			if (cInfo.compareTo(latestBuild) < 0)
				ctx.setAttribute("newerBuild", latestBuild, REQUEST);
			
			// Validate the Flight ID
			if (inf.getID() != 0) {
				GetACARSData addao = new GetACARSData(con);
				FlightInfo savedInf = addao.getInfo(inf.getID());
				if (savedInf == null) {
					ctx.setMessage("Invalid  ACARS Flight ID - " + inf.getID() + ", cannot submit purged/invalid Flight");
					return;
				}
			}
			
			// Get the user information
			GetPilot pdao = new GetPilot(con);
			CacheManager.invalidate("Pilots", ctx.getUser().cacheKey());
			Pilot p = pdao.get(ctx.getUser().getID());
			
			// Get the SID/STAR data
			GetNavRoute nvdao = new GetNavRoute(con);
			inf.setSID(nvdao.getRoute(afr.getAirportD(), TerminalRoute.Type.SID, flight.getSID(), true));
			inf.setSTAR(nvdao.getRoute(afr.getAirportA(), TerminalRoute.Type.STAR, flight.getSTAR(), true));
			
			// Create comments field
			Collection<String> comments = new LinkedHashSet<String>();
			
			// Check for Draft PIREPs by this Pilot
			GetFlightReports prdao = new GetFlightReports(con);
			List<FlightReport> dFlights = prdao.getDraftReports(p.getID(), afr, SystemData.get("airline.db"));
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
						comments.add("Not eligible for promotion: " + helper.getLastComment());
					}
				}
				
				afr.setCaptEQType(promoEQ);
			}
			
			// Check that the user has an online network ID
			OnlineNetwork network = afr.getNetwork();
			if ((network != null) && (!p.hasNetworkID(network))) {
				comments.add("SYSTEM: No " + network.toString() + " ID, resetting Online Network flag");
				afr.setNetwork(null);
			} else if ((network == null) && (afr.getDatabaseID(DatabaseID.EVENT) != 0)) {
				comments.add("SYSTEM: Filed offline, resetting Online Event flag");
				afr.setDatabaseID(DatabaseID.EVENT, 0);
			}
			
			// Check if it's an Online Event flight
			GetEvent evdao = new GetEvent(con);
			if ((afr.getDatabaseID(DatabaseID.EVENT) == 0) && (afr.hasAttribute(FlightReport.ATTR_ONLINE_MASK))) {
				int eventID = evdao.getPossibleEvent(afr);
				if (eventID != 0) {
					Event e = evdao.get(eventID);
					comments.add("SYSTEM: Detected participation in " + e.getName() + " Online Event");
					afr.setDatabaseID(DatabaseID.EVENT, eventID);
				}
			}
			
			// Check that the event hasn't expired
			if (afr.getDatabaseID(DatabaseID.EVENT) != 0) {
				Event e = evdao.get(afr.getDatabaseID(DatabaseID.EVENT));
				if (e != null) {
					long timeSinceEnd = (System.currentTimeMillis() - e.getEndTime().toEpochMilli()) / 3600_000;
					if (timeSinceEnd > 6) {
						comments.add("SYSTEM: Flight logged " + timeSinceEnd + " hours after '" + e.getName() + "' completion");
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
			boolean isAcademy = ((sEntry != null) && sEntry.getAcademy());
			Course c = null;
			if (isAcademy) {
				GetAcademyCourses crsdao = new GetAcademyCourses(con);
				Collection<Course> courses = crsdao.getByPilot(ctx.getUser().getID());
				for (Iterator<Course> i = courses.iterator(); (c == null) && i.hasNext(); ) {
					Course crs = i.next();
					if (crs.getStatus() == Status.STARTED)
						c = crs;
				}
				
				afr.setAttribute(FlightReport.ATTR_ACADEMY, (c != null));
			}
			
			// Combine offline and online position reports
			Collection<ACARSRouteEntry> positions = new TreeSet<ACARSRouteEntry>(flight.getPositions().comparator());
			positions.addAll(flight.getPositions());
			if (inf.getID() > 0) {
				GetACARSPositions posdao = new GetACARSPositions(con);
				positions.addAll(posdao.getRouteEntries(inf.getID(), false));
			}
			
			// Check ETOPS
			ETOPSResult etopsClass = ETOPSHelper.classify(positions); 
			afr.setAttribute(FlightReport.ATTR_ETOPSWARN, ETOPSHelper.validate(a, etopsClass.getResult()));
			if (afr.hasAttribute(FlightReport.ATTR_ETOPSWARN))
				comments.add("ETOPS classificataion: " + String.valueOf(etopsClass));
			
			// Check prohibited airspace
			Collection<Airspace> rstAirspaces = positions.stream().map(pos -> Airspace.isRestricted(pos)).filter(Objects::nonNull).collect(Collectors.toSet());
			if (!rstAirspaces.isEmpty()) {
				afr.setAttribute(FlightReport.ATTR_AIRSPACEWARN, true);
				comments.add("SYSTEM: Entered restricted airspace " + StringUtils.listConcat(rstAirspaces, ", "));
			}
			
			// Check for inflight refueling and calculate fuel use
			FuelUse fuelUse = FuelUse.validate(positions);
			afr.setAttribute(FlightReport.ATTR_REFUELWARN, fuelUse.getRefuel());
			afr.setTotalFuel(fuelUse.getTotalFuel());

			// Check the schedule database and check the route pair
			FlightTime avgHours = sdao.getFlightTime(afr);
			boolean isAssignment = (afr.getDatabaseID(DatabaseID.ASSIGN) != 0);
			boolean isEvent = (afr.getDatabaseID(DatabaseID.EVENT) != 0);
			if (!avgHours.hasHistoric() && !avgHours.hasCurrent() && !inf.isScheduleValidated() && !isAssignment && !isEvent)
				afr.setAttribute(FlightReport.ATTR_ROUTEWARN, true);
			else if (avgHours.getFlightTime() > 0) {
				int minHours = (int) ((avgHours.getFlightTime() * 0.75) - (SystemData.getDouble("users.pirep.pad_hours", 0) * 10));
				int maxHours = (int) ((avgHours.getFlightTime() * 1.15) + (SystemData.getDouble("users.pirep.pad_hours", 0) * 10));
				if ((afr.getLength() < minHours) || (afr.getLength() > maxHours))
					afr.setAttribute(FlightReport.ATTR_TIMEWARN, true);
			}
			
			// Calculate average frame rate
			afr.setAverageFrameRate(positions.stream().mapToInt(ACARSRouteEntry::getFrameRate).average().getAsDouble());
				
			// Save comments
			if (!comments.isEmpty())
				afr.setComments(StringUtils.listConcat(comments, "\r\n"));
			
			// Load the departure runway
			GetNavAirway navdao = new GetNavAirway(con);
			Runway rD = null;
			if (afr.getTakeoffHeading() > -1) {
				LandingRunways lr = navdao.getBestRunway(inf.getAirportD(), afr.getSimulator(), afr.getTakeoffLocation(), afr.getTakeoffHeading());
				Runway r = lr.getBestRunway();
				if (r != null) {
					int dist = GeoUtils.distanceFeet(r, afr.getTakeoffLocation());
					rD = new RunwayDistance(r, dist);
					if (r.getLength() < a.getTakeoffRunwayLength())
						afr.setAttribute(FlightReport.ATTR_RWYWARN, true);
					if (!r.getSurface().isHard() && !a.getUseSoftRunways())
						afr.setAttribute(FlightReport.ATTR_RWYSFCWARN, true);
				}
			}

			// Load the arrival runway
			Runway rA = null;
			if (afr.getLandingHeading() > -1) {
				LandingRunways lr = navdao.getBestRunway(afr.getAirportA(), afr.getSimulator(), afr.getLandingLocation(), afr.getLandingHeading());
				Runway r = lr.getBestRunway();
				if (r != null) {
					int dist = GeoUtils.distanceFeet(r, afr.getLandingLocation());
					rA = new RunwayDistance(r, dist);
					if (r.getLength() < a.getLandingRunwayLength())
						afr.setAttribute(FlightReport.ATTR_RWYWARN, true);
					if (!r.getSurface().isHard() && !a.getUseSoftRunways())
						afr.setAttribute(FlightReport.ATTR_RWYSFCWARN, true);
				}
			}
			
			// Calculate gates
			Gate gD = null; Gate gA = null;
			if (flight.getPositions().size() > 1) {
				GeoComparator dgc = new GeoComparator(flight.getPositions().first(), true);
				GeoComparator agc = new GeoComparator(flight.getPositions().last(), true);
			
				// Get the closest departure gate
				GetGates gdao = new GetGates(con);
				SortedSet<Gate> dGates = new TreeSet<Gate>(dgc);
				dGates.addAll(gdao.getAllGates(afr.getAirportD(), inf.getSimulator()));
				gD = dGates.isEmpty() ? null : dGates.first();
				
				// Get the closest arrival gate
				SortedSet<Gate> aGates = new TreeSet<Gate>(agc);
				aGates.addAll(gdao.getAllGates(afr.getAirportA(), inf.getSimulator()));
				gA = aGates.isEmpty() ? null : aGates.first();
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
			SetACARSRunway awdao = new SetACARSRunway(con);
			awdao.createFlight(inf);
			afr.setDatabaseID(DatabaseID.ACARS, inf.getID());
			awdao.writeSIDSTAR(inf.getID(), inf.getSID());
			awdao.writeSIDSTAR(inf.getID(), inf.getSTAR());
			awdao.writeRunways(inf.getID(), rD, rA);
			awdao.writeGates(inf.getID(), gD, gA);
			if (inf.isDispatchPlan())
				awdao.writeDispatch(inf.getID(), inf.getDispatcherID(), inf.getRouteID());
			if (!flight.getPositions().isEmpty())
				awdao.writePositions(inf.getID(), flight.getPositions());
			
			// Update the checkride record (don't assume pilots check the box, because they don't)
			GetExam exdao = new GetExam(con);
			CheckRide cr = null;
			if (c != null) {
				List<CheckRide> cRides = exdao.getAcademyCheckRides(c.getID(), TestStatus.NEW);
				if (!cRides.isEmpty())
					cr = cRides.get(0);
			}
			
			if (cr == null)
				cr = exdao.getCheckRide(p.getID(), afr.getEquipmentType(), TestStatus.NEW);
			
			if (cr != null) {
				cr.setFlightID(inf.getID());
				cr.setSubmittedOn(Instant.now());
				cr.setStatus(TestStatus.SUBMITTED);

				// Update the checkride
				SetExam wdao = new SetExam(con);
				wdao.write(cr);
			} else
				afr.setAttribute(FlightReport.ATTR_CHECKRIDE, false);
			
			// Write the PIREP
			SetFlightReport fwdao = new SetFlightReport(con);
			fwdao.write(afr);
			fwdao.writeACARS(afr, SystemData.get("airline.db"));
			if (fwdao.updatePaxCount(afr.getID(), SystemData.get("airline.db")))
				log.warn("Update Passnger count for PIREP #" + afr.getID());
			
			// Commit
			ctx.commitTX();
			ctx.setAttribute("pirep", afr, REQUEST);
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