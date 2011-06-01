// Copyright 2006, 2007, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.mvs;

import java.sql.Connection;

import org.deltava.beans.mvs.Channel;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to delete an MVS persistent channel.
 * @author Luke
 * @version 4.0
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
			GetMVSChannel dao = new GetMVSChannel(con);
			Channel c = dao.get(ctx.getID());
			if (c == null)
				throw notFoundException("Invalid Channel - " + ctx.getID());
			
			// Get the write DAO and delete the channel
			SetMVSChannel wdao = new SetMVSChannel(con);
			wdao.delete(c.getID());
			
			// Save the channel in the request
			ctx.setAttribute("channel", c, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/mvs/channelUpdate.jsp");
		result.setSuccess(true);
	}
}