// Copyright 2010, 2012, 2015, 2016, 2017, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.time.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.deltava.beans.Pilot;
import org.deltava.beans.acars.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.*;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to recalculate what Accomplishments a Pilot has achieved.
 * @author Luke
 * @version 9.1
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
			
			// Instantiate the helper
			AccomplishmentHistoryHelper helper = new AccomplishmentHistoryHelper(p);

			// Load the Pilot's Flight Reports
			GetFlightReports frdao = new GetFlightReports(con);
			Collection<FlightReport> flights = frdao.getByPilot(p.getID(), null);
			frdao.getCaptEQType(flights);
			flights.forEach(helper::add);
			
			// Load the Pilot's Dispatch entries
			GetACARSLog acdao = new GetACARSLog(con);
			GetDispatchCalendar dcdao = new GetDispatchCalendar(con);
			Collection<ConnectionEntry> cons = acdao.getConnections(new LogSearchCriteria(p.getID()));
			for (ConnectionEntry ce : cons) {
				DispatchConnectionEntry dce = (DispatchConnectionEntry) ce;
				Collection<FlightInfo> dspFlights = dcdao.getDispatchedFlights(dce);
				dce.addFlights(dspFlights);
				helper.add(ce);
			}
			
			// Load existing accomplishments, and recalculate
			Map<Integer, DatedAccomplishment> pAccs = Collections.unmodifiableMap(CollectionUtils.createMap(adao.getByPilot(p, SystemData.get("airline.db")), Accomplishment::getID));
			Collection<DatedAccomplishment> newAccs = accs.parallelStream().map(a -> accCheck(a, helper, pAccs.get(Integer.valueOf(a.getID())))).filter(Objects::nonNull).collect(Collectors.toCollection(TreeSet::new));
			Collection<DatedAccomplishment> deltaAccs = CollectionUtils.getDelta(newAccs, pAccs.values());

			// Start a transaction
			ctx.startTX();
			
			// Clear the user's accomplishments and rewrite everything
			SetAccomplishment awdao = new SetAccomplishment(con);
			for (Accomplishment a : pAccs.values())
				awdao.clearAchievement(p.getID(), a);
			for (DatedAccomplishment da : newAccs)
				awdao.achieve(da.getPilotID(), da, da.getDate());
			
			// Commit and clear cache
			ctx.commitTX();
			CacheManager.invalidate("Pilots", p.cacheKey());
			
			// Write status variable
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("accs", deltaAccs, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status variable
		ctx.setAttribute("accomplishUpdate", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/pilotUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
	
	private static DatedAccomplishment accCheck(Accomplishment a, AccomplishmentHistoryHelper helper, DatedAccomplishment da2) {
		Instant dt = helper.achieved(a);		
		if (dt == null) return null;
		
		DatedAccomplishment da = new DatedAccomplishment(helper.getPilotID(), dt, a);
		if (da2 == null) return da;
		
		// Return updated accomplishment only if the date has changed by >1 day
		Duration d = Duration.between(dt, da2.getDate());
		return (d.abs().getSeconds() > 86400) ? da : da2;
	}
}