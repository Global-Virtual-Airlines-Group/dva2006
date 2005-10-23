// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.Collection;
import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetFlightReports;
import org.deltava.dao.DAOException;

/**
 * A Web Site Command to display Flight Reports awaiting disposition.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PIREPQueueCommand extends AbstractViewCommand {

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
			Collection pireps = dao.getDisposalQueue();
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