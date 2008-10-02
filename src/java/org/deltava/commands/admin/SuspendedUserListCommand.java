// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display Suspended users.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class SuspendedUserListCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
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
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Iterator<Pilot> i = pilots.iterator(); i.hasNext(); ) {
				Pilot p = i.next();
				IDs.add(new Integer(p.getID()));
			}
			
			// Load the status updates
			Map<Integer, StatusUpdate> updates = new HashMap<Integer, StatusUpdate>();
			GetStatusUpdate sudao = new GetStatusUpdate(con);
			Collection<StatusUpdate> upds = sudao.getByType(StatusUpdate.SUSPENDED);
			for (Iterator<StatusUpdate> i = upds.iterator(); i.hasNext(); ) {
				StatusUpdate upd = i.next();
				Integer id = new Integer(upd.getID());
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
		ctx.setAttribute("now", new Date(), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/suspendedUsers.jsp");
		result.setSuccess(true);
	}
}