// Copyright 2006, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;

import org.deltava.beans.schedule.Aircraft;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.AircraftAccessControl;

/**
 * A Web Site Command to display aircraft data.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class AircraftListCommand extends AbstractViewCommand {

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		ViewContext<Aircraft> vc = initView(ctx, Aircraft.class);
		try {
			GetAircraft dao = new GetAircraft(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(Math.round(vc.getCount() * 1.5f));
			
			// Calcualte access
			List<Aircraft> results = dao.getAll();
			Map<Aircraft, AircraftAccessControl> access = new HashMap<Aircraft, AircraftAccessControl>();
			for (Iterator<Aircraft> i = results.iterator(); i.hasNext(); ) {
				Aircraft a = i.next();
				AircraftAccessControl ac = new AircraftAccessControl(ctx, a);
				ac.validate();
				if (ac.getCanRead())
					access.put(a, ac);
				else
					i.remove();
			}
			
			vc.setResults((results.size() > vc.getCount()) ? results.subList(0, vc.getCount()) : results);
			ctx.setAttribute("accessMap", access, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save access rights
		AircraftAccessControl ac = new AircraftAccessControl(ctx, null);
		ac.validate();
		ctx.setAttribute("ac", ac, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/aircraftList.jsp");
		result.setSuccess(true);
	}
}