// Copyright 2005, 2006, 2007, 2008, 2009, 2013, 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.system.HTTPContextData;

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
 * @version 7.0
 * @since 1.0
 */

public class HomeCommand extends AbstractCommand {

	private static final Logger log = Logger.getLogger(HomeCommand.class);
	private final Random RND = new Random();
	
	private enum DynContent {
		NEXT_EVENT, NEW_HIRES, CENTURY_CLUB, PROMOTIONS, ACARS_USERS, ACARS_TOLAND
	}
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get Command result
		CommandResult result = ctx.getResult();
		String myHost = SystemData.get("airline.url");
		
		// Check that the hostname is correct
		HTTPContextData reqctx = (HTTPContextData) ctx.getRequest().getAttribute(HTTPContext.HTTPCTXT_ATTR_NAME);
		if (!reqctx.isIPv6() && !ctx.getRequest().getServerName().equals(myHost)) {
			result.setType(ResultType.REDIRECT);
			result.setURL(ctx.getRequest().getScheme() + "://" + myHost + "/");
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
		List<DynContent> cList = new ArrayList<DynContent>(Arrays.asList(DynContent.values()));

		// Check if ACARS has anyone connected
		ACARSAdminInfo<?> acarsPool = (ACARSAdminInfo<?>) SharedData.get(SharedData.ACARS_POOL);
		if ((acarsPool == null) || (acarsPool.size() == 0)) {
			ctx.setAttribute("noACARSUsers", Boolean.TRUE, REQUEST);
			cList.remove(DynContent.ACARS_USERS);
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
				cList.remove(DynContent.NEXT_EVENT);
			}

			// Get latest water cooler data
			GetStatistics stdao = new GetStatistics(con);
			ctx.setAttribute("coolerStats", Long.valueOf(stdao.getCoolerStatistics(1)), REQUEST);

			// Get new/active NOTAMs since last login
			if (ctx.isAuthenticated() && (ctx.getUser().getLastLogin() != null)) {
				Person usr = ctx.getUser();
				Collection<?> notams = nwdao.getActiveNOTAMs();
				for (Iterator<?> i = notams.iterator(); i.hasNext();) {
					Notice ntm = (Notice) i.next();
					if (ntm.getDate().isBefore(usr.getLastLogin()))
						i.remove();
				}

				ctx.setAttribute("notams", notams, REQUEST);
			}
			
			// Calculate the contnt type
			int ofs = RND.nextInt(cList.size());
			DynContent contentType = cList.get(ofs);
			
			// Figure out dynamic content
			switch (contentType) {
				case NEXT_EVENT:
					evdao.setQueryMax(5);					
					ctx.setAttribute("futureEvents", evdao.getFutureEvents(), REQUEST);
					break;

				case ACARS_USERS:
					ctx.setAttribute("acarsPool", IPCUtils.deserialize(acarsPool.getPoolInfo(false)), REQUEST);
					break;
					
				// Latest takeoffs and landings
				case ACARS_TOLAND:
					GetACARSData afdao = new GetACARSData(con);
					GetACARSTakeoffs todao = new GetACARSTakeoffs(con);
					todao.setQueryMax(10);
					Map<TakeoffLanding, FlightInfo> toLand = new LinkedHashMap<TakeoffLanding, FlightInfo>();
					for (TakeoffLanding tl : todao.getLatest()) {
						FlightInfo fl = afdao.getInfo(tl.getID());
						if (fl != null)
							toLand.put(tl, fl);
					}
					
					ctx.setAttribute("toLand", toLand, REQUEST);
					break;

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
					if (contentType == DynContent.CENTURY_CLUB) {
						upds = sudao.getByType(StatusUpdate.RECOGNITION);
						ctx.setAttribute("centuryClub", upds, REQUEST);
					} else {
						upds = sudao.getByType(StatusUpdate.EXTPROMOTION);
						ctx.setAttribute("promotions", upds, REQUEST);
					}
					
					// Get pilot IDs
					Collection<Integer> IDs = new HashSet<Integer>();
					for (StatusUpdate upd : upds)
						IDs.add(Integer.valueOf(upd.getID()));
					
					// Load pilots
					GetPilot pdao = new GetPilot(con);
					ctx.setAttribute("updPilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
					break;
			}
			
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