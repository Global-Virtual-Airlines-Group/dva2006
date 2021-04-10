// Copyright 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.io.*;
import java.util.*;
import java.time.*;
import java.sql.Connection;
import java.util.zip.GZIPInputStream;

import org.deltava.beans.Pilot;
import org.deltava.beans.acars.*;
import org.deltava.beans.econ.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.dao.*;
import org.deltava.dao.file.GetSerializedPosition;

import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to calculate Elite scores for Flight Reports. 
 * @author Luke
 * @version 10.0
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
		AirlineInformation ai = SystemData.getApp(null);
		log.info("Scoring flights submitted in the past 30 days");
		
		try {
			Connection con = ctx.getConnection();
			ctx.startTX();
			
			// Get the DAOs
			GetPilot pdao = new GetPilot(con);
			GetElite eldao = new GetElite(con);
			GetAircraft acdao = new GetAircraft(con);
			GetFlightReports frdao = new GetFlightReports(con);
			
			SetElite elwdao = new SetElite(con);
			SetFlightReport frwdao = new SetFlightReport(con);
			
			// Get the Flight Reports
			GetEliteStatistics esdao = new GetEliteStatistics(con);
			GetFlightReportStatistics frsdao = new GetFlightReportStatistics(con);
			frsdao.setDayFilter(31);
			ScheduleSearchCriteria ssc = new ScheduleSearchCriteria("DATE, PR.SUBMITTED");
			Collection<Integer> IDs = frsdao.getUnscoredFlights();
			for (Integer id : IDs) {
				ctx.startTX();
				
				FlightReport fr = frdao.get(id.intValue(), ctx.getDB());
				final int yr = EliteLevel.getYear(fr.getDate());
				TreeSet<EliteLevel> lvls = eldao.getLevels(yr);
				
				// Get the pilot and Elite data - if we have no status see if we need to do a rollover
				Pilot p = pdao.get(fr.getAuthorID());
				EliteStatus st = eldao.getStatus(p.getID());
				if (st == null) {
					st = new EliteStatus(p.getID(), lvls.first());
					st.setEffectiveOn(fr.getSubmittedOn());
					elwdao.write(st);
				}
				
				// Load all previous Flight Reports for this Pilot
				PointScorer es = PointScorer.init(SystemData.get("econ.elite.scorer"));
				List<FlightReport> pireps = frdao.getByPilot(p.getID(), ssc);
				pireps.stream().filter(pirep -> !IDs.contains(Integer.valueOf(pirep.getID()))).forEach(es::add);
				
				// Get our total and next level
				YearlyTotal total = esdao.getEliteTotals(p.getID()).stream().filter(yt -> yt.getYear() == yr).findFirst().orElse(new YearlyTotal(yr, p.getID()));
				EliteLevel nextLevel = lvls.higher(st.getLevel());
				
				// Calculate the sore
				FlightEliteScore sc = null;
				if (fr instanceof FDRFlightReport) {
					Aircraft ac = acdao.get(fr.getEquipmentType());
					AircraftPolicyOptions opts = ac.getOptions(ai.getCode());
					
					// Load the archived positions
					Collection<RouteEntry> entries = new ArrayList<RouteEntry>();
					File f = ArchiveHelper.getPositions(fr.getDatabaseID(DatabaseID.ACARS));
					try (InputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(f), 32768))) {
						GetSerializedPosition psdao = new GetSerializedPosition(is);
						entries.addAll(psdao.read());
					} catch (IOException ie) {
						log.error("Error reading positions for Flight " + fr.getDatabaseID(DatabaseID.ACARS) + " - " + ie.getMessage());
					}
					
					// Create the package
					ScorePackage pkg = new ScorePackage(ac, (FDRFlightReport) fr, null, null, opts);
					entries.forEach(pkg::add);
					sc = es.score(pkg, st.getLevel());
				} else
					sc  = es.score(fr, st.getLevel());
				
				// Write the score and status history
				fr.addStatusUpdate(0, HistoryType.UPDATE, "Updated " + SystemData.get("econ.elite.name") + " activity");
				frwdao.writeElite(sc, ai.getDB());
				frwdao.writeHistory(fr.getStatusUpdates(), ctx.getDB());
				log.info("Scored Flight Report #" + fr.getID() + " - " + sc.getPoints());
				
				// Check for upgrade
				UpgradeReason updR = total.wouldMatch(nextLevel, sc.getDistance(), sc.getPoints()); 
				if ((nextLevel != null) && (updR != UpgradeReason.NONE)) {
					log.warn(p.getName() + " reaches " + nextLevel.getName() + " for " + yr + " / " + updR.getDescription());
					st = new EliteStatus(p.getID(), nextLevel);
					st.setEffectiveOn(Instant.now());
					st.setUpgradeReason(updR);
					elwdao.write(st);
				}
				
				ctx.commitTX();
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		log.info("Processing Complete");
	}
}