// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to recalculate what Accomplishments a Pilot has achieved.
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class AccomplishmentCheckCommand extends AbstractCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		Collection<DatedAccomplishment> newAccs = new TreeSet<DatedAccomplishment>();
		try {
			Connection con = ctx.getConnection();
			
			// Load the pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(ctx.getID());
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());
			
			// Check our access
			PilotAccessControl access = new PilotAccessControl(ctx, p);
			access.validate();
			if (!access.getCanChangeStatus())
				throw securityException("Cannot recalculate Pilot Accomplishments");
			
			// Load all Accomplishment profiles
			GetAccomplishment adao = new GetAccomplishment(con);
			Collection<Accomplishment> accs = adao.getAll();
			
			// Load the Pilot's Flight Reports
			GetFlightReports frdao = new GetFlightReports(con);
			Collection<FlightReport> flights = frdao.getByPilot(p.getID(), null);
			
			// Start a transaction
			ctx.startTX();
			
			// Clear the user's accomplishments
			SetAccomplishment awdao = new SetAccomplishment(con);
			Map<Integer, DatedAccomplishment> pAccs = CollectionUtils.createMap(adao.getByPilot(p.getID(), SystemData.get("airline.db")), "ID");
			for (Accomplishment a : pAccs.values())
				awdao.clearAchievement(p.getID(), a);
			
			// Instantiate the helper and loop through
			AccomplishmentHistoryHelper helper = new AccomplishmentHistoryHelper(p, flights);
			for (Accomplishment a : accs) {
				Date dt = helper.achieved(a);
				if (dt != null) {
					DatedAccomplishment da = new DatedAccomplishment(dt, a);
					DatedAccomplishment da2 = pAccs.get(Integer.valueOf(a.getID()));
					if (da2 != null) {
						long timeDiff = Math.abs(dt.getTime() - da2.getDate().getTime()) / 1000;
						if (timeDiff > 86400)
							newAccs.add(da);	
					} else
						newAccs.add(da);
					
					// Write the accomplishment
					awdao.achieve(p.getID(), da, dt);
				}
			}
			
			// Commit
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status variables
		ctx.setAttribute("accomplishUpdate", Boolean.TRUE, REQUEST);
		ctx.setAttribute("accs", newAccs, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/pilotUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}