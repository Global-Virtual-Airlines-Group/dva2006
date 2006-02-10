// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;
import java.text.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.cooler.*;
import org.deltava.beans.gallery.Image;
import org.deltava.beans.system.*;
import org.deltava.commands.*;

import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;
import org.deltava.security.command.GalleryAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A web site command for viewing Water Cooler discussion threads.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ThreadCommand extends AbstractCommand {

	private static final List<String> SCORES = Arrays.asList(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" });
	private static final List<String> COLORS = Arrays.asList(new String[] {"blue", "black", "green", "red", "purple", "grey", 
			"brown", "orange", "pink", "yellow"});

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the user for the channel list
		Person p = ctx.getUser();
		
		// Determine if we are editing the last post
		boolean doEdit = "edit".equals(ctx.getCmdParameter(OPERATION, null));

		// Get the default airline
		AirlineInformation airline = SystemData.getApp(SystemData.get("airline.code"));
		try {
			Connection con = ctx.getConnection();

			// Get the Pilot's airline
			GetUserData uddao = new GetUserData(con);
			if (p != null) {
				UserData usrData = uddao.get(p.getID());
				if (usrData != null)
					airline = SystemData.getApp(usrData.getAirlineCode());
			}

			// Get the Message Thread
			GetCoolerThreads tdao = new GetCoolerThreads(con);
			MessageThread thread = tdao.getThread(ctx.getID());
			if (thread == null)
				throw new CommandException("Unknown Message Thread - " + ctx.getID());

			// Get the channel profile
			GetCoolerChannels cdao = new GetCoolerChannels(con);
			Channel c = cdao.get(thread.getChannel());
			
			// Load the poll options (if any)
			GetCoolerPolls tpdao = new GetCoolerPolls(con);
			thread.addOptions(tpdao.getOptions(thread.getID()));
			thread.addVotes(tpdao.getVotes(thread.getID()));
			if (!thread.getOptions().isEmpty()) {
				int maxVotes = 0;
				for (Iterator<PollOption> i = thread.getOptions().iterator(); i.hasNext(); ) {
					PollOption opt = i.next();
					maxVotes = Math.max(maxVotes, opt.getVotes());
				}
				
				ctx.setAttribute("barColors", COLORS, REQUEST);
				ctx.setAttribute("maxVotes", new Integer(maxVotes), REQUEST);
			}

			// Check user access
			CoolerThreadAccessControl ac = new CoolerThreadAccessControl(ctx);
			ac.updateContext(thread, c);
			ac.validate();
			if (!ac.getCanRead())
				throw securityException("Cannot read Message Thread " + ctx.getID());
			
			// Make sure we can edit the thread and save the last post content
			doEdit &= ac.getCanEdit();
			if (doEdit)
				ctx.setAttribute("lastPost", thread.getLastPost(), REQUEST);

			// If we have an image, load its metadata
			if (thread.getImage() != 0) {
				// Figure out who started the thread, since the image will be in their gallery
				UserData aUsrData = uddao.get(thread.getAuthorID());
				String imgDB = (aUsrData == null) ? SystemData.get("airline.db") : aUsrData.getDB();

				GetGallery irdao = new GetGallery(con);
				Image img = irdao.getImageData(thread.getImage(), imgDB);
				ctx.setAttribute("img", img, REQUEST);
				ctx.setAttribute("imgDB", imgDB, REQUEST);

				// Get our access to the image
				GalleryAccessControl imgAccess = new GalleryAccessControl(ctx, img);
				imgAccess.validate();
				ctx.setAttribute("imgAccess", imgAccess, REQUEST);
			}

			// Get the location of all the Pilots
			uddao.setQueryMax(0);
			UserDataMap udm = uddao.getByThread(thread.getID());
			ctx.setAttribute("userData", udm, REQUEST);

			// Get the authors and online totals for each user
			Map<Integer, Person> users = new HashMap<Integer, Person>();
			GetPilot pdao = new GetPilot(con);
			GetApplicant adao = new GetApplicant(con);
			GetFlightReports prdao = new GetFlightReports(con);
			for (Iterator<String> i = udm.getTableNames().iterator(); i.hasNext();) {
				String dbTableName = i.next();

				// Get the pilots/applicants from each table and apply their online totals
				if (UserDataMap.isPilotTable(dbTableName)) {
					Map<Integer, Pilot> pilots = pdao.getByID(udm.getByTable(dbTableName), dbTableName);
					prdao.getOnlineTotals(pilots, dbTableName);
					users.putAll(pilots);
				} else {
					Map<Integer, Applicant> applicants = adao.getByID(udm.getByTable(dbTableName), dbTableName);
					users.putAll(applicants);
				}
			}
			
			// Get statistics for all posters
			if (ctx.isUserInRole("Moderator")) {
				GetStatistics stdao = new GetStatistics(con);
				ctx.setAttribute("postStats", stdao.getCoolerStatistics(udm.getIDs()), REQUEST);
			}
			
			// Get the thread notifications
			ThreadNotifications nt =  tdao.getNotifications(thread.getID());
			ctx.setAttribute("notify", nt, REQUEST);
			if (ctx.isAuthenticated())
				ctx.setAttribute("doNotify", Boolean.valueOf(nt.contains(ctx.getUser().getID())), REQUEST);

			// Mark the thread as being read
			SetCoolerMessage wdao = new SetCoolerMessage(con);
			wdao.viewThread(thread.getID());

			// Save all channels in the thread for the move combobox
			if (ac.getCanUnlock() || ac.getCanLock()) {
				cdao.setQueryMax(0);
				ctx.setAttribute("channel", c, REQUEST);
				ctx.setAttribute("channels", cdao.getChannels(airline, false), REQUEST);
			}
			
			// Save the sticky date
			if (ac.getCanResync() && (thread.getStickyUntil() != null)) {
				DateFormat df = new SimpleDateFormat(ctx.getUser().getDateFormat());
				ctx.setAttribute("stickyDate", df.format(thread.getStickyUntil()), REQUEST);
			}

			// Save the thread, pilots and access controller in the request
			ctx.setAttribute("thread", thread, REQUEST);
			ctx.setAttribute("access", ac, REQUEST);
			ctx.setAttribute("pilots", users, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save scores choices and if we are editing
		ctx.setAttribute("doEdit", Boolean.valueOf(doEdit), REQUEST);
		ctx.setAttribute("scores", SCORES, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/cooler/threadShow.jsp");
		result.setSuccess(true);
	}
}