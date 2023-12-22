// Copyright 2020, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.econ;

import static java.util.concurrent.TimeUnit.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.sql.Connection;

import org.apache.logging.log4j.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.econ.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.file.GetSerializedPosition;

import org.deltava.security.command.EliteAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to recalculate a Pilot's Elite status.
 * @author Luke
 * @version 11.1
 * @since 9.2
 */

public class EliteStatusCalculateCommand extends AbstractCommand {
	
	private static final Logger log = LogManager.getLogger(EliteStatusCalculateCommand.class);
	
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

		EliteScorer es = EliteScorer.getInstance();
		final int year = EliteScorer.getStatusYear(Instant.now());
		boolean saveChanges = Boolean.parseBoolean(ctx.getParameter("saveChanges"));
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
			List<EliteStatus> oldStatus = edao.getAllStatus(p.getID(), year);
			EliteStatus st = oldStatus.stream().filter(est -> est.getUpgradeReason().isRollover()).findAny().orElse(new EliteStatus(p.getID(), lvls.first()));
			if (st.getEffectiveOn() == null)
				st.setEffectiveOn(LocalDate.of(year, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant());
			
			// Track status throughout the year
			SortedMap<Instant, EliteStatus> myStatus = new TreeMap<Instant, EliteStatus>();
			myStatus.put(st.getEffectiveOn(), st);
			
			// We also want to load previous year's status for January flights
			List<EliteStatus> pyStatus = edao.getAllStatus(p.getID(), year - 1);
			if (!pyStatus.isEmpty()) {
				EliteStatus lyStatus = pyStatus.getLast();
				myStatus.put(lyStatus.getEffectiveOn(), lyStatus);
			}
			
			// Get current totals
			GetEliteStatistics esdao = new GetEliteStatistics(con);
			YearlyTotal oldTotal = esdao.getEliteTotals(p.getID()).stream().filter(yt -> yt.getYear() == year).findAny().orElse(new YearlyTotal(year, p.getID()));
			
			// Get the Flight Reports
			GetFlightReports frdao = new GetFlightReports(con);
			List<FlightReport> pireps = frdao.getEliteFlights(p.getID(), year);
			
			// Load the ACARS data
			IntervalTaskTimer tt = new IntervalTaskTimer();
			Collection<Integer> IDs = pireps.stream().filter(FDRFlightReport.class::isInstance).map(fr -> Integer.valueOf(fr.getDatabaseID(DatabaseID.ACARS))).collect(Collectors.toSet());
			Map<Integer, SequencedCollection<? extends RouteEntry>> routeData = new HashMap<Integer, SequencedCollection<? extends RouteEntry>>();
			IDs.parallelStream().map(id -> Map.entry(id, loadACARS(id))).forEach(me -> routeData.put(me.getKey(), me.getValue()));
			long ms = MILLISECONDS.convert(tt.mark("flights"), NANOSECONDS);
			log.info("ACARS data for {} flights loaded in {}ms", Integer.valueOf(IDs.size()), Long.valueOf(ms));
			
			// Load elite scoring data
			tt.start(); Map<Integer, FlightEliteScore> scores = new HashMap<Integer, FlightEliteScore>();
			for (FlightReport fr : pireps) {
				FlightEliteScore sc = frdao.getElite(fr.getID());
				if (sc != null)
					scores.put(Integer.valueOf(fr.getID()), sc);
			}
			
			ms = MILLISECONDS.convert(tt.mark("totals"), NANOSECONDS);
			log.info("{} data for {} flights loaded in {}ms", SystemData.get("econ.elite.name"), Integer.valueOf(pireps.size()), Long.valueOf(ms));
			
			// Open transaction boundary
			ctx.startTX();
			
			// Get the DAOs
			GetAircraft acdao = new GetAircraft(con);
			GetACARSData fidao = new GetACARSData(con);
			SetFlightReport frwdao = new SetFlightReport(con);
			
			// Purge status
			SetElite elwdao = new SetElite(con);
			elwdao.clear(p.getID(), year, false);
			
			// Score the Flight Reports
			FlightEliteScore sc = null; 
			YearlyTotal total = new YearlyTotal(year, p.getID());
			Collection<String> msgs = new ArrayList<String>(); Map<Integer, String> updatedScores = new LinkedHashMap<Integer, String>();
			AirlineInformation ai = SystemData.getApp(null);
			for (FlightReport fr : pireps) {
				tt.mark("scoreStart");
				st = myStatus.getOrDefault(myStatus.headMap(fr.getSubmittedOn()).lastKey(), myStatus.get(myStatus.firstKey()));
				
				if (fr instanceof FDRFlightReport ffr) {
					Aircraft a = acdao.get(fr.getEquipmentType());
					AircraftPolicyOptions opts = a.getOptions(ai.getCode());
					FlightInfo fi = fidao.getInfo(fr.getDatabaseID(DatabaseID.ACARS));
					
					// Load the positions
					if ((fi != null) && fi.getArchived()) {
						ScorePackage pkg = new ScorePackage(a, ffr, fi.getRunwayD(), fi.getRunwayA(), opts);
						SequencedCollection<? extends RouteEntry> entries = routeData.getOrDefault(Integer.valueOf(fi.getID()), Collections.emptyList());
						if (entries.isEmpty())
							msgs.add(String.format("No flight data found for Flight %d", Integer.valueOf(fr.getID())));

						entries.forEach(pkg::add);
						sc = es.score(pkg, st.getLevel());
					} else if (fi != null)
						sc = es.score(new ScorePackage(a, ffr, fi.getRunwayD(), fi.getRunwayA(), opts), st.getLevel());
					else
						sc  = es.score(new ScorePackage(a, ffr, null, null, opts), st.getLevel());
				} else
					sc  = es.score(fr, st.getLevel());
				
				// Check if the score has changed
				FlightEliteScore oldScore = scores.getOrDefault(Integer.valueOf(fr.getID()), new FlightEliteScore(fr.getID()));
				if (!sc.equals(oldScore) && (oldScore.getPoints() > 0))
					updatedScores.put(Integer.valueOf(fr.getID()), String.format("Was %d / %d, now %d / %d", Integer.valueOf(oldScore.getDistance()), Integer.valueOf(oldScore.getPoints()), Integer.valueOf(sc.getDistance()), Integer.valueOf(sc.getPoints())));
				
				// Write the score
				tt.mark("acarsData");
				frwdao.writeElite(sc, ai.getDB());
				
				// Determine the next level
				EliteLevel nextLevel = lvls.higher(st.getLevel());
				UpgradeReason updR = total.wouldMatch(nextLevel, sc);
				if ((nextLevel != null) && (updR != UpgradeReason.NONE)) {
					String msg = String.format("%s reaches %s for %d on %s / %s", p.getName(), nextLevel.getName(), Integer.valueOf(year), StringUtils.format(fr.getDate(), ctx.getUser().getDateFormat()), updR.getDescription());
					log.info(msg);
					msgs.add(msg);
					EliteStatus newSt = new EliteStatus(p.getID(), nextLevel);
					newSt.setEffectiveOn(ZonedDateTime.ofInstant(fr.getDate(), ZoneOffset.UTC).plusDays(1).truncatedTo(ChronoUnit.DAYS).toInstant());
					newSt.setUpgradeReason(updR);
					myStatus.put(newSt.getEffectiveOn(), newSt);
					elwdao.write(st);
					tt.mark("upgrade");
				}
				
				// Update the totals
				es.add(fr);
				total.add(sc);
				
				// Log warning
				ms = MILLISECONDS.convert(tt.mark("scoreEnd") - tt.getInterval("scoreStsart"), NANOSECONDS);
				if (ms > 1250) {
					log.warn("Scored Flight Report #{} - {} pts ({} ms)", Integer.valueOf(fr.getID()), Integer.valueOf(sc.getPoints()), Long.valueOf(ms));
					tt.getMarkerNames().forEach(mrk -> log.warn("{} - {}ms", mrk, Long.valueOf(tt.getInterval(mrk))));
				}
			}
			
			// Commit
			boolean isChanged = (EliteTotals.compare(total, oldTotal) != 0);
			if (saveChanges && isChanged)
				ctx.commitTX();
			else
				ctx.rollbackTX();
			
			// Set status attributes
			ctx.setAttribute("isRecalc", Boolean.TRUE, REQUEST);
			ctx.setAttribute("msgs", msgs, REQUEST);
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("total", total, REQUEST);
			ctx.setAttribute("oldTotal", oldTotal, REQUEST);
			ctx.setAttribute("updatedScores", updatedScores, REQUEST);
			ctx.setAttribute("isPersisted", Boolean.valueOf(saveChanges), REQUEST);
			ctx.setAttribute("isDifferent", Boolean.valueOf(isChanged), REQUEST);
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
	
	private static SequencedCollection<? extends RouteEntry> loadACARS(Integer id) {
		try {
			File f = ArchiveHelper.getPositions(id.intValue());
			Compression c = Compression.detect(f);
			try (InputStream is = c.getStream(new BufferedInputStream(new FileInputStream(f), 32768))) {
				GetSerializedPosition psdao = new GetSerializedPosition(is);
				return psdao.read();
			}
		} catch (DAOException | IOException ie) {
			log.atError().withThrowable(ie).log("Error reading positions for Flight {} - {}", id, ie.getMessage());
			return Collections.emptyList();
		}		
	}
}