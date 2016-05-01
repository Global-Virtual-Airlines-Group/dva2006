// Copyright 2005, 2009, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.cooler.*;
import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display a user's Water Cooler Threads.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class MyThreadsCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the default airline
		AirlineInformation airline = SystemData.getApp(SystemData.get("airline.code"));

		// Get/set start/count parameters
		ViewContext<MessageThread> vc = initView(ctx, MessageThread.class);
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Pilot's airline
			GetUserData uddao = new GetUserData(con);
			UserData usrData = uddao.get(ctx.getUser().getID());
			if (usrData != null)
				airline = SystemData.getApp(usrData.getAirlineCode());

			// Get the channel DAO and the list of channels
			GetCoolerChannels dao = new GetCoolerChannels(con);
			ctx.setAttribute("channels", dao.getChannels(airline, ctx.getRoles()), REQUEST);

			// Get the Message Thread DAO
			GetCoolerThreads dao2 = new GetCoolerThreads(con);
			dao2.setQueryStart(vc.getStart());
			dao2.setQueryMax(Math.round(vc.getCount() * 1.25f));

			// Initialize the access controller
			CoolerThreadAccessControl ac = new CoolerThreadAccessControl(ctx);

			// Get either by channel or all; now filter by role
			Collection<Integer> pilotIDs = new HashSet<Integer>();
			List<MessageThread> threads = dao2.getByAuthor(ctx.getUser().getID(), ctx.getUser().getShowSSThreads());
			for (Iterator<MessageThread> i = threads.iterator(); i.hasNext();) {
				MessageThread thread = i.next();

				// Get this thread's channel and see if we can read it
				Channel c = dao.get(thread.getChannel());
				ac.updateContext(thread, c);
				ac.validate();

				// If we cannot read the thread, remove it from the results, otherwise load the pilot profiles
				if (ac.getCanRead()) {
					pilotIDs.add(Integer.valueOf(thread.getAuthorID()));
					pilotIDs.add(Integer.valueOf(thread.getLastUpdateID()));
				} else
					i.remove();
			}

			// Remove excess entires
			if (threads.size() > vc.getCount())
				threads.subList(vc.getCount(), threads.size()).clear();

			// Get the location of all the Pilots
			UserDataMap udm = uddao.get(pilotIDs);
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("userData", udm, REQUEST);
			ctx.setAttribute("pilots", pdao.get(udm), REQUEST);

			// Save in the view context
			vc.setResults(threads);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set channel/command name attributes
		ctx.setAttribute("viewCmd", getID(), REQUEST);
		ctx.setAttribute("channelName", "My Discussion Threads", REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/cooler/threadList.jsp");
		result.setSuccess(true);
	}
}