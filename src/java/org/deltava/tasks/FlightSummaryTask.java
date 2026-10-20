// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.sql.Connection;
import java.util.Collection;

import org.apache.logging.log4j.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.acars.FlightInfo;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.http.GetAIAnalysis;

import org.deltava.taskman.*;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to generate Flight Summaries.
 * @author Luke
 * @version 13.0
 * @since 12.5
 */

public class FlightSummaryTask extends Task {

	/**
	 * Creates the Task.
	 */
	public FlightSummaryTask() {
		super("Flight Summarizer", FlightSummaryTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {
		try {
			Connection con = ctx.getConnection();
			
			// Get the pending flights to smmarize
			GetFlightReportQueue frqdao = new GetFlightReportQueue(con);
			Collection<Integer> IDs = frqdao.getSummaryQueue();
			ctx.release();
			
			// Summarize the flights
			GetPilot pdao = new GetPilot(con);
			GetAircraft acdao = new GetAircraft(con);
			GetGates gdao = new GetGates(con);
			GetACARSData fidao = new GetACARSData(con);
			GetACARSPositions posdao = new GetACARSPositions(con);
			GetFlightReports frdao = new GetFlightReports(con);
			SetFlightReport frwdao = new SetFlightReport(con);
			SetFlightReportQueue frqwao = new SetFlightReportQueue(con);
			for (Integer id : IDs) {
				con = ctx.getConnection();
				log.info("Summarizing Flight Report {}", id);
				FlightReport fr = frdao.get(id.intValue(), ctx.getDB());
				FlightInfo inf = fidao.getInfo(fr.getDatabaseID(DatabaseID.ACARS));
				if (!(fr instanceof ACARSFlightReport afr) || (inf == null)) {
					log.warn("Flight {} not an ACARS Flight Report!");
					frqwao.clear(id.intValue());
					break;
				}
				
				// Create the scoring package
				gdao.populate(inf);
				Pilot p = pdao.get(afr.getAuthorID());
				Aircraft ac = acdao.get(afr.getEquipmentType());
				AircraftPolicyOptions opts = ac.getOptions(SystemData.get("airline.code"));
				ScorePackage pkg = new ScorePackage(ac, afr, p, inf.getRunwayD(), inf.getRunwayA(), opts);
				
				// Load the positions/gates
				pkg.setGates(inf.getGateD(), inf.getGateA());
				posdao.getRouteEntries(inf.getID(), inf.getArchived()).forEach(pkg::add);
				ctx.release();
				
				// Pass to Claude and update
				FlightAnalysis fa = null;
				try {
					GetAIAnalysis sdao = new GetAIAnalysis(pkg);
					sdao.setConnectTimeout(3500);
					sdao.setReadTimeout(45000);
					fa = sdao.execute();
					log.log((fa.getGenerationTime() > 30_000) ? Level.WARN : Level.INFO, "Summary for Flight Report {} loaded in {}ms", id, Long.valueOf(fa.getGenerationTime()));
				} catch (DAOException de) {
					log.error("Error loading AI Summary for Flight {} - {}", id, de.getMessage());
				}
				
				// Write the summary
				if (fa != null) {
					con = ctx.getConnection();
					ctx.startTX();
					frwdao.writeAnalysis(fa, ctx.getDB());
					frqwao.clear(id.intValue());
				}
				
				ctx.release();
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.atError().withThrowable(de).log(de.getMessage());
		} finally {
			ctx.release();
		}

		log.info("Complete");
	}
}