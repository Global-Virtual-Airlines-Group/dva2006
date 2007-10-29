// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;

import org.deltava.beans.Pilot;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.security.command.*;

/**
 * A Web Site Command to display Pilots eligible for Promotion.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PromotionQueueCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		Collection<Pilot> pilots = null;
		try {
			GetPilotRecognition dao = new GetPilotRecognition(ctx.getConnection());
			Collection<Integer> IDs = dao.getPromotionQueue();
			pilots = dao.getByID(IDs, "PILOTS").values();
			ctx.setAttribute("queue", pilots, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Check pilot access
		Map<Integer, AccessControl> accessMap = new HashMap<Integer, AccessControl>();
		for (Iterator<Pilot> i = pilots.iterator(); i.hasNext();) {
			Pilot p = i.next();
			PilotAccessControl access = new PilotAccessControl(ctx, p);
			access.validate();
			accessMap.put(new Integer(p.getID()), access);
		}

		// Save pilot access
		ctx.setAttribute("accessMap", accessMap, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/roster/promotionQueue.jsp");
		result.setSuccess(true);
	}
}