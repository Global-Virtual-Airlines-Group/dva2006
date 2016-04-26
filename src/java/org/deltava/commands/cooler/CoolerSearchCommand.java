// Copyright 2005, 2007, 2008, 2009, 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;
import java.util.concurrent.*;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.deltava.beans.*;
import org.deltava.beans.cooler.*;
import org.deltava.commands.*;

import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to search the Water Cooler.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class CoolerSearchCommand extends AbstractViewCommand {
	
	private static final Collection<String> DAY_OPTS = Arrays.asList("15", "30", "60", "90", "180", "365", "720");
	
	private static final Semaphore _usrLock = new Semaphore(5, true);
	private static final Semaphore _anonLock = new Semaphore(1, false);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Save days option
		ctx.setAttribute("days", DAY_OPTS, REQUEST);
		
		// Get days to search
		int daysBack = StringUtils.parse(ctx.getParameter("daysBack"), 180);
		ctx.setAttribute("daysBack", Integer.valueOf(daysBack), REQUEST);
		
		// Get the command result and view context
		CommandResult result = ctx.getResult();
		ViewContext vc = initView(ctx);
		Semaphore s = ctx.isAuthenticated() ? _usrLock : _anonLock; boolean hasLock = false;
		try {
			Connection con = ctx.getConnection();

			// Get the channel names
			GetCoolerChannels cdao = new GetCoolerChannels(con);
			ctx.setAttribute("channels", cdao.getChannels(SystemData.getApp(SystemData.get("airline.code")), ctx.getRoles()), REQUEST);

			// Check if we're doing a GET, and redirect to the results JSP
			if (ctx.getParameter("searchStr") == null) {
				ctx.release();
				result.setURL("/jsp/cooler/threadSearch.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Check if we can get the lock
			TaskTimer tt = new TaskTimer();
			try {
				if (!s.tryAcquire(200, TimeUnit.MILLISECONDS)) {
					ctx.release();
					result.setURL("/jsp/error/tooBusy.jsp");
					return;
				}
				
				hasLock = true;
			} catch (InterruptedException ie) {
				throw new CommandException(ie);
			}
			
			// Get last update date
			LocalDateTime lud = LocalDateTime.now().minusDays(daysBack);

			// Build the search criteria
			SearchCriteria criteria = new SearchCriteria(ctx.getParameter("searchStr"));
			criteria.setChannel(ctx.getParameter("channel"));
			criteria.setAuthorName(ctx.getParameter("pilotName"));
			criteria.setSearchSubject(Boolean.valueOf(ctx.getParameter("checkSubject")).booleanValue());
			criteria.setSearchNameFragment(Boolean.valueOf(ctx.getParameter("nameMatch")).booleanValue());
			criteria.setMinimumDate(lud.toInstant(ZoneOffset.UTC));
			
			// Get the DAO and search
			GetCoolerThreads dao = new GetCoolerThreads(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(Math.round(vc.getCount() * 1.25f));
			Collection<MessageThread> 	threads = dao.search(criteria);
			
			// Filter out the threads based on our access
			Collection<Integer> pilotIDs = new HashSet<Integer>();
			CoolerThreadAccessControl access = new CoolerThreadAccessControl(ctx);
			for (Iterator<MessageThread> i = threads.iterator(); i.hasNext();) {
				MessageThread mt = i.next();
				Channel c = cdao.get(mt.getChannel());
				access.updateContext(mt, c);
				access.validate();

				// Remove the thread if we cannot access it
				if (access.getCanRead()) {
					pilotIDs.add(Integer.valueOf(mt.getAuthorID()));
					pilotIDs.add(Integer.valueOf(mt.getLastUpdateID()));
				} else
					i.remove();
			}

			// Get the location of all the Pilots
			GetUserData usrdao = new GetUserData(con);
			UserDataMap udm = usrdao.get(pilotIDs);
			ctx.setAttribute("userData", udm, REQUEST);

			// Get the authors for the last post in each channel
			Map<Integer, Person> authors = new HashMap<Integer, Person>();
			GetPilot pdao = new GetPilot(con);
			GetApplicant adao = new GetApplicant(con);
			authors.putAll(pdao.get(udm));
			authors.putAll(adao.get(udm));

			// Save the threads in the request
			vc.setResults(threads);
			ctx.setAttribute("pilots", authors, REQUEST);
			ctx.setAttribute("searchTime", Long.valueOf(tt.stop()), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			if (hasLock)
				s.release();

			ctx.release();
		}

		// Set search attribute
		ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/cooler/threadSearch.jsp");
		result.setSuccess(true);
	}
}