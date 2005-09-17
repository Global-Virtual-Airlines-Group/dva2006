// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;
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

	private static final List SCORES = Arrays
			.asList(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" });

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the user for the channel list
		Person p = ctx.getUser();

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

			// Check user access
			CoolerThreadAccessControl ac = new CoolerThreadAccessControl(ctx);
			ac.updateContext(thread, c);
			ac.validate();
			if (!ac.getCanRead())
				throw securityException("Cannot read Message Thread " + ctx.getID());

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

			// Get the authors and online totals for the last post in each channel
			Map pilots = new HashMap();
			GetPilot pdao = new GetPilot(con);
			GetFlightReports prdao = new GetFlightReports(con);
			for (Iterator i = udm.getTableNames().iterator(); i.hasNext();) {
				String dbTableName = (String) i.next();
				StringTokenizer tkns = new StringTokenizer(dbTableName, ".");
				String dbName = tkns.nextToken();
				String tableName = tkns.nextToken();

				// Get the pilots from each table and apply their online totals
				Map pilotSubset = pdao.getByID(udm.getByTable(dbTableName), dbTableName);
				if ("PILOTS".equals(tableName))
					prdao.getOnlineTotals(pilotSubset, dbName);

				pilots.putAll(pilotSubset);
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
				ctx.setAttribute("channel", c, REQUEST);
				ctx.setAttribute("channels", cdao.getChannels(airline, false), REQUEST);
			}

			// Save the thread, pilots and access controller in the request
			ctx.setAttribute("thread", thread, REQUEST);
			ctx.setAttribute("access", ac, REQUEST);
			ctx.setAttribute("pilots", pilots, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save scores choices
		ctx.setAttribute("scores", SCORES, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/cooler/threadShow.jsp");
		result.setSuccess(true);
	}
}