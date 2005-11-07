// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.system.EMailConfiguration;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PilotAccessControl;

/**
 * A Web Site Command to create a new IMAP mailbox profile.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class IMAPMalboxCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Pilot
			GetPilotDirectory dao = new GetPilotDirectory(con);
			Pilot usr = dao.get(ctx.getID());
			if (usr == null)
				throw new CommandException("Invalid Pilot - " + ctx.getID());
			
			// Get the Mailbox profile
			EMailConfiguration emailCfg = dao.getEMailInfo(ctx.getID());
			if (emailCfg != null)
				throw new CommandException(usr.getName() + " already has an IMAP mailbox");
			
			// Pre-populate the mailbox
			
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
	}
}