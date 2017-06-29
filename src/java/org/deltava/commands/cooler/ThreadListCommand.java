// Copyright 2005, 2006, 2007, 2012, 2014, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.cooler.*;
import org.deltava.beans.system.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerChannelAccessControl;
import org.deltava.security.command.CoolerThreadAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A web site command to display Message Threads in a Water Cooler channel.
 * @author Luke
 * @version 7.5
 * @since 1.0
 */

public class ThreadListCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the user for the channel list
		Pilot p = ctx.getUser();
		AirlineInformation airline = SystemData.getApp(SystemData.get("airline.code"));

		// Check if we want to display image threads
		boolean showImgThreads = ctx.isAuthenticated() ? p.getShowSSThreads() : true;

		// Get/set start/count parameters and channel name
		ViewContext<MessageThread> vc = initView(ctx, MessageThread.class);
		String cName = (String) ctx.getCmdParameter(ID, "General Discussion");
		try {
			Connection con = ctx.getConnection();

			// Get the Pilot's airline
			GetUserData uddao = new GetUserData(con);
			if (p != null) {
				UserData usrData = uddao.get(p.getID());
				if (usrData != null)
					airline = SystemData.getApp(usrData.getAirlineCode());
			}

			// Get the channel DAO and the list of channels
			GetCoolerChannels dao = new GetCoolerChannels(con);
			Channel ch = dao.get(cName);
			ctx.setAttribute("channel", ch, REQUEST);
			ctx.setAttribute("channels", dao.getChannels(airline, ctx.getRoles()), REQUEST);

			// Initialize the channel access controller
			CoolerChannelAccessControl cAccess = new CoolerChannelAccessControl(ctx, ch);
			cAccess.validate();
			ctx.setAttribute("channelAccess", cAccess, REQUEST);

			// Get the Message Threads for this channel - add by 10% for filtering
			GetCoolerThreads tdao = new GetCoolerThreads(con);
			tdao.setQueryStart(vc.getStart());
			tdao.setQueryMax(vc.getCount() * 2);

			// Figure out what message threads to display
			List<MessageThread> threads = null;
			if (ch != null)
				threads = tdao.getByChannel(cName, showImgThreads);
			else if (Channel.SHOTS.getName().equals(cName)) {
				ctx.setAttribute("channelName", Channel.SHOTS.getComboName(), REQUEST);
				ctx.setAttribute("channel", Channel.SHOTS, REQUEST);
				threads = tdao.getScreenShots();
			} else {
				ctx.setAttribute("channelName", Channel.ALL.getComboName(), REQUEST);
				threads = tdao.getByChannel(null, showImgThreads);
			}
			
			// Initialize the access controller and the set to store pilot IDs
			Collection<Integer> pilotIDs = new HashSet<Integer>();
			CoolerThreadAccessControl ac = new CoolerThreadAccessControl(ctx);
			
			// Validate our access to each thread
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
			
			// Add the unread marks
			if ((p != null) && p.getShowNewPosts()) {
				GetCoolerLastRead lrdao = new GetCoolerLastRead(con);
				Map<Integer, Instant> lr = lrdao.getLastRead(threads, p.getID());
				Map<Integer, Integer> lrIDs = new HashMap<Integer, Integer>();
				for (MessageThread thread : threads) {
					Integer id = Integer.valueOf(thread.getID());
					Instant dt = lr.get(id);
					int postID = thread.getNextPostID(dt);
					if (postID < Integer.MAX_VALUE)
						lrIDs.put(id, Integer.valueOf(postID));
				}
				
				ctx.setAttribute("threadViews", lr, REQUEST);
				ctx.setAttribute("threadViewIDs", lrIDs, REQUEST);
			}

			// Get the location of all the Pilots
			UserDataMap udm = uddao.get(pilotIDs);
			ctx.setAttribute("userData", udm, REQUEST);

			// Get the authors for the last post in each channel
			Map<Integer, Person> authors = new HashMap<Integer, Person>();
			GetPilot pdao = new GetPilot(con);
			GetApplicant adao = new GetApplicant(con);
			for (String tableName : udm.getTableNames()) {
				Collection<UserData> udd = udm.getByTable(tableName);
				if (tableName.endsWith("APPLICANTS"))
					authors.putAll(adao.getByID(udd, tableName));
				else
					authors.putAll(pdao.getByID(udd, tableName));
			}

			// Get the pilot IDs in the returned threads
			ctx.setAttribute("pilots", authors, REQUEST);

			// Save in the view context - If threads is still larger than the max, then cut it off
			vc.setResults((threads.size() > vc.getCount()) ? threads.subList(0, vc.getCount()) : threads);
			ctx.setExpiry(10);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/cooler/threadList.jsp");
		result.setSuccess(true);
	}
}