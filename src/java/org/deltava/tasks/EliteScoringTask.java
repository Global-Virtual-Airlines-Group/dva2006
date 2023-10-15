// Copyright 2020, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.time.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.econ.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.file.GetSerializedPosition;

import org.deltava.taskman.*;
import org.deltava.util.TaskTimer;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to calculate Elite scores for Flight Reports. 
 * @author Luke
 * @version 11.1
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
		log.info("Scoring flights approved in the past 31 days");
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAOs
			GetPilot pdao = new GetPilot(con);
			GetElite eldao = new GetElite(con);
			GetAircraft acdao = new GetAircraft(con);
			GetFlightReports frdao = new GetFlightReports(con);
			GetACARSData fidao = new GetACARSData(con);
			
			SetElite elwdao = new SetElite(con);
			SetFlightReport frwdao = new SetFlightReport(con);
			SetStatusUpdate updwdao = new SetStatusUpdate(con);
			
			// Get the Flight Reports
			GetEliteStatistics esdao = new GetEliteStatistics(con);
			GetFlightReportStatistics frsdao = new GetFlightReportStatistics(con);
			frsdao.setDayFilter(31);
			Collection<Integer> IDs = frsdao.getUnscoredFlights();
			log.warn("Scoring {} flights", Integer.valueOf(IDs.size()));
			
			int lastID = 0; final List<FlightReport> pireps = new ArrayList<FlightReport>();
			for (Iterator<Integer> i = IDs.iterator(); i.hasNext(); ) {
				Integer id = i.next();
				ctx.startTX();
				TaskTimer tt = new TaskTimer();
				EliteScorer es = EliteScorer.getInstance();
				FlightReport fr = frdao.get(id.intValue(), ctx.getDB());
				final int yr = EliteScorer.getStatusYear(fr.getDate());
				TreeSet<EliteLevel> lvls = eldao.getLevels(yr);
				
				// Get the pilot and Elite data - if we have no status see if we need to do a rollover
				Pilot p = pdao.get(fr.getAuthorID());
				EliteStatus st = eldao.getStatus(p.getID(), yr);
				if (st == null) {
					st = new EliteStatus(p.getID(), lvls.first());
					st.setEffectiveOn(fr.getSubmittedOn());
					elwdao.write(st);
				}
				
				// Load all previous Flight Reports for this Pilot
				if (p.getID() != lastID) {
					lastID = p.getID();
					pireps.clear();
					pireps.addAll(frdao.getEliteFlights(p.getID(), EliteScorer.getStatsYear(fr.getDate())));
					long ms = TimeUnit.MILLISECONDS.convert(tt.getInterval(), TimeUnit.NANOSECONDS);
					log.info("Loaded {} flights for {} ({}) in {}ms", Integer.valueOf(pireps.size()), p.getName(), p.getPilotCode(), Long.valueOf(ms));
				}
				
				pireps.stream().filter(pirep -> !IDs.contains(Integer.valueOf(pirep.getID()))).forEach(es::add);
				
				// Get our total and next level
				YearlyTotal total = esdao.getEliteTotals(p.getID()).stream().filter(yt -> yt.getYear() == yr).findFirst().orElse(new YearlyTotal(yr, p.getID()));
				EliteLevel nextLevel = lvls.higher(st.getLevel());
				
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
					
					// Get the landing runway
					RunwayDistance rwyA = fidao.getLandingRunway(fr.getDatabaseID(DatabaseID.ACARS));
					
					// Create the package
					ScorePackage pkg = new ScorePackage(ac, ffr, null, rwyA, opts);
					entries.forEach(pkg::add);
					sc = es.score(pkg, st.getLevel());
				} else
					sc  = es.score(fr, st.getLevel());
				
				// Write the score and status history
				fr.addStatusUpdate(0, HistoryType.ELITE, String.format("Updated %s activity - %d %s", SystemData.get("econ.elite.name"), Integer.valueOf(sc.getPoints()), SystemData.get("econ.elite.points")));
				frwdao.writeElite(sc, ctx.getDB());
				frwdao.writeHistory(fr.getStatusUpdates(), ctx.getDB());
				
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
				}
				
				ctx.commitTX();
				i.remove();
				long ms = tt.stop();
				if (ms > 1250)
					log.warn("Scored Flight Report #{} - {} pts ({} ms)", Integer.valueOf(fr.getID()), Integer.valueOf(sc.getPoints()), Long.valueOf(ms));
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