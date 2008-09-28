// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.sql.Connection;

import org.deltava.beans.cooler.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.*;

/**
 * A Web Site Command to update Water Cooler discussion thread titles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ThreadSubjectEditCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the message thread
			GetCoolerThreads tdao = new GetCoolerThreads(con);
			MessageThread mt = tdao.getThread(ctx.getID());
			if (mt == null)
				throw notFoundException("Invalid Message Thread - " + ctx.getID());
			
			// Get the Cooler Channel
			GetCoolerChannels cdao = new GetCoolerChannels(con);
			Channel c = cdao.get(mt.getChannel());
			if (c == null)
				throw notFoundException("Invalid Channel - " + mt.getChannel());
			
			// Validate our access
			CoolerThreadAccessControl access = new CoolerThreadAccessControl(ctx);
			access.updateContext(mt, c);
			access.validate();
			if (!access.getCanEditTitle())
				throw securityException("Cannot edit Thread title");
			
			// Add a subject changed entry
			ThreadUpdate upd = new ThreadUpdate(mt.getID());
			upd.setAuthorID(ctx.getUser().getID());
			upd.setMessage("Subject changed from \'" + mt.getSubject() + "\'");
			
			// Start a JDBC transaction
			ctx.startTX();

			// Get the write DAO and update the thread
			SetCoolerMessage wdao = new SetCoolerMessage(con);
			wdao.updateSubject(mt.getID(), ctx.getParameter("newTitle"));
			
			// Write the status update
			wdao.write(upd);

			// Commit the changes
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward back to the thread
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("thread", null, ctx.getID());
		result.setSuccess(true);
	}
}