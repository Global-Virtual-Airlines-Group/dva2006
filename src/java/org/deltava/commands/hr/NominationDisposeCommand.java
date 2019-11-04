// Copyright 2010, 2011, 2012, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.hr;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.hr.Nomination;
import org.deltava.beans.hr.Nomination.Status;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.NominationAccessControl;

import org.deltava.util.cache.CacheManager;

/**
 * A Web Site Command to approve or reject Senior Captain nominations.
 * @author Luke
 * @version 9.0
 * @since 3.3
 */

public class NominationDisposeCommand extends AbstractCommand {

	/**
	 * Executes the Command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		boolean isApproved = Boolean.valueOf(String.valueOf(ctx.getCmdParameter(Command.OPERATION, Boolean.FALSE))).booleanValue();
		try {
			Connection con = ctx.getConnection();
			
			// Load the nomination
			GetNominations ndao = new GetNominations(con);
			Nomination n = ndao.get(ctx.getID());
			if (n == null)
				throw notFoundException("Cannot find Nomination - " + ctx.getID());
			
			// Load the pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(n.getID());
			boolean isCaptain = (p.getRank() == Rank.C);
			boolean isSC = (p.getRank() == Rank.SC);
			ctx.setAttribute("pilot", p, REQUEST);
			
			// Check our access
			NominationAccessControl access = new NominationAccessControl(ctx, n);
			access.setPilot(p);
			access.validate();
			if (!access.getCanUpdate())
				throw securityException("Cannot update Nomination");
			
			// Set status
			n.setStatus(isApproved ? Status.APPROVED : Status.REJECTED);
			StatusUpdate upd = new StatusUpdate(p.getID(), isApproved ? UpdateType.SR_CAPTAIN : UpdateType.COMMENT);
			upd.setAuthorID(ctx.getUser().getID());
			if (isApproved && isCaptain)
				upd.setDescription("Promoted to " + Rank.SC);
			else if (isApproved && !isSC)
				upd.setDescription("Promoted to " + Rank.SC + " upon Captain eligibility");
			else
				upd.setDescription("Nomination to " + Rank.SC + " rejected");
			
			// Start transaction
			ctx.startTX();
			
			// Save the nomination
			SetNomination nwdao = new SetNomination(con);
			nwdao.update(n);
			
			// If we've approved, update rank
			if (isApproved && isCaptain) {
				p.setRank(Rank.SC);
				SetPilot pwdao = new SetPilot(con);
				pwdao.write(p);
			}
			
			// Update Status
			if (!isSC) {
				SetStatusUpdate suwdao = new SetStatusUpdate(con);
				suwdao.write(upd);
			}
			
			// Clear caches and commit
			GetPilotRecognition.invalidate(null);
			CacheManager.invalidate("Pilots", p.cacheKey());
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save status variables
		ctx.setAttribute("isDisposed", Boolean.TRUE, REQUEST);
		ctx.setAttribute("isApproved", Boolean.valueOf(isApproved), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/hr/scNominateUpdate.jsp");
		result.setSuccess(true);
	}
}