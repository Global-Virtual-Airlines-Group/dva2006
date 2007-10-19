// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;

import org.deltava.commands.*;

import org.deltava.dao.*;
import org.deltava.dao.file.GetProcData;

import org.deltava.util.IPCUtils;
import org.deltava.util.system.SystemData;

import org.gvagroup.acars.ACARSAdminInfo;
import org.gvagroup.common.SharedData;

/**
 * A Web Site Command to display the home page.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class HomeCommand extends AbstractCommand {

	private static final Logger log = Logger.getLogger(HomeCommand.class);
	private final Random RND = new Random();

	// Dynamic content codes
	private static final int NEXT_EVENT = 0;
	private static final int NEW_HIRES = 1;
	private static final int CENTURY_CLUB = 2;
	private static final int PROMOTIONS = 3;
	private static final int ACARS_USERS = 4;

	// Dynamic content choices
	private static final int[] DYN_CHOICES = { NEXT_EVENT, NEW_HIRES, CENTURY_CLUB, PROMOTIONS, ACARS_USERS };

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public void execute(CommandContext ctx) throws CommandException {

		// Get Command result
		CommandResult result = ctx.getResult();
		String myHost = SystemData.get("airline.url");
		
		// Check that the hostname is correct
		if (!ctx.getRequest().getServerName().equals(myHost)) {
			result.setType(CommandResult.REDIRECT);
			result.setURL("http://" + myHost + "/");
			result.setSuccess(true);
			return;
		}
		
		// Get OS uptime if running on Linux
		if ("Linux".equals(System.getProperty("os.name"))) {
			GetProcData pdao = new GetProcData();
			try {
				int runTime = pdao.getUptime();
				ctx.setAttribute("runTimeDays", Integer.valueOf(runTime / 86400), REQUEST);
				runTime %= 86400;
				ctx.setAttribute("runTimeHours", Integer.valueOf(runTime / 3600), REQUEST);
				runTime %= 3600;
				ctx.setAttribute("runTimeMinutes", Integer.valueOf(runTime / 60), REQUEST);
			} catch (DAOException de) {
				log.error(de.getMessage());
			}
		}
		
		// Build a list of choices
		List<Integer> cList = new ArrayList<Integer>(DYN_CHOICES.length);
		for (int x = 0; x < DYN_CHOICES.length; x++)
			cList.add(Integer.valueOf(DYN_CHOICES[x]));

		// Check if ACARS has anyone connected
		ACARSAdminInfo acarsPool = (ACARSAdminInfo) SharedData.get(SharedData.ACARS_POOL);
		if ((acarsPool == null) || acarsPool.isEmpty()) {
			ctx.setAttribute("noACARSUsers", Boolean.TRUE, REQUEST);
			cList.remove(Integer.valueOf(ACARS_USERS));
		}
		
		try {
			Connection con = ctx.getConnection();

			// Get the system news and save in the request
			GetNews nwdao = new GetNews(con);
			nwdao.setQueryMax(5);
			ctx.setAttribute("latestNews", nwdao.getNews(), REQUEST);

			// Get the HTTP statistics and save in the request
			GetSystemData sysdao = new GetSystemData(con);
			ctx.setAttribute("httpStats", sysdao.getHTTPTotals(), REQUEST);
			
			// Check if we have future events
			GetEvent evdao = new GetEvent(con);
			if (!evdao.hasFutureEvents()) {
				ctx.setAttribute("noUpcomingEvents", Boolean.TRUE, REQUEST);
				cList.remove(Integer.valueOf(NEXT_EVENT));
			}

			// Get latest water cooler data
			GetStatistics stdao = new GetStatistics(con);
			ctx.setAttribute("coolerStats", new Integer(stdao.getCoolerStatistics(1)), REQUEST);

			// Get new/active NOTAMs since last login
			if (ctx.isAuthenticated() && (ctx.getUser().getLastLogin() != null)) {
				Person usr = ctx.getUser();
				Collection notams = nwdao.getActiveNOTAMs();
				for (Iterator i = notams.iterator(); i.hasNext();) {
					Notice ntm = (Notice) i.next();
					if (ntm.getDate().before(usr.getLastLogin()))
						i.remove();
				}

				ctx.setAttribute("notams", notams, REQUEST);
			}
			
			// Calculate the contnt type
			int ofs = RND.nextInt(cList.size());
			Integer contentType = cList.get(ofs);
			
			// Figure out dynamic content
			switch (contentType.intValue()) {
				// Next Event
				case NEXT_EVENT:
					evdao.setQueryMax(5);					
					ctx.setAttribute("futureEvents", evdao.getFutureEvents(), REQUEST);
					break;

				// Online ACARS Users
				case ACARS_USERS:
					ctx.setAttribute("acarsPool", IPCUtils.deserialize(acarsPool.getPoolInfo(false)), REQUEST);
					break;

				// Latest Hires
				case NEW_HIRES:
					GetPilot daoP = new GetPilot(con);
					daoP.setQueryMax(10);
					ctx.setAttribute("latestPilots", daoP.getNewestPilots(), REQUEST);
					break;

				// Newest Century Club members
				case CENTURY_CLUB:
				case PROMOTIONS:
					GetStatusUpdate sudao = new GetStatusUpdate(con);
					sudao.setQueryMax(5);
					Collection<StatusUpdate> upds = null;
					if (contentType.intValue() == CENTURY_CLUB) {
						upds = sudao.getByType(StatusUpdate.RECOGNITION);
						ctx.setAttribute("centuryClub", upds, REQUEST);
					} else {
						upds = sudao.getByType(StatusUpdate.EXTPROMOTION);
						ctx.setAttribute("promotions", upds, REQUEST);
					}
					
					// Get pilot IDs
					Collection<Integer> IDs = new HashSet<Integer>();
					for (Iterator<StatusUpdate> i = upds.iterator(); i.hasNext(); ) {
						StatusUpdate upd = i.next();
						IDs.add(new Integer(upd.getID()));
					}
					
					// Load pilots
					GetPilot pdao = new GetPilot(con);
					ctx.setAttribute("updPilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
					break;
			}
			
			// Save the content type
			ctx.setAttribute("dynContentType", contentType, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Redirect to the home page
		result.setURL("/jsp/main/home.jsp");
		result.setSuccess(true);
	}
}