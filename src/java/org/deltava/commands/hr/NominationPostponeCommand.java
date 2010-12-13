// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.hr;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.hr.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * Moves any pending nominations from previous Quarters into the current Quarter.
 * @author Luke
 * @version 3.4
 * @since 3.4
 */

public class NominationPostponeCommand extends AbstractCommand {

	/**
	 * Executes the Command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAOs
			GetPilot pdao = new GetPilot(con);
			GetNominations ndao = new GetNominations(con);
			SetNomination nwdao = new SetNomination(con);
			
			// Load the nominations
			Quarter currentQ = new Quarter();
			Collection<Pilot> pilots = new ArrayList<Pilot>();
			Collection<Nomination> noms = ndao.getByStatus(Nomination.Status.PENDING, null);
			for (Iterator<Nomination> i = noms.iterator(); i.hasNext(); ) {
				Nomination nom = i.next();

				// If it's a previous quarter, move to current quarter
				Quarter q = new Quarter(nom.getCreatedOn());
				if (q.compareTo(currentQ) < 0) {
					nwdao.adjustToCurrentQuarter(nom);
					pilots.add(pdao.get(nom.getID()));
				}
			}
			
			// Save pilots adjusted
			ctx.setAttribute("pilots", pilots, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save status variable
		ctx.setAttribute("isPostponed", Boolean.TRUE, REQUEST);

		// Forward back to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/hr/scNominateUpdate.jsp");
		result.setSuccess(true);
	}
}