// Copyright 2005, 2006, 2007, 2008, 2009, 2013, 2014, 2015, 2016, 2019, 2020, 2021, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.util.*;
import java.time.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.apache.logging.log4j.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.system.*;

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
 * @version 11.0
 * @since 1.0
 */

public class HomeCommand extends AbstractCommand {

	private static final Logger log = LogManager.getLogger(HomeCommand.class);
	private final Random RND = new Random();
	
	private enum DynContent {
		NEXT_EVENT, NEW_HIRES, CENTURY_CLUB, PROMOTIONS, ACARS_USERS, ACARS_TOLAND, TOURS
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
			Instant now = Instant.now();
			try {
				GetProcData pdao = new GetProcData();
				ctx.setAttribute("runTime", Duration.between(now.minusSeconds(pdao.getUptime()), now), REQUEST);
			} catch (DAOException de) {
				log.error(de.getMessage());
			}
		}
		
		// Build a list of choices
		boolean isSpider = (reqctx.getBrowserType() == BrowserType.SPIDER);
		List<DynContent> cList = new ArrayList<DynContent>(isSpider ? List.of(DynContent.NEW_HIRES) : List.of(DynContent.values()));

		// Check if ACARS has anyone connected
		ACARSAdminInfo<?> acarsPool = (ACARSAdminInfo<?>) SharedData.get(SharedData.ACARS_POOL);
		if ((acarsPool == null) || (acarsPool.size() == 0)) {
			ctx.setAttribute("noACARSUsers", Boolean.TRUE, REQUEST);
			cList.remove(DynContent.ACARS_USERS);
		}
		
		try {
			Connection con = ctx.getConnection();

			// Get the system news and save in the request
			GetPilot pdao = new GetPilot(con);
			GetNews nwdao = new GetNews(con);
			nwdao.setQueryMax(5);
			List<News> latestNews = nwdao.getNews();
			ctx.setAttribute("latestNews", latestNews, REQUEST);
			Collection<Integer> IDs = latestNews.stream().map(News::getAuthorID).collect(Collectors.toSet());
			ctx.setAttribute("authors", pdao.getByID(IDs, "PILOTS"), REQUEST);

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
				Collection<?> notams = nwdao.getActiveNOTAMs().stream().filter(n -> n.getDate().isAfter(usr.getLastLogin())).collect(Collectors.toList());
				ctx.setAttribute("notams", notams, REQUEST);
			}
			
			// Calculate the contnt type
			int ofs = RND.nextInt(cList.size());
			DynContent contentType = cList.get(ofs);
			
			// Figure out dynamic content
			String airlineCode = SystemData.get("airline.code");
			switch (contentType) {
				case NEXT_EVENT:
					evdao.setQueryMax(5);					
					ctx.setAttribute("futureEvents", evdao.getFutureEvents(), REQUEST);
					break;

				case ACARS_USERS:
					if (acarsPool == null) break;
					Collection<ConnectionEntry> poolInfo = IPCUtils.deserialize(acarsPool.getPoolInfo(false));
					poolInfo.removeIf(ce -> (ce.getUserData() == null) || !ce.getUserData().getAirlineCode().equals(airlineCode));
					ctx.setAttribute("acarsPool", poolInfo, REQUEST);
					break;
					
				// Latest takeoffs and landings
				case ACARS_TOLAND:
					GetACARSData afdao = new GetACARSData(con);
					GetACARSTakeoffs todao = new GetACARSTakeoffs(con);
					GetUserData uddao = new GetUserData(con);
					todao.setQueryMax(10);
					
					Map<TakeoffLanding, FlightInfo> toLand = new LinkedHashMap<TakeoffLanding, FlightInfo>();
					for (TakeoffLanding tl : todao.getLatest()) {
						FlightInfo fl = afdao.getInfo(tl.getID());
						if (fl != null) {
							UserData ud = uddao.get(fl.getAuthorID());
							if ((ud != null) && ud.getAirlineCode().equals(airlineCode))
								toLand.put(tl, fl);
						}
					}
					
					ctx.setAttribute("toLand", toLand, REQUEST);
					break;

				case NEW_HIRES:
					pdao.setQueryMax(10);
					ctx.setAttribute("latestPilots", pdao.getNewestPilots(), REQUEST);
					break;

				// Newest Century Club members
				default:
				case CENTURY_CLUB:
				case PROMOTIONS:
				case TOURS:
					GetStatusUpdate sudao = new GetStatusUpdate(con);
					sudao.setQueryMax(10);
					Collection<StatusUpdate> upds = null;
					if (contentType == DynContent.CENTURY_CLUB) {
						upds = sudao.getByType(UpdateType.RECOGNITION);
						ctx.setAttribute("centuryClub", upds, REQUEST);
					} else if (contentType == DynContent.TOURS) {
						upds = sudao.getByType(UpdateType.TOUR);
						ctx.setAttribute("toursCompleted", upds, REQUEST);
					} else {
						upds = sudao.getByType(UpdateType.EXTPROMOTION);
						ctx.setAttribute("promotions", upds, REQUEST);
					}
					
					// Load pilots
					IDs = upds.stream().map(StatusUpdate::getID).collect(Collectors.toSet());
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