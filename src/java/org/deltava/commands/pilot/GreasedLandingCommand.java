// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetFlightReports;
import org.deltava.dao.DAOException;

import org.deltava.util.ComboUtils;

/**
 * A Web Site Command to display the smoothest landings.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GreasedLandingCommand extends AbstractViewCommand {
	
	private static final List DATE_FILTER = ComboUtils.fromArray(new String[] {"All Landings", "30 Days", "60 Days", "90 Days" }, 
			new String[] {"0", "30", "60", "90"});

	 /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {
    	
    	// Load the view context
    	ViewContext vc = initView(ctx, 25);
    	
    	// Check how many days back to search
    	int daysBack = 30;
    	try {
    		daysBack = Integer.parseInt(ctx.getParameter("days"));
    	} catch (Exception e) {
    		daysBack = 0;
    	}

        try {
            Connection con = ctx.getConnection();
            
            // Get the DAO and the results
            GetFlightReports dao = new GetFlightReports(con);
            dao.setQueryMax(vc.getCount());
            ctx.setAttribute("pireps", dao.getGreasedLandings(daysBack), REQUEST);
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Save combobox choices
        ctx.setAttribute("dateFilter", DATE_FILTER, REQUEST);
        
        // Forward to the JSP
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/roster/greasedLandings.jsp");
        result.setSuccess(true);
    }
}