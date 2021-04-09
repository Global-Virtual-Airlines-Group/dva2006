// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.econ;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.acars.*;
import org.deltava.beans.econ.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.file.GetSerializedPosition;

import org.deltava.security.command.EliteAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to recalculate a Pilot's Elite status.
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public class EliteStatusCalculateCommand extends AbstractCommand {
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check our access
		EliteAccessControl ac = new EliteAccessControl(ctx);
		ac.validate();
		if (!ac.getCanRecalculate())
			throw securityException("Cannot recalculate Elite status");

		final int year = EliteLevel.getYear(Instant.now());
		try {
			Connection con = ctx.getConnection();
			
			// Get the pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(ctx.getID());
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());
			
			// Get the current levels
			GetElite edao = new GetElite(con);
			TreeSet<EliteLevel> lvls = edao.getLevels(year);
			List<EliteStatus> oldStatus = edao.getStatus(p.getID(), year);
			EliteStatus st = oldStatus.stream().filter(es -> es.getUpgradeReason().isRollover()).findAny().orElse(new EliteStatus(p.getID(), lvls.first()));
			if (st.getEffectiveOn() == null)
				st.setEffectiveOn(LocalDate.of(year, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant());
			
			// Track status throughout the year
			SortedMap<Instant, EliteStatus> myStatus = new TreeMap<Instant, EliteStatus>();
			myStatus.put(st.getEffectiveOn(), st);
			
			// Get the Flight Reports
			ScheduleSearchCriteria ssc = new ScheduleSearchCriteria("DATE, PR.SUBMITTED");
			GetFlightReports frdao = new GetFlightReports(con);
			List<FlightReport> pireps = frdao.getByPilot(p.getID(), ssc);
			pireps.removeIf(fr -> ((fr.getStatus() != FlightStatus.OK) || (EliteLevel.getYear(fr.getDate()) != year)));
			
			// Get the DAOs
			GetAircraft acdao = new GetAircraft(con);
			GetACARSData fidao = new GetACARSData(con);
			SetFlightReport frwdao = new SetFlightReport(con);
			
			ctx.startTX();
			
			// Purge status
			SetElite elwdao = new SetElite(con);
			elwdao.clear(p.getID(), year, false);
			
			// Score the Flight Reports
			PointScorer es = PointScorer.init(SystemData.get("econ.elite.scorer"));
			YearlyTotal total = new YearlyTotal(year, p.getID());
			Collection<String> msgs = new ArrayList<String>();
			AirlineInformation ai = SystemData.getApp(null);
			for (FlightReport fr : pireps) {
				FlightEliteScore sc = null;
				st = myStatus.getOrDefault(myStatus.headMap(fr.getSubmittedOn()).lastKey(), myStatus.get(myStatus.firstKey()));
				
				if (fr instanceof FDRFlightReport) {
					Aircraft a = acdao.get(fr.getEquipmentType());
					AircraftPolicyOptions opts = a.getOptions(ai.getCode());
					FlightInfo fi = fidao.getInfo(fr.getDatabaseID(DatabaseID.ACARS));
					
					// Load the positions
					Collection<RouteEntry> entries = new ArrayList<RouteEntry>();
					if ((fi != null) && fi.getArchived()) {
						File f = ArchiveHelper.getPositions(fi.getID());
						try (InputStream is = new BufferedInputStream(new FileInputStream(f), 32768)) {
							GetSerializedPosition psdao = new GetSerializedPosition(is);
							entries.addAll(psdao.read());
						} catch (IOException ie) {
							msgs.add("Error reading positions for Flight " + fr.getDatabaseID(DatabaseID.ACARS) + " - " + ie.getMessage());
						}
						
						// Create the package
						ScorePackage pkg = new ScorePackage(a, (FDRFlightReport) fr, fi.getRunwayD(), fi.getRunwayA(), opts);
						entries.forEach(pkg::add);
						sc = es.score(pkg, st.getLevel());
					} else if (fi != null) {
						ScorePackage pkg = new ScorePackage(a, (FDRFlightReport) fr, fi.getRunwayD(), fi.getRunwayA(), opts);
						sc = es.score(pkg, st.getLevel());
					} else
						sc  = es.score(fr, st.getLevel());
				} else
					sc  = es.score(fr, st.getLevel());
				
				// Determine the next level
				EliteLevel nextLevel = lvls.higher(st.getLevel());
				
				// Write the score
				frwdao.writeElite(sc, ai.getDB());
				UpgradeReason updR = total.wouldMatch(nextLevel, sc.getDistance(), sc.getPoints());
				if ((nextLevel != null) && (updR != UpgradeReason.NONE)) {
					msgs.add("Reaches " + nextLevel.getName() + " for " + year + " on " + StringUtils.format(fr.getDate(), ctx.getUser().getDateFormat()) + " / " + updR.getDescription());
					EliteStatus newSt = new EliteStatus(p.getID(), nextLevel);
					newSt.setEffectiveOn(ZonedDateTime.ofInstant(fr.getDate(), ZoneOffset.UTC).plusDays(1).truncatedTo(ChronoUnit.DAYS).toInstant());
					newSt.setUpgradeReason(updR);
					myStatus.put(newSt.getEffectiveOn(), newSt);
					elwdao.write(st);
				}
				
				// Update the totals
				es.add(fr);
				total.addLegs(1, sc.getDistance(), sc.getPoints());
			}
			
			// Set status attributes
			ctx.setAttribute("isRecalc", Boolean.TRUE, REQUEST);
			ctx.setAttribute("msgs", msgs, REQUEST);
			ctx.setAttribute("plilot", p, REQUEST);
			ctx.setAttribute("total", total, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/econ/eliteLevelUpdate.jsp");
		result.setSuccess(true);		
	}
}