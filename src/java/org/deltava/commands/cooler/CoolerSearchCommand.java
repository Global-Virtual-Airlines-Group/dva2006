// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Person;
import org.deltava.beans.UserDataMap;
import org.deltava.beans.cooler.*;
import org.deltava.commands.*;

import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to search the Water Cooler.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CoolerSearchCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command result
		CommandResult result = ctx.getResult();

		// Get/set start/count parameters and channel name
		ViewContext vc = initView(ctx);

		try {
			Connection con = ctx.getConnection();

			// Get the channel names
			GetCoolerChannels cdao = new GetCoolerChannels(con);
			ctx.setAttribute("channels", cdao.getChannels(SystemData.getApp(SystemData.get("airline.code")), ctx
					.getRoles()), REQUEST);

			// Check if we're doing a GET, and redirect to the results JSP
			if (ctx.getParameter("searchStr") == null) {
				ctx.release();
				result.setURL("/jsp/cooler/threadSearch.jsp");
				result.setSuccess(true);
				return;
			}

			// Build the search criteria
			SearchCriteria criteria = new SearchCriteria(ctx.getParameter("searchStr"));
			criteria.setChannel(ctx.getParameter("channel"));
			criteria.setSearchSubject(Boolean.valueOf(ctx.getParameter("checkSubject")).booleanValue());
			criteria.setSearchNameFragment(Boolean.valueOf(ctx.getParameter("nameMatch")).booleanValue());

			// If we're doing a Pilot name search, start that first
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			if (!StringUtils.isEmpty(ctx.getParameter("pilotName"))) {
				pdao.setQueryMax(50);
				criteria.addIDs(pdao.search(ctx.getParameter("pilotName"), criteria.getSearchNameFragment()));
				pdao.setQueryMax(0);
			}

			// Get the DAO and search
			Collection<MessageThread> threads = null;
			synchronized (CoolerSearchCommand.class) {
				GetCoolerThreads dao = new GetCoolerThreads(con);
				dao.setQueryStart(vc.getStart());
				dao.setQueryMax(vc.getCount());
				threads = dao.search(criteria);
			}

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
					pilotIDs.add(new Integer(mt.getAuthorID()));
					pilotIDs.add(new Integer(mt.getLastUpdateID()));
				} else
					i.remove();
			}

			// Get the location of all the Pilots
			GetUserData usrdao = new GetUserData(con);
			UserDataMap udm = usrdao.get(pilotIDs);
			ctx.setAttribute("userData", udm, REQUEST);

			// Get the authors for the last post in each channel
			Map<Integer, Person> authors = new HashMap<Integer, Person>();
			GetApplicant adao = new GetApplicant(con);
			for (Iterator<String> i = udm.getTableNames().iterator(); i.hasNext();) {
				String tableName = i.next();
				if (tableName.endsWith("APPLICANTS"))
					authors.putAll(adao.getByID(udm.getByTable(tableName), tableName));
				else
					authors.putAll(pdao.getByID(udm.getByTable(tableName), tableName));
			}

			// Save the threads in the request
			vc.setResults(threads);
			ctx.setAttribute("pilots", authors, REQUEST);
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