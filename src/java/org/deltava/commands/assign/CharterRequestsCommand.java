// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.util.stream.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.assign.CharterRequest;

import org.deltava.comparators.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.RoleUtils;

/**
 * A Web Site Command to list Charter Flight requests.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class CharterRequestsCommand extends AbstractViewCommand {

	private static final List<String> GLOBAL_ROLES = List.of("PIREP", "Operations", "HR");

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the pilot ID
		int pilotID = ctx.getID();
		if (!RoleUtils.hasAccess(ctx.getUser().getRoles(), GLOBAL_ROLES))
			pilotID = ctx.getUser().getID();

		// Get the view context
		ViewContext<CharterRequest> vctx = initView(ctx, CharterRequest.class);
		try {
			Connection con = ctx.getConnection();

			// Get the requests
			GetCharterRequests rqdao = new GetCharterRequests(con);
			rqdao.setQueryStart(vctx.getStart());
			rqdao.setQueryMax(vctx.getCount());
			vctx.setResults((pilotID == 0) ? rqdao.getAll() : rqdao.getByPilot(pilotID));
			
			// Load the pilots
			Collection<Integer> IDs = vctx.getResults().stream().flatMap(cr -> Stream.of(Integer.valueOf(cr.getAuthorID()), Integer.valueOf(cr.getDisposalID()))).filter(id -> id.intValue() != 0).collect(Collectors.toSet());
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
			if (pilotID != 0)
				ctx.setAttribute("pilot", pdao.get(pilotID), REQUEST);
			
			// Load authors
			Collection<Pilot> pilots = new TreeSet<Pilot>(new PilotComparator(PersonComparator.FIRSTNAME));
			pilots.addAll(pdao.getByID(rqdao.getPilotIDs(), "PILOTS").values());
			ctx.setAttribute("authors", pilots, REQUEST);			
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/assign/charterRequests.jsp");
		result.setSuccess(true);
	}
}