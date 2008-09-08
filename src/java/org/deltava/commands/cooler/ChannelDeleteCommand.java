// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.sql.Connection;

import org.deltava.beans.cooler.Channel;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerChannelAccessControl;

/**
 * A Web Site Command to delete a Water Cooler channel.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class ChannelDeleteCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the channel
			GetCoolerChannels ccdao = new GetCoolerChannels(con);
			Channel c = ccdao.get(ctx.getParameter("id"));
			if (c == null)
				throw notFoundException("Invalid Channel - " + ctx.getParameter("id"));
			
			// Check our access
			CoolerChannelAccessControl access = new CoolerChannelAccessControl(ctx, c);
			access.validate();
			if (!access.getCanDelete())
				throw securityException("Cannot delete Chanel " + c.getName());
			
			// Delete the channel
			SetCoolerChannel cwdao = new SetCoolerChannel(con);
			cwdao.delete(c);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the channel list
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REDIRECT);
		result.setURL("channeladmin.do");
		result.setSuccess(true);
	}
}