// Copyright 2005, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.security.command.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display Pilots eligible for Promotion.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class PromotionQueueCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the equipment type
		String eqType = ctx.isUserInRole("HR") ? null : ctx.getUser().getEquipmentType();
		Collection<Pilot> pilots = null;
		try {
			Connection con = ctx.getConnection();
			
			// Load the queue
			GetPilotRecognition dao = new GetPilotRecognition(con);
			Collection<Integer> IDs = dao.getPromotionQueue(eqType);
			Map<Integer, Pilot> queue = dao.getByID(IDs, "PILOTS");
			
			// Load PIREP totals
			GetFlightReports prdao = new GetFlightReports(con);
			prdao.getOnlineTotals(queue, SystemData.get("airline.db"));
			pilots = queue.values();
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
		ctx.setAttribute("queue", pilots, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/roster/promotionQueue.jsp");
		result.setSuccess(true);
	}
}