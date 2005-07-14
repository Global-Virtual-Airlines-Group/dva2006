// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetChart;
import org.deltava.dao.DAOException;

import org.deltava.security.command.ChartAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web site Command to display Approach Charts.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ChartsCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {
        
        // Get airport code to display and save it into the request
        String aCode = (String) ctx.getCmdParameter(Command.ID, "ATL");
        ctx.setAttribute("airport", SystemData.getAirport(aCode), REQUEST);
        
        // Get our access level
        ChartAccessControl access = new ChartAccessControl(ctx);
        access.validate();
        ctx.setAttribute("access", access, REQUEST);
        
        try {
            Connection con = ctx.getConnection();
            
            // Get available airport codes and charts
            GetChart dao = new GetChart(con); 
            ctx.setAttribute("airports", dao.getAirports(), REQUEST);
            ctx.setAttribute("charts", dao.getCharts(aCode), REQUEST);
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }

        // Redirect to the home page
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/schedule/charts.jsp");
        result.setSuccess(true);
    }
}