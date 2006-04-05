// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.FlightReport;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display Flight Reports awaiting disposition.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PIREPQueueCommand extends AbstractViewCommand {
	
	private static final Integer PENDING[] = { new Integer(FlightReport.SUBMITTED), new Integer(FlightReport.HOLD) };

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error (typically database) occurs
     */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check our access level
		if (!ctx.getRequest().isUserInRole("PIREP"))
			throw securityException("Cannot access PIREP queue");
		
        // Get/set start/count parameters
        ViewContext vc = initView(ctx);

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO
			GetFlightReports dao = new GetFlightReports(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			
			// Get the PIREPs and load the promotion type
			Collection<FlightReport> pireps = dao.getByStatus(Arrays.asList(PENDING));
			dao.getCaptEQType(pireps);
			vc.setResults(pireps);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/pirepQueue.jsp");
		result.setSuccess(true);
	}
}