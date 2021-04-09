// Copyright 2010, 2016, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.hr;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.hr.Nomination;
import org.deltava.beans.hr.Nomination.Status;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.NominationAccessControl;

/**
 * A Web Site Command to reject all pending Senior Captain nominations.
 * @author Luke
 * @version 10.0
 * @since 3.3
 */

public class NominationPurgeCommand extends AbstractCommand {

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
			GetNominations ndao = new GetNominations(con);
			SetStatusUpdate sudao = new SetStatusUpdate(con);
			SetNomination nwdao = new SetNomination(con);
			
			// Load the nominations
			Collection<Nomination> noms = ndao.getByStatus(Status.PENDING, null);
			for (Iterator<Nomination> i = noms.iterator(); i.hasNext(); ) {
				Nomination nom = i.next();
				
				// Check our access
				NominationAccessControl access = new NominationAccessControl(ctx, nom);
				access.validate();
				if (!access.getCanDispose()) {
					i.remove();
					continue;
				}
				
				// Set status
				nom.setStatus(Status.REJECTED);
				StatusUpdate upd = new StatusUpdate(nom.getID(), UpdateType.COMMENT);
				upd.setAuthorID(ctx.getUser().getID());
				upd.setDescription("Nomination to " + Rank.SC + " rejected");
				
				// Start transaction
				ctx.startTX();
				
				// Write the updates
				sudao.write(upd, ctx.getDB());
				nwdao.update(nom);
				
				// Commit
				ctx.commitTX();
			}
			
			// Load the pilots we have updated
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(noms, "PILOTS").values(), REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save status variable
		ctx.setAttribute("isPurged", Boolean.TRUE, REQUEST);
		
		// Forward back to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/hr/scNominateUpdate.jsp");
		result.setSuccess(true);
	}
}