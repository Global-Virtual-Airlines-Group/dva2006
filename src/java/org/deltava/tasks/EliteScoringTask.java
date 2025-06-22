// Copyright 2020, 2021, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import static java.util.concurrent.TimeUnit.*;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.temporal.ChronoField;
import java.sql.Connection;

import org.apache.logging.log4j.Level;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.econ.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.file.GetSerializedPosition;

import org.deltava.taskman.*;
import org.deltava.util.IntervalTaskTimer;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to calculate Elite scores for Flight Reports. 
 * @author Luke
 * @version 11.5
 * @since 9.2
 */

public class EliteScoringTask extends Task {
	
	/**
	 * Initializes the Task.
	 */
	public EliteScoringTask() {
		super("Elite Flight Scorer", EliteScoringTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {
		
		// Determine lookback interval
		int daysBack = Math.max(31, LocalDate.now().get(ChronoField.DAY_OF_YEAR));
		log.info("Scoring flights approved in the past {} days", Integer.valueOf(daysBack));
		
		Collection<Integer> pilotIDs = new HashSet<Integer>();
		try {
			Connection con = ctx.getConnection();
			ctx.startTX();
			
			// Get the DAOs
			GetPilot pdao = new GetPilot(con);
			GetElite eldao = new GetElite(con);
			GetGates gdao = new GetGates(con);
			GetAircraft acdao = new GetAircraft(con);
			GetFlightReports frdao = new GetFlightReports(con);
			GetACARSData fidao = new GetACARSData(con);
			
			SetElite elwdao = new SetElite(con);
			SetFlightReport frwdao = new SetFlightReport(con);
			SetStatusUpdate updwdao = new SetStatusUpdate(con);
			
			// Get the Flight Reports
			GetEliteStatistics esdao = new GetEliteStatistics(con);
			GetFlightReportStatistics frsdao = new GetFlightReportStatistics(con);
			frsdao.setDayFilter(daysBack);
			Collection<Integer> IDs = frsdao.getUnscoredFlights();
			log.warn("Scoring {} flights", Integer.valueOf(IDs.size()));
			
			int lastID = 0; final List<FlightReport> pireps = new ArrayList<FlightReport>();
			for (Iterator<Integer> i = IDs.iterator(); i.hasNext(); ) {
				Integer id = i.next();
				IntervalTaskTimer tt = new IntervalTaskTimer();
				EliteScorer es = EliteScorer.getInstance();
				FlightReport fr = frdao.get(id.intValue(), ctx.getDB());
				final int yr = EliteScorer.getStatusYear(fr.getDate());
				TreeSet<EliteLevel> lvls = eldao.getLevels(yr);
				TreeSet<EliteLifetime> ltLvls = eldao.getLifetimeLevels();
				
				// Get the pilot and Elite data - if we have no status create a dummy
				Pilot p = pdao.get(fr.getAuthorID());
				EliteStatus st = eldao.getStatus(p.getID(), yr);
				if (st == null) {
					st = new EliteStatus(p.getID(), lvls.first());
					st.setEffectiveOn(fr.getSubmittedOn());
					elwdao.write(st);
				}
				
				// Check our lifetime status
				EliteLifetimeStatus els = eldao.getLifetimeStatus(p.getID(), ctx.getDB());
				if (st.overridenBy(els)) {
					st = els.toStatus();
					log.info("Effective Status for {} is {} due to {}", p.getName(), st.getLevel(), els.getLifetimeStatus().getName());
				}
				
				// Load all previous Flight Reports for this Pilot
				tt.mark("init");
				if (p.getID() != lastID) {
					lastID = p.getID();
					pireps.clear();
					pireps.addAll(frdao.getEliteFlights(p.getID(), EliteScorer.getStatsYear(fr.getDate())));
					
					long ms = MILLISECONDS.convert(tt.mark("flights"), NANOSECONDS);
					log.info("Loaded {} flights for {} ({}) in {}ms", Integer.valueOf(pireps.size()), p.getName(), p.getPilotCode(), Long.valueOf(ms));
				}
				
				pireps.stream().filter(pirep -> !IDs.contains(Integer.valueOf(pirep.getID()))).forEach(es::add);
				
				// Get our total and next level
				YearlyTotal total = esdao.getEliteTotals(p.getID(), yr);
				YearlyTotal lifetime = esdao.getLifetimeTotals(p.getID());
				EliteLevel nextLevel = lvls.higher(st.getLevel());
				EliteLifetime lt = ltLvls.descendingSet().stream().filter(el -> lifetime.matches(el)).findFirst().orElse(null);
				EliteLifetime nextLT = (lt == null) ? (ltLvls.isEmpty() ? null : ltLvls.first()) : ltLvls.higher(lt); // check if empty, if so then get nothing
				tt.mark("totals");
				
				// Calculate the sore
				FlightEliteScore sc = null;
				if (fr instanceof FDRFlightReport ffr) {
					Aircraft ac = acdao.get(fr.getEquipmentType());
					AircraftPolicyOptions opts = ac.getOptions(SystemData.get("airline.code"));
					
					// Load the archived positions
					Collection<RouteEntry> entries = new ArrayList<RouteEntry>();
					File f = ArchiveHelper.getPositions(fr.getDatabaseID(DatabaseID.ACARS));
					try {
						Compression c = Compression.detect(f);
						try (InputStream is = c.getStream(new BufferedInputStream(new FileInputStream(f), 32768))) {
							GetSerializedPosition psdao = new GetSerializedPosition(is);
							entries.addAll(psdao.read());
						}
					} catch (IOException ie) {
						log.error("Error reading positions for Flight {} - {}", Integer.valueOf(fr.getDatabaseID(DatabaseID.ACARS)), ie.getMessage());
					}
					
					// Get the Gates and Runways
					FlightInfo info = fidao.getInfo(fr.getDatabaseID(DatabaseID.ACARS));
					gdao.populate(info);
					tt.mark("acarsData");
					
					// Create the package
					ScorePackage pkg = new ScorePackage(ac, ffr, null, info.getRunwayA(), opts);
					pkg.setGates(info.getGateD(), info.getGateA());
					entries.forEach(pkg::add);
					sc = es.score(pkg, st.getLevel());
				} else
					sc  = es.score(fr, st.getLevel());
				
				// Write the score and status history
				tt.mark("score");
				fr.addStatusUpdate(0, HistoryType.ELITE, String.format("Updated %s activity - %d %s", SystemData.get("econ.elite.name"), Integer.valueOf(sc.getPoints()), SystemData.get("econ.elite.points")));
				frwdao.writeElite(sc, ctx.getDB());
				frwdao.writeHistory(fr.getStatusUpdates(), ctx.getDB());
				pilotIDs.add(Integer.valueOf(fr.getAuthorID()));
				
				// Check for upgrade
				UpgradeReason updR = total.wouldMatch(nextLevel, sc); 
				if ((nextLevel != null) && (updR != UpgradeReason.NONE)) {
					log.warn("{} reaches {} for {} / {}", p.getName(), nextLevel.getName(), Integer.valueOf(yr), updR.getDescription());
					st = new EliteStatus(p.getID(), nextLevel);
					st.setEffectiveOn(Instant.now());
					st.setUpgradeReason(updR);
					elwdao.write(st);
					
					StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.ELITE_QUAL);
					upd.setDate(Instant.now());
					upd.setAuthorID(p.getID());
					upd.setDescription(String.format("Reached %s for %d ( %s )", nextLevel.getName(), Integer.valueOf(yr), updR.getDescription()));
					updwdao.write(upd, ctx.getDB());
					tt.mark("upgrade");
				} else if (nextLevel != null) {
					YearlyTotal nlt = new YearlyTotal(nextLevel.getYear(), p.getID());
					nlt.addLegs(nextLevel.getLegs(), nextLevel.getDistance(), nextLevel.getPoints());
					log.info("{} does not reach {} - {} < {}", p.getName(), nextLevel.getName(), total, nlt);
				}
				
				
				// Check for lifetime status upgrade
				updR = total.wouldMatch(nextLT, sc);
				if ((nextLT != null) && (updR != UpgradeReason.NONE)) {
					log.warn("{} reaches {} / {}", p.getName(), nextLT.getName(), updR.getDescription());
					EliteLifetimeStatus newLT = new EliteLifetimeStatus(p.getID(), nextLT);
					newLT.setEffectiveOn(Instant.now());
					newLT.setUpgradeReason(updR);
					elwdao.write(newLT);
					
					StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.ELITE_QUAL);
					upd.setDate(Instant.now());
					upd.setAuthorID(p.getID());
					upd.setDescription(String.format("Reached %s ( %s )", nextLT.getName(), updR.getDescription()));
					tt.mark("upgradeLT");
				}
				
				ctx.commitTX();
				i.remove();
				log.log((tt.stop() > 1250) ? Level.WARN : Level.INFO, "Scored Flight Report #{} - {} pts {}", Integer.valueOf(fr.getID()), Integer.valueOf(sc.getPoints()), tt);
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.atError().withThrowable(de).log(de.getMessage());
		} finally {
			ctx.release();
		}
		
		log.info("Processing Complete");
	}
}