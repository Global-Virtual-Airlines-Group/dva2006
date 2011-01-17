// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.acars.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.stats.*;
import org.deltava.beans.stats.Accomplishment.Unit;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to calculate what Pilots are eligible for an Accomplishment.
 * @author Luke
 * @version 3.6
 * @since 3.6
 */

public class AccomplishmentUpdateCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get command result
		CommandResult result = ctx.getResult();
		
		try {
			Connection con = ctx.getConnection();
			
			// Get all accomplishments
			GetAccomplishment adao = new GetAccomplishment(con);
			int id = ctx.getID();
			if (id == 0) {
				ctx.setAttribute("accomplishments", adao.getAll(), REQUEST);
				ctx.release();
				
				// Forward to the JSP
				result.setURL("/jsp/stats/accomplishmentRecalc.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Get the Accomplishment
			Accomplishment a = adao.get(id);
			if (a == null)
				throw notFoundException("Invalid Accomplishment ID - " + id);
			
			// Get the DAOs
			GetPilot pdao = new GetPilot(con);
			GetFlightReports frdao = new GetFlightReports(con);
			GetACARSLog acdao = new GetACARSLog(con);
			SetAccomplishment awdao = new SetAccomplishment(con);
			long lastUse = System.currentTimeMillis();
			
			// Validate Accomplishments
			List<Pilot> pilots = pdao.getActivePilots(null); 
			Collection<Pilot> awardedPilots = new ArrayList<Pilot>();
			Collection<Pilot> clearedPilots = new ArrayList<Pilot>();
			for (Iterator<Pilot> i = pilots.iterator(); i.hasNext(); ) {
				Pilot p = i.next();
				boolean hasA = adao.has(p.getID(), a);

				// Load Dispatch/Flight Report statistics
				AccomplishmentHistoryHelper helper = new AccomplishmentHistoryHelper(p);
				boolean isDispatch = (a.getUnit() == Unit.DFLIGHTS) || (a.getUnit() == Unit.DHOURS);
				if (isDispatch) {
					Collection<ConnectionEntry> cons = acdao.getConnections(new LogSearchCriteria(p.getID(), true));
					for (ConnectionEntry ce : cons)
						helper.add(ce);
				} else {
					Collection<FlightReport> pireps = frdao.getByPilot(p.getID(), null);
					for (FlightReport fr : pireps)
						helper.add(fr);
				}
				
				// Check accomplishment eligibility
				Date achieveDate = helper.achieved(a);
				if ((achieveDate != null) && !hasA) {
					awdao.achieve(p.getID(), a, achieveDate);
					awardedPilots.add(p);
				} else if ((achieveDate == null) && hasA) { 
					awdao.clearAchievement(p.getID(), a);
					clearedPilots.add(p);
				}
				
				// Check how long we've had the JDBC connection open - if >15s, reset it
				long now = System.currentTimeMillis();
				if ((now - lastUse) > 15000) {
					ctx.release();
					con = ctx.getConnection();
					lastUse = now;
				}
			}
			
			// Save status attributes
			ctx.setAttribute("accomplish", a, REQUEST);
			ctx.setAttribute("doAward", Boolean.TRUE, REQUEST);
			ctx.setAttribute("pilots", awardedPilots, REQUEST);
			ctx.setAttribute("cleared", clearedPilots, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		result.setURL("/jsp/stats/accomplishmentRecalc.jsp");
		result.setSuccess(true);
	}
}