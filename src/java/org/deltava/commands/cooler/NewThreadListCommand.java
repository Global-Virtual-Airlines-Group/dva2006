// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
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
 * A Web Site Command to display Water Cooler threads updated since a certain date/time.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class NewThreadListCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the user/airline for the channel list
		Person p = ctx.getUser();
		AirlineInformation airline = SystemData.getApp(SystemData.get("airline.code"));

		// Get the thread view map
		Map threadViews = (Map) ctx.getSession().getAttribute(CommandContext.THREADREAD_ATTR_NAME);
		if (threadViews == null)
			threadViews = new HashMap();

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

			// Get the Message Threads for this channel
			GetCoolerThreads dao2 = new GetCoolerThreads(con);
			dao2.setQueryStart(vc.getStart());
			dao2.setQueryMax(Math.round(vc.getCount() * 1.33f) + threadViews.size());

			// Initialize the access controller and the set to store pilot IDs
			CoolerThreadAccessControl ac = new CoolerThreadAccessControl(ctx);

			// Get either by channel or all; now filter by role
			Set<Integer> pilotIDs = new HashSet<Integer>();
			List<MessageThread> threads = dao2.getSince(p.getLastLogoff(), true);
			for (Iterator<MessageThread> i = threads.iterator(); i.hasNext();) {
				MessageThread thread = i.next();
				Date lastView = (Date) threadViews.get(new Integer(thread.getID()));

				// Get this thread's channel and see if we can read it
				Channel c = dao.get(thread.getChannel());
				ac.updateContext(thread, c);
				ac.validate();

				// If we cannot read the thread, remove it from the results and check if it's still unread
				if (!ac.getCanRead()) {
					i.remove();
				} else if ((lastView != null) && (lastView.after(thread.getLastUpdatedOn())))
					i.remove();
				else {
					pilotIDs.add(new Integer(thread.getAuthorID()));
					pilotIDs.add(new Integer(thread.getLastUpdateID()));
				}
			}

			// Get the location of all the Pilots
			UserDataMap udm = uddao.get(pilotIDs);
			ctx.setAttribute("userData", udm, REQUEST);

			// Get the authors for the last post in each channel
			Map<Integer, Pilot> authors = new HashMap<Integer, Pilot>();
			GetPilot pdao = new GetPilot(con);
			for (Iterator<String> i = udm.getTableNames().iterator(); i.hasNext();) {
				String tableName = i.next();
				authors.putAll(pdao.getByID(udm.getByTable(tableName), tableName));
			}

			// Get the pilot IDs in the returned threads
			ctx.setAttribute("pilots", authors, REQUEST);
			vc.setResults(threads);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set command/channel name attributes
		ctx.setAttribute("viewCmd", getID(), REQUEST);
		ctx.setAttribute("channelName", "New/Updated Discussion Threads", REQUEST);

		// Forward to JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/cooler/threadList.jsp");
		result.setSuccess(true);
	}
}