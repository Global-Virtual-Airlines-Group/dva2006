// Copyright 2005, 2006, 2009, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.time.Instant;

import org.deltava.beans.navdata.OceanicTrackInfo.Type;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.*;

/**
 * A Web Site Command to display Oceanic Route data.
 * @author Luke
 * @version 9.1
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
		Type rType = EnumUtils.parse(Type.class, (String) ctx.getCmdParameter(OPERATION, "NAT"), Type.NAT);
		Instant vd = StringUtils.parseInstant((String) ctx.getCmdParameter(ID, null), "MMddyyyy");
		
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