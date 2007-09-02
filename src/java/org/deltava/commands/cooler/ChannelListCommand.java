// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.cooler.*;
import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A web site command to display Water Cooler channels.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ChannelListCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the default airline
		AirlineInformation airline = SystemData.getApp(SystemData.get("airline.code").toUpperCase());
		try {
			Connection con = ctx.getConnection();
			
			// Get the channels for the user's role
			GetCoolerChannels dao = new GetCoolerChannels(con);
			List<Channel> channels = dao.getChannels(airline, ctx.getRoles());
			channels.remove(Channel.ALL);
			channels.remove(Channel.SHOTS);
			ctx.setAttribute("channels", channels, REQUEST);
			
			// Get the last posts in each of the returned channels
			Map<Integer, Message> posts = dao.getLastPosts(channels);
			ctx.setAttribute("posts", posts, REQUEST);
			
			// Build a set of pilot IDs from the last posts
			Collection<Integer> pilotIDs = new HashSet<Integer>();
			for (Iterator<Message> i = posts.values().iterator(); i.hasNext(); ) {
			    Message msg = i.next();
			    pilotIDs.add(new Integer(msg.getAuthorID()));
			}
			
			// Get the location of all the Pilots
			GetUserData usrdao = new GetUserData(con);
			UserDataMap udm = usrdao.get(pilotIDs);
			ctx.setAttribute("userData", udm, REQUEST);

			// Get the authors for the last post in each channel
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("authors", pdao.get(udm), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/cooler/channelList.jsp");
		result.setSuccess(true);
		ctx.getCache().setMaxAge(240);
	}
}