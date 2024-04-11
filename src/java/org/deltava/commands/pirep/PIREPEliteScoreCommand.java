// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Compression;
import org.deltava.beans.Pilot;
import org.deltava.beans.acars.*;
import org.deltava.beans.econ.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.file.GetSerializedPosition;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to recalculate Elite program scoring for a Flight Report.
 * @author Luke
 * @version 11.2
 * @since 11.2
 */

public class PIREPEliteScoreCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check that we have an elite program
		if (!SystemData.getBoolean("econ.elite.enabled"))
			throw new CommandException("No Elite program", false);
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Flight Report
			GetFlightReports dao = new GetFlightReports(con);
			FlightReport fr = dao.get(ctx.getID(), ctx.getDB());
			if (fr == null)
				throw notFoundException(String.format("Invalid Flight Report - %d", Integer.valueOf(ctx.getID())));
			
			// Check our access
			PIREPAccessControl access = new PIREPAccessControl(ctx, fr);
			access.validate();
			if (!access.getCanEliteRescore())
				throw securityException(String.format("Cannot recalculate %s score for Flight %d", SystemData.get("econ.elite.name"), Integer.valueOf(fr.getID())));
			
			// Load the pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(fr.getAuthorID());
			
			// Get elite status for the year
			GetElite eldao = new GetElite(con);
			EliteStatus st = eldao.getStatus(fr.getAuthorID(), EliteScorer.getStatusYear(fr.getDate()));
			if (st == null)
				throw notFoundException("No Elite status for " + p.getName());
			
			// Get the scorer and previous flight reports 
			EliteScorer es = EliteScorer.getInstance();
			List<FlightReport> pireps = dao.getEliteFlights(fr.getAuthorID(), EliteScorer.getStatsYear(fr.getDate()));
			pireps.stream().filter(pirep -> pirep.getSubmittedOn().isBefore(fr.getSubmittedOn())).forEach(es::add);
			
			// Get the existing flight score
			FlightEliteScore osc = dao.getElite(fr.getID());
			
			// Calculate the new score
			FlightEliteScore sc = null;
			if (fr instanceof FDRFlightReport ffr) {
				GetAircraft acdao = new GetAircraft(con);
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
					throw new DAOException(ie);
				}
				
				// Get the landing runway
				GetACARSData fidao = new GetACARSData(con);
				RunwayDistance rwyA = fidao.getLandingRunway(fr.getDatabaseID(DatabaseID.ACARS));
				
				// Create the package
				ScorePackage pkg = new ScorePackage(ac, ffr, null, rwyA, opts);
				entries.forEach(pkg::add);
				sc = es.score(pkg, st.getLevel());
			} else
				sc  = es.score(fr, st.getLevel());
			
			// Compare scores and update if different
			if (!sc.equals(osc)) {
				fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.ELITE, String.format("Recalculated %s activity - %d %s", SystemData.get("econ.elite.name"), Integer.valueOf(sc.getPoints()), SystemData.get("econ.elite.points")));
				
				ctx.startTX();
				SetFlightReport frwdao = new SetFlightReport(con);
				frwdao.writeElite(sc, ctx.getDB());
				frwdao.writeHistory(fr.getStatusUpdates(), ctx.getDB());
				ctx.commitTX();
			}
			
			// Save status attributes
			ctx.setAttribute("isEliteScore", Boolean.TRUE, REQUEST);
			ctx.setAttribute("score", sc, REQUEST);
			ctx.setAttribute("oldScore", osc, REQUEST);
			ctx.setAttribute("pirep", fr, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/pirepUpdate.jsp");
		result.setSuccess(true);
	}
}