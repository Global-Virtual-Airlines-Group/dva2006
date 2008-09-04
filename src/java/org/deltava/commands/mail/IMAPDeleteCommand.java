// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.mail;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to delete an IMAP mailbox.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class IMAPDeleteCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			// Delete the mailbox configuration
			SetPilotEMail wdao = new SetPilotEMail(ctx.getConnection());
			wdao.delete(ctx.getID());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forwad to the view
		CommandResult result = ctx.getResult();
		result.setURL("imaplist.do");
		result.setType(CommandResult.REDIRECT);
		result.setSuccess(true);
	}
}