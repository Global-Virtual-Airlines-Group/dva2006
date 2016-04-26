// Copyright 2005, 2006, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.time.Instant;

import org.deltava.beans.navdata.OceanicTrackInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display Oceanic Route data.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class RouteCommand extends AbstractCommand {

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the route type and date
		OceanicTrackInfo.Type rType;
		Instant vd = Instant.now();
		try {
			rType = OceanicTrackInfo.Type.valueOf((String) ctx.getCmdParameter(OPERATION, "NAT"));
			vd = StringUtils.parseInstant((String) ctx.getCmdParameter(ID, null), "MMddyyyy");
		} catch (Exception e) {
			rType = OceanicTrackInfo.Type.NAT;
		}
		
		try {
			GetOceanicRoute dao = new GetOceanicRoute(ctx.getConnection());
			ctx.setAttribute("route", dao.get(rType, vd), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Get our access level
		ScheduleAccessControl access = new ScheduleAccessControl(ctx);
		access.validate();
		ctx.setAttribute("access", access, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/oRoute.jsp");
		result.setSuccess(true);
	}
}