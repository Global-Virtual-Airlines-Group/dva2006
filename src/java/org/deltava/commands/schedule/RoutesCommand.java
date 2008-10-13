// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetRoute;
import org.deltava.dao.DAOException;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.StringUtils;

/**
 * Web site command to return Preferred/Oceanic routes.
 * @author Luke
 * @version 1.0
 * @since 1.0
 * @deprecated
 */

@Deprecated
public class RoutesCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {
        
        // Get the view context
        ViewContext vc = initView(ctx);
        
        // Determine if we are doing oceanic routes
        boolean isOceanic = "oceanic".equals(ctx.getCmdParameter(OPERATION, "domestic"));
        
        // Check our access
        ScheduleAccessControl access = new ScheduleAccessControl(ctx);
        access.validate();
        ctx.setAttribute("access", access, REQUEST);
        
        try {
            Connection con = ctx.getConnection();

            // Get the DAO
            GetRoute dao = new GetRoute(con);
            dao.setQueryStart(vc.getStart());
            dao.setQueryMax(vc.getCount());
            
            // If we're not displaying oceanic routes, get the domestic routes and the available airports
            if (!isOceanic) {
                ctx.setAttribute("airports", dao.getAirports(), REQUEST);
                
                // Get the airport codes
                String dCode = (String) ctx.getCmdParameter(ID, "ATL");
                String aCode = StringUtils.isEmpty(ctx.getParameter("airportA")) ? null : ctx.getParameter("airportA");
                
                // Save the airport codes
                ctx.setAttribute("airportD", dCode, REQUEST);
                ctx.setAttribute("airportA", aCode, REQUEST);
                
                // Get the destination airports
                ctx.setAttribute("dstAP", dao.getRouteDestinations(dCode), REQUEST);
                
                // Load the routes
                vc.setResults(dao.getRoutes(dCode, aCode));
            } else {
                vc.setResults(dao.getOceanic());
            }
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Redirect to the JSP
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/schedule/" + (isOceanic ? "oRoutes" : "pRoutes") + ".jsp");
        result.setSuccess(true);
    }
}