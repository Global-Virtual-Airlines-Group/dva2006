// Copyright 2005, 2006, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.sql.Connection;
import java.util.Collection;

import org.deltava.beans.cooler.Channel;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.commands.*;

import org.deltava.dao.GetCoolerChannels;
import org.deltava.dao.DAOException;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display Water Cooler channels for administrators.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class ChannelAdminCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the pilot's airline
		AirlineInformation airline = SystemData.getApp(SystemData.get("airline.code"));
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the channel list
			GetCoolerChannels dao = new GetCoolerChannels(con);
			Collection<Channel> channels = dao.getChannels(airline, ctx.getRoles());
			channels.remove(Channel.ALL);
			channels.remove(Channel.SHOTS);
			ctx.setAttribute("channels", channels, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/cooler/channelAdmin.jsp");
		result.setSuccess(true);
	}
}