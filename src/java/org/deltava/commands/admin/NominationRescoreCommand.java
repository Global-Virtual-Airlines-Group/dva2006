// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.hr.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to rescore all Senior Captain nominations.
 * @author Luke
 * @version 3.3
 * @since 3.3
 */

public class NominationRescoreCommand extends AbstractCommand {

	/**
	 * Executes the Command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Load nominations
			GetNominations ndao = new GetNominations(con);
			Collection<Nomination> noms = ndao.getByStatus(Nomination.Status.PENDING, new Quarter());
			
			// Start transaction
			ctx.startTX();
			
			// Update score
			Collection<Nomination> updated = new ArrayList<Nomination>();
			GetPilot pdao = new GetPilot(con);
			SetNomination nwdao = new SetNomination(con);
			for (Iterator<Nomination> i = noms.iterator(); i.hasNext(); ) {
				Nomination n = i.next();
				
				// Get the new score
				NominationScoreHelper helper = new NominationScoreHelper(n);
				helper.addAuthors(pdao.getByID(helper.getAuthorIDs(), "PILOTS").values());
				int newScore = helper.getScore();
				if (newScore != n.getScore()) {
					updated.add(n);
					n.setScore(newScore);
					nwdao.update(n);
				}
			}
			
			// Commit
			ctx.commitTX();
			
			// Load the pilots and updated nominations
			ctx.setAttribute("noms", updated, REQUEST);
			ctx.setAttribute("pilots", pdao.getByID(updated, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status variable
		ctx.setAttribute("isRescored", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/admin/scNominateUpdate.jsp");
		result.setSuccess(true);
	}
}