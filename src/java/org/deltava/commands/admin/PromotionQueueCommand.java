// Copyright 2005, 2007, 2008, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 8.6
 * @since 1.0
 */

public class PromotionQueueCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the equipment type
		ViewContext<Pilot> vc = initView(ctx, Pilot.class);
		String eqType = ctx.isUserInRole("HR") ? null : ctx.getUser().getEquipmentType();
		try {
			Connection con = ctx.getConnection();
			
			// Load the queue
			GetPilotRecognition dao = new GetPilotRecognition(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			Collection<Integer> IDs = dao.getPromotionQueue(eqType);
			Map<Integer, Pilot> queue = dao.getByID(IDs, "PILOTS");
			
			// Load PIREP totals
			GetFlightReports prdao = new GetFlightReports(con);
			prdao.getOnlineTotals(queue, SystemData.get("airline.db"));
			vc.setResults(queue.values());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Check pilot access
		Map<Integer, AccessControl> accessMap = new HashMap<Integer, AccessControl>();
		for (Pilot p : vc.getResults()) {
			PilotAccessControl access = new PilotAccessControl(ctx, p);
			access.validate();
			accessMap.put(Integer.valueOf(p.getID()), access);
		}

		// Save pilot access
		ctx.setAttribute("accessMap", accessMap, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/roster/promotionQueue.jsp");
		result.setSuccess(true);
	}
}