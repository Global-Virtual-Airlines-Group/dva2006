// Copyright 2008, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display Suspended users.
 * @author Luke
 * @version 7.0
 * @since 2.2
 */

public class SuspendedUserListCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the view context
		ViewContext vctx = initView(ctx);
		try {
			Connection con = ctx.getConnection();
			
			// Get the pilots
			GetPilot pdao = new GetPilot(con);
			pdao.setQueryStart(vctx.getStart());
			pdao.setQueryMax(vctx.getCount());
			Collection<Pilot> pilots = pdao.getPilotsByStatus(Pilot.SUSPENDED);
			vctx.setResults(pilots);
			
			// Load the IDs
			Collection<Integer> IDs = pilots.stream().map(Pilot::getID).collect(Collectors.toSet());
			
			// Load the status updates
			Map<Integer, StatusUpdate> updates = new HashMap<Integer, StatusUpdate>();
			GetStatusUpdate sudao = new GetStatusUpdate(con);
			Collection<StatusUpdate> upds = sudao.getByType(StatusUpdate.SUSPENDED);
			for (StatusUpdate upd : upds) {
				Integer id = Integer.valueOf(upd.getID());
				if (IDs.contains(id)) {
					if (updates.containsKey(id)) {
						StatusUpdate upd2 = updates.get(id);
						if (upd.compareTo(upd2) > 0)
							updates.put(id, upd);
					} else
						updates.put(id, upd);
				}
			}
			
			// Save the status updates
			ctx.setAttribute("updates", updates, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save current date
		ctx.setAttribute("now", Instant.now(), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/suspendedUsers.jsp");
		result.setSuccess(true);
	}
}