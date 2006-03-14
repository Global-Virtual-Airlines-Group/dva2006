// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.util.*;
import java.net.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.acars.ACARSAdminInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display the home page.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class HomeCommand extends AbstractCommand {

	private static final Logger log = Logger.getLogger(HomeCommand.class);
	
	private static final Random RND = new Random();

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
		
		// Build a list of choices
		List<Integer> cList = new ArrayList<Integer>();
		for (int x = 0; x < DYN_CHOICES.length; x++)
			cList.add(new Integer(DYN_CHOICES[x]));

		// Check if ACARS has anyone connected
		ACARSAdminInfo acarsPool = (ACARSAdminInfo) SystemData.getObject(SystemData.ACARS_POOL);
		if ((acarsPool == null) || acarsPool.isEmpty()) {
			ctx.setAttribute("noACARSUsers", Boolean.TRUE, REQUEST);
			cList.remove(new Integer(ACARS_USERS));
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
				cList.remove(new Integer(NEXT_EVENT));
			}

			// Get latest water cooler data
			GetStatistics stdao = new GetStatistics(con);
			ctx.setAttribute("coolerStats", new Integer(stdao.getCoolerStatistics(1)), REQUEST);

			// Get new/active NOTAMs since last login
			if (ctx.isAuthenticated()) {
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
					ctx.setAttribute("acarsPool", acarsPool.getPoolInfo(), REQUEST);
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
					if (contentType.intValue() == CENTURY_CLUB) {
						ctx.setAttribute("centuryClub", sudao.getByType(StatusUpdate.RECOGNITION), REQUEST);
					} else {
						ctx.setAttribute("promotions", sudao.getByType(StatusUpdate.EXTPROMOTION), REQUEST);
					}

					break;
			}
			
			// Save the content type
			ctx.setAttribute("dynContentType", contentType, REQUEST);
			
			// Determine where we are referring from
			String referer = ctx.getRequest().getHeader("Referer");
			if (!StringUtils.isEmpty(referer)) {
				try {
					URL url = new URL(referer);
					
					// Log the referring host
					if (!myHost.equalsIgnoreCase(url.getHost())) {
						SetSystemData wdao = new SetSystemData(con);
						wdao.logReferer(referer, url.getHost());
					}
				} catch (MalformedURLException mue) {
					log.warn("Invalid HTTP referer - " + referer);
				}
			}
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