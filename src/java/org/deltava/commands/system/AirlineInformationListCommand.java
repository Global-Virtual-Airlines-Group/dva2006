// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;

import org.deltava.beans.system.AirlineInformation;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.AirlineInformationAccessControl;

/**
 * A Web Site Command to display all Virtual Airline profiles.
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class AirlineInformationListCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			GetUserData uddao = new GetUserData(ctx.getConnection());
			Map<String, AirlineInformation> apps = uddao.getAirlines(true);
			
			// Check our access
			Map<String, AirlineInformationAccessControl> access = new HashMap<String, AirlineInformationAccessControl>();
			for (Iterator<Map.Entry<String, AirlineInformation>> i = apps.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry<String, AirlineInformation> me = i.next();
				AirlineInformationAccessControl ac = new AirlineInformationAccessControl(me.getValue(), ctx);
				ac.validate();
				
				// Remove if no read access
				if (!ac.getCanRead())
					i.remove();
				else
					access.put(me.getKey(), ac);
			}
			
			// Save request attributes
			ctx.setAttribute("access", access, REQUEST);
			ctx.setAttribute("apps", apps.values(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/aInfoList.jsp");
		result.setSuccess(true);
	}
}