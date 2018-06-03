// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PilotAccessControl;

/**
 * A Web Site Command to mark a Pilot as "forgotten" for GDPR purposes. 
 * @author Luke
 * @version 8.3
 * @since 8.3
 */

public class ForgetUserCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(ctx.getID());
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());
			else if (p.getIsForgotten())
				throw forgottenException();
			
			// Check our access
			PilotAccessControl ac = new PilotAccessControl(ctx, p);
			ac.validate();
			if (!ac.getCanEdit())
				throw securityException("Not Authorized");
			
			// Status updats
			Collection<StatusUpdate> upds = new ArrayList<StatusUpdate>();
			
			// Write status update
			StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.STATUS_CHANGE);
			upd.setAuthorID(ctx.getUser().getID());
			upd.setDescription("User Forgotten");
			upds.add(upd);
			
			// Forget the user
			p.setIsForgotten(true);
			p.setEmailInvalid(true);
			p.setEmailAccess(Person.HIDE_EMAIL);
			if ((p.getStatus() == Pilot.ACTIVE) || (p.getStatus() == Pilot.ON_LEAVE)) {
				p.setStatus(Pilot.RETIRED);
				upd = new StatusUpdate(p.getID(), StatusUpdate.STATUS_CHANGE);
				upd.setDescription("Pilot Retired");
				upds.add(upd);
			}
			
			// Save the user and commit
			ctx.startTX();
			SetPilot pwdao = new SetPilot(con);
			SetStatusUpdate suwdao = new SetStatusUpdate(con);
			pwdao.write(p);
			suwdao.write(upds);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("profile", null, ctx.getID());
		result.setSuccess(true);
	}
}