// Copyright 2009, 2010, 2011, 2012, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import static java.nio.charset.StandardCharsets.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.sql.Connection;
import java.time.Instant;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.econ.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.crypt.MessageDigester;

import org.deltava.util.*;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to allow users to submit Offline Flight Reports.
 * @author Luke
 * @version 10.0
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
		String sha = null; byte[] xml = null;
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
							xml = out.toByteArray();
						else if (name.endsWith(".sha"))
							sha = new String(out.toByteArray(), US_ASCII);
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
			if (sha == null) sha = new String(shaF.getBuffer(), US_ASCII);
			if (xml == null) xml = xmlF.getBuffer();
			
			// Sanity check the length
			if (xml.length > SystemData.getInt("acars.max_offline_size", 4096000))
				throw new IllegalArgumentException("XML too large - " + StringUtils.format(xml.length, ctx.getUser().getNumberFormat()) + " bytes");
		
			// Validate the hash
			if (!noValidate) {
				Map<String, String> shaData = new HashMap<String, String>();
				try (BufferedReader br = new BufferedReader(new StringReader(sha))) {
					String data = br.readLine();
					while (data != null) {
						int pos = data.indexOf(':');
						if (pos > -1)
							shaData.put(data.substring(0, pos), data.substring(pos + 1));
						
						data = br.readLine();
					}
				}
				
				// Calculate length
				int xmlSize = StringUtils.parse(shaData.getOrDefault("Size", "0"), 0);
				if (xml.length != xmlSize) {
					log.warn("Expected XML size = " + xmlSize + ", actual size = " + xml.length);
					if (xml.length > xmlSize)
						xml = Arrays.copyOf(xml, xmlSize);
				}
				
				// Calculate hashes
				Map<String, String> hashData = new HashMap<String, String>(); boolean isHashOK = false;
				for (Map.Entry<String, String> me : shaData.entrySet()) {
					if (!me.getKey().startsWith("SHA")) continue;
					MessageDigester md = new MessageDigester(me.getKey());
					md.salt(SystemData.get("security.hash.acars.salt"));
					String calcHash = MessageDigester.convert(md.digest(xml));
					hashData.put(me.getKey(), calcHash);
					if (calcHash.equals(me.getValue())) {
						isHashOK = true;
						log.info("ACARS " + me.getKey() + " validated - " + calcHash);
					} else {
						log.warn("ACARS " + me.getKey() + " mismatch - expected " + me.getValue() + ", calculated " + calcHash);
					}
				}
				
				if (!isHashOK) {
					ctx.setAttribute("hashFailure", Boolean.TRUE, REQUEST);
					return;
				}
			}
		
			flight = OfflineFlightParser.create(new String(xml, UTF_8));
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
		afr.addStatusUpdate(afr.getAuthorID(), HistoryType.LIFECYCLE, "Submitted via web site");
		
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
			
			// Init the helper
			FlightSubmissionHelper fsh = new FlightSubmissionHelper(con, afr, p);
			fsh.setAirlineInfo(SystemData.get("airline.code"), ctx.getDB());
			fsh.setACARSInfo(inf);
			
			// Get the SID/STAR data
			GetNavRoute nvdao = new GetNavRoute(con);
			inf.setSID(nvdao.getRoute(afr.getAirportD(), TerminalRoute.Type.SID, flight.getSID(), true));
			inf.setSTAR(nvdao.getRoute(afr.getAirportA(), TerminalRoute.Type.STAR, flight.getSTAR(), true));
			
			// Check for Draft PIREPs by this Pilot
			fsh.checkFlightReports();

			// Check rating
			fsh.checkRatings();
			
			// Check Online Network / Event
			fsh.checkOnlineNetwork();
			fsh.checkOnlineEvent();
			
			// Check aircraft
			fsh.checkAircraft();
			
			// Check Tour
			fsh.checkTour();

			// Check if it's a Flight Academy flight / schedule
			fsh.checkSchedule();
			
			// Combine offline and online position reports
			Collection<ACARSRouteEntry> positions = new TreeSet<ACARSRouteEntry>(flight.getPositions().comparator());
			positions.addAll(flight.getPositions());
			if (inf.getID() > 0) {
				GetACARSPositions posdao = new GetACARSPositions(con);
				positions.addAll(posdao.getRouteEntries(inf.getID(), false));
			}
			
			fsh.addPositions(positions);
			
			// Check ETOPS / airspace
			fsh.checkAirspace();
			
			// Calculate runway / gates
			fsh.calculateGates();
			fsh.calculateRunways();
			
			// Calculate the load factor
			fsh.calculateLoadFactor((EconomyInfo) SystemData.getObject(SystemData.ECON_DATA), false);
			
			// Check for inflight refueling and calculate fuel use
			fsh.checkRefuel();

			// Calculate average frame rate
			afr.setAverageFrameRate(positions.stream().mapToInt(ACARSRouteEntry::getFrameRate).average().getAsDouble());
				
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
			awdao.writeRunways(inf.getID(), inf.getRunwayD(), inf.getRunwayA());
			awdao.writeGates(inf.getID(), inf.getGateD(), inf.getGateA());
			if (inf.isDispatchPlan())
				awdao.writeDispatch(inf.getID(), inf.getDispatcherID(), inf.getRouteID());
			if (!flight.getPositions().isEmpty())
				awdao.writePositions(inf.getID(), flight.getPositions());
			
			// Update the checkride record (don't assume pilots check the box, because they don't)
			GetExam exdao = new GetExam(con);
			CheckRide cr = null;
			if (fsh.getCourse() != null) {
				List<CheckRide> cRides = exdao.getAcademyCheckRides(fsh.getCourse().getID(), TestStatus.NEW);
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
			fwdao.writeACARS(afr, ctx.getDB());
			if (fwdao.updatePaxCount(afr.getID()))
				log.warn("Update Passnger count for PIREP #" + afr.getID());
			
			// Write ontime data if there is any
			if (afr.getOnTime() != OnTime.UNKNOWN) {
				SetACARSOnTime aowdao = new SetACARSOnTime(con);
				aowdao.write(ctx.getDB(), afr, fsh.getOnTimeEntry());
			}
			
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