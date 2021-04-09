// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.econ;

import java.util.*;
import java.time.Instant;
import java.sql.Connection;

import org.deltava.beans.econ.EliteLevel;
import org.deltava.beans.stats.ElitePercentile;
import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to view statistics about the Elite program.
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public class EliteStatsCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		int year = EliteLevel.getYear(Instant.now());
		try {
			Connection con = ctx.getConnection();
			
			// Load current levels
			GetElite eldao = new GetElite(con);
			Collection<EliteLevel> lvls = eldao.getLevels();
			
			// Load stats
			GetEliteStatistics elsdao = new GetEliteStatistics(con);
			List<ElitePercentile> pcts = elsdao.getElitePercentiles(year, 1, false);
			
			
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/econ/eliteStats.jsp");
		result.setSuccess(true);
	}
}