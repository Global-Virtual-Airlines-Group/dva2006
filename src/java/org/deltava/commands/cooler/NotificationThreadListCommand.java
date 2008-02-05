// Copyright 2005, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.UserData;
import org.deltava.beans.UserDataMap;
import org.deltava.beans.cooler.*;
import org.deltava.beans.system.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display Water Cooler threads set up for notifications.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class NotificationThreadListCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the user for the channel list and default airline
		Pilot p = (Pilot) ctx.getUser();
		AirlineInformation airline = SystemData.getApp(SystemData.get("airline.code"));

		// Get/set start/count parameters
		ViewContext vc = initView(ctx);
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Pilot's airline
			GetUserData uddao = new GetUserData(con);
			if (p != null) {
				UserData usrData = uddao.get(p.getID());
				if (usrData != null)
					airline = SystemData.getApp(usrData.getAirlineCode());
			}

			// Get the channel DAO and the list of channels
			GetCoolerChannels dao = new GetCoolerChannels(con);
			ctx.setAttribute("channels", dao.getChannels(airline, ctx.getRoles()), REQUEST);

			// Get the Message Thread DAO
			GetCoolerThreads tdao = new GetCoolerThreads(con);
			tdao.setQueryStart(vc.getStart());
			tdao.setQueryMax(vc.getCount());

			// Initialize the access controller
			CoolerThreadAccessControl ac = new CoolerThreadAccessControl(ctx);

			// Get either by channel or all; now filter by role
			Collection<Integer> pilotIDs = new HashSet<Integer>();
			Collection<MessageThread> threads = tdao.getByNotification(ctx.getUser().getID());
			for (Iterator<MessageThread> i = threads.iterator(); i.hasNext();) {
				MessageThread thread = i.next();

				// Get this thread's channel and see if we can read it
				Channel c = dao.get(thread.getChannel());
				ac.updateContext(thread, c);
				ac.validate();

				// If we cannot read the thread, remove it from the results, otherwise load the pilot profiles
				if (ac.getCanRead()) {
					pilotIDs.add(new Integer(thread.getAuthorID()));
					pilotIDs.add(new Integer(thread.getLastUpdateID()));
				} else
					i.remove();
			}

			// Get the authors for the last post in each channel
			UserDataMap udm = uddao.get(pilotIDs);
			GetPilot pdao = new GetPilot(con);
			Map<Integer, Pilot> authors = pdao.get(udm);
			ctx.setAttribute("pilots", authors, REQUEST);
			ctx.setAttribute("userData", udm, REQUEST);

			// Save in the view context
			vc.setResults(threads);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/cooler/notifyThreadList.jsp");
		result.setSuccess(true);
	}
}