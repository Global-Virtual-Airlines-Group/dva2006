package org.deltava.commands.cooler;

import java.sql.Connection;
import java.util.*;

import org.deltava.commands.*;

import org.deltava.beans.cooler.Message;
import org.deltava.beans.system.*;

import org.deltava.dao.GetCoolerChannels;
import org.deltava.dao.GetUserData;
import org.deltava.dao.GetPilot;
import org.deltava.dao.DAOException;

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
			Collection channels = dao.getChannels(airline, ctx.getRoles());
			ctx.setAttribute("channels", channels, REQUEST);
			
			// Get the last posts in each of the returned channels
			Map posts = dao.getLastPosts(channels);
			ctx.setAttribute("posts", posts, REQUEST);
			
			// Build a set of pilot IDs from the last posts
			Set pilotIDs = new HashSet();
			for (Iterator i = posts.values().iterator(); i.hasNext(); ) {
			    Message msg = (Message) i.next();
			    pilotIDs.add(new Integer(msg.getAuthorID()));
			}
			
			// Get the location of all the Pilots
			GetUserData usrdao = new GetUserData(con);
			UserDataMap udm = usrdao.get(pilotIDs);
			ctx.setAttribute("userData", udm, REQUEST);

			// Get the authors for the last post in each channel
			Map authors = new HashMap();
			GetPilot pdao = new GetPilot(con);
			for (Iterator i = udm.getTableNames().iterator(); i.hasNext(); ) {
				String tableName = (String) i.next();
				authors.putAll(pdao.getByID(udm.getByTable(tableName), tableName));
			}

			// Save the authors for the last post in each channel
			ctx.setAttribute("authors", authors, REQUEST);
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