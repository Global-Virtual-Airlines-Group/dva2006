// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.cooler.*;
import org.deltava.beans.system.UserDataMap;
import org.deltava.commands.*;

import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to search the Water Cooler
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
			ctx.setAttribute("channels", cdao.getChannels(SystemData.getAirline(SystemData.get("airline.code")), ctx.getRoles()), REQUEST);

			// Check if we're doing a GET, and redirect to the results JSP
			if (ctx.getParameter("searchStr") == null) {
				ctx.release();
				result.setURL("/jsp/cooler/threadSearch.jsp");
				result.setSuccess(true);
				return;
			}

			// Get the DAO and search
			GetCoolerThreads dao = new GetCoolerThreads(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			List threads = dao.search(ctx.getParameter("searchStr"), ctx.getParameter("channel"));

			// Filter out the threads based on our access
			Set pilotIDs = new HashSet();
			CoolerThreadAccessControl access = new CoolerThreadAccessControl(ctx);
			for (Iterator i = threads.iterator(); i.hasNext();) {
				MessageThread mt = (MessageThread) i.next();
				Channel c = cdao.get(mt.getChannel());
				access.updateContext(mt, c);
				access.validate();

				// Remove the thread if we cannot access it
				if (!access.getCanRead()) {
					i.remove();
				} else {
					pilotIDs.add(new Integer(mt.getAuthorID()));
					pilotIDs.add(new Integer(mt.getLastUpdateID()));
				}
			}

			// Get the location of all the Pilots
			GetUserData usrdao = new GetUserData(con);
			UserDataMap udm = usrdao.get(pilotIDs);
			ctx.setAttribute("userData", udm, REQUEST);

			// Get the authors for the last post in each channel
			Map authors = new HashMap();
			GetPilot pdao = new GetPilot(con);
			GetApplicant adao = new GetApplicant(con);
			for (Iterator i = udm.getTableNames().iterator(); i.hasNext();) {
				String tableName = (String) i.next();
				Set IDs = new HashSet(udm.getByTable(tableName));
				if (tableName.endsWith("APPLICANTS")) {
					authors.putAll(adao.getByID(IDs, tableName));
				} else {
					authors.putAll(pdao.getByID(IDs, tableName));
				}
			}

			// Get the pilot IDs in the returned threads
			ctx.setAttribute("pilots", authors, REQUEST);

			// Save the threads in the request
			vc.setResults(threads);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		result.setURL("/jsp/cooler/threadList.jsp");
		result.setSuccess(true);
	}
}