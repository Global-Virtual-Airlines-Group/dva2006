// Copyright 2005, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;
import java.io.IOException;
import java.sql.Connection;

import org.apache.log4j.Logger;

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
 * @version 2.5
 * @since 1.0
 */

public class CoolerSearchCommand extends AbstractViewCommand {
	
	private static final Logger log = Logger.getLogger(CoolerSearchCommand.class);
	private static final Collection<String> DAY_OPTS = Arrays.asList("15", "30", "60", "90", "180", "365", "720");

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Save days option
		ctx.setAttribute("days", DAY_OPTS, REQUEST);
		
		// Get the command result and view context
		CommandResult result = ctx.getResult();
		ViewContext vc = initView(ctx);
		try {
			Connection con = ctx.getConnection();

			// Get the channel names
			GetCoolerChannels cdao = new GetCoolerChannels(con);
			ctx.setAttribute("channels", cdao.getChannels(SystemData.getApp(SystemData.get("airline.code")),
					ctx.getRoles()), REQUEST);

			// Check if we're doing a GET, and redirect to the results JSP
			if (ctx.getParameter("searchStr") == null) {
				ctx.release();
				result.setURL("/jsp/cooler/threadSearch.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Get last update date
			int daysBack = StringUtils.parse(ctx.getParameter("daysBack"), 90);
			Date lud = CalendarUtils.getInstance(null, false, daysBack * -1).getTime();

			// Build the search criteria
			SearchCriteria criteria = new SearchCriteria(ctx.getParameter("searchStr"));
			criteria.setChannel(ctx.getParameter("channel"));
			criteria.setAuthorName(ctx.getParameter("pilotName"));
			criteria.setSearchSubject(Boolean.valueOf(ctx.getParameter("checkSubject")).booleanValue());
			criteria.setSearchNameFragment(Boolean.valueOf(ctx.getParameter("nameMatch")).booleanValue());
			criteria.setMinimumDate(lud);

			// Do the search
			long start = System.currentTimeMillis();
			Collection<SearchResult> results = SearchUtils.search(criteria, vc.getCount());
			ctx.setAttribute("searchTime", new Long(System.currentTimeMillis() - start), REQUEST);
			
			// Load the threads
			Collection<Integer> pilotIDs = new HashSet<Integer>();
			GetCoolerThreads dao = new GetCoolerThreads(con);
			CoolerThreadAccessControl access = new CoolerThreadAccessControl(ctx);
			for (Iterator<SearchResult> i = results.iterator(); i.hasNext(); ) {
				SearchResult sr = i.next();
				MessageThread mt = dao.getThread(sr.getID(), false);
				
				// Check our access
				if (mt != null) {
					Channel c = cdao.get(mt.getChannel());
					access.updateContext(mt, c);
					access.updateContext(mt, c);	
					access.validate();
					
					// Remove the thread if we cannot access it
					if (access.getCanRead()) {
						sr.setThread(mt);
						pilotIDs.add(new Integer(mt.getAuthorID()));
						pilotIDs.add(new Integer(mt.getLastUpdateID()));
					} else
						i.remove();
				} else {
					log.warn("Cannot find Message Thread " + sr.getID() + ", removing from index");
					SearchUtils.delete(sr.getID());
					i.remove();
				}
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
			vc.setResults(results);
			ctx.setAttribute("pilots", authors, REQUEST);
		} catch (IOException ie) {
			throw new CommandException(ie);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set search attribute
		ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/cooler/threadSearch.jsp");
		result.setSuccess(true);
	}
}