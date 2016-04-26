// Copyright 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.navdata.CycleInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display navigation cycle publish dates.
 * @author Luke
 * @version 7.0
 * @since 5.2
 */

public class NavigationCycleCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			Collection<CycleInfo> future = new TreeSet<CycleInfo>();
			
			// Get the current cycles
			GetNavCycle ncdao = new GetNavCycle(con);
			GetMetadata mddao = new GetMetadata(con);
			String cycleID = mddao.get("navdata.cycle");
			String chartCycleID = mddao.get("charts.cycle.faa");
			if (cycleID != null) {
				CycleInfo cc = ncdao.getCycle(cycleID);
				if (cc != null) {
					cc.setLoaded(true);
					future.add(cc);
					ctx.setAttribute("currentCycle", cc, REQUEST);
				}
			}
			
			if (chartCycleID != null) {
				CycleInfo cc = ncdao.getCycle(chartCycleID);
				future.add(cc);
				ctx.setAttribute("currentChartCycle", cc, REQUEST);	
			}

			// Get what we should be loading
			CycleInfo now = ncdao.getCycle(Instant.now());
			ctx.setAttribute("nowCycle", (now == null) ? CycleInfo.getCurrent() : now, REQUEST);

			// Get all future cycles
			future.add(now);
			future.addAll(ncdao.getFuture());
			ctx.setAttribute("cycles", future, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/navCycles.jsp");
		result.setSuccess(true);
	}
}