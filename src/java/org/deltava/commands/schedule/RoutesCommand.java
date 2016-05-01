// Copyright 2005, 2008, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import org.deltava.beans.schedule.OceanicNOTAM;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

/**
 * A Web Site Command to return Preferred/Oceanic routes.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class RoutesCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    @Override
	public void execute(CommandContext ctx) throws CommandException {
        
        ViewContext<OceanicNOTAM> vc = initView(ctx, OceanicNOTAM.class);
        try {
            GetOceanicRoute dao = new GetOceanicRoute(ctx.getConnection());
            dao.setQueryStart(vc.getStart());
            dao.setQueryMax(vc.getCount());
            vc.setResults(dao.getOceanic());
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Check our access
        ScheduleAccessControl access = new ScheduleAccessControl(ctx);
        access.validate();
        ctx.setAttribute("access", access, REQUEST);
        
        // Redirect to the JSP
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/schedule/oRoutes.jsp");
        result.setSuccess(true);
    }
}