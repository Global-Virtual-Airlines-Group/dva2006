// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.time.Instant;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.econ.*;

import org.deltava.commands.*;

import org.deltava.util.ComboUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to show load factor calculation data.
 * @author Luke
 * @version 11.2
 * @since 11.2
 */

public class LoadInfoCommand extends AbstractCommand {
	
	private static final Collection<ComboAlias> DAY_OPTS = ComboUtils.fromArray(new String[] { "90 Days", "180 Days", "1 Year", "2 Years"}, new String[] {"90", "180", "365", "720"});

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the calculator
		EconomyInfo eInfo = (EconomyInfo) SystemData.getObject(SystemData.ECON_DATA);
		if (eInfo == null)
			throw new CommandException("No Economy data for Airline");

		// Save parameters
		ctx.setAttribute("dayOpts", DAY_OPTS, REQUEST);
		ctx.setAttribute("days", "365", REQUEST);
		ctx.setAttribute("targetLoad", Double.valueOf(SystemData.getDouble("econ.targetLoad", 0d)), REQUEST);
		ctx.setAttribute("minimumLoad", Double.valueOf(SystemData.getDouble("econ.minimumLoad", 0d)), REQUEST);
		
		// Get today's target load factor
		Instant now = Instant.now();
		LoadFactor lf = new LoadFactor(eInfo);
		ctx.setAttribute("today", now, REQUEST);
		ctx.setAttribute("dailyTargetLoad", Double.valueOf(lf.getTargetLoad(now)), REQUEST);
		
		// Fowrard to JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/loadStats.jsp");
		result.setSuccess(true);
	}
}