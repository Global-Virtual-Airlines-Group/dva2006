// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.pilot;

import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetFlightReports;
import org.deltava.dao.DAOException;

/**
 * A Web Site Command to display the smoothest landings.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GreasedLandingCommand extends AbstractCommand {

	 /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {

        try {
            Connection con = ctx.getConnection();
            
            // Get the DAO and the results
            GetFlightReports dao = new GetFlightReports(con);
            dao.setQueryMax(25);
            ctx.setAttribute("pireps", dao.getGreasedLandings(), REQUEST);
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Forward to the JSP
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/roster/greasedLandings.jsp");
        result.setSuccess(true);
    }
}