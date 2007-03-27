// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.ts2;

import java.sql.Connection;

import org.deltava.beans.ts2.Channel;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to delete a TeamSpeak 2 channel.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ChannelDeleteCommand extends AbstractCommand {

	/**
	 * Executes the Command.
	 * @param ctx the Command Context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the channel
			GetTS2Data dao = new GetTS2Data(con);
			Channel c = dao.getChannel(ctx.getID());
			if (c == null)
				throw notFoundException("Invalid Channel - " + ctx.getID());
			
			// Start a transaction
			ctx.startTX();
			
			// Get the write DAO and delete the channel
			SetTS2Data wdao = new SetTS2Data(con);
			wdao.delete(c);
			
			// Check for a default channel
			wdao.setDefault(c.getServerID());
			
			// Commit the transaction
			ctx.commitTX();
			
			// Save the channel in the request
			ctx.setAttribute("channel", c, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/ts2/ts2Update.jsp");
		result.setSuccess(true);
	}
}