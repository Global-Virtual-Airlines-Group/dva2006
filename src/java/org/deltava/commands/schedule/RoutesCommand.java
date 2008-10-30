// Copyright 2005, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import org.deltava.commands.*;

import org.deltava.dao.GetRoute;
import org.deltava.dao.DAOException;

import org.deltava.security.command.ScheduleAccessControl;

/**
 * Web site command to return Preferred/Oceanic routes.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class RoutesCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {
        
        // Get the view context
        ViewContext vc = initView(ctx);
        
        // Check our access
        ScheduleAccessControl access = new ScheduleAccessControl(ctx);
        access.validate();
        ctx.setAttribute("access", access, REQUEST);
        
        try {
            GetRoute dao = new GetRoute(ctx.getConnection());
            dao.setQueryStart(vc.getStart());
            dao.setQueryMax(vc.getCount());
            vc.setResults(dao.getOceanic());
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Redirect to the JSP
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/schedule/oRoutes.jsp");
        result.setSuccess(true);
    }
}