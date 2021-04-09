// Copyright 2005, 2006, 2016, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.fleet.Newsletter;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.FleetEntryAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to view Newsletters.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class NewsLibraryCommand extends AbstractViewCommand {

	private static final Logger log = Logger.getLogger(DocumentLibraryCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the newsletter category
		String catName = ctx.getParameter("category");
		if (catName == null)
			catName = SystemData.get("airline.newsletters.name");
		
		// Save the category name
		ctx.setAttribute("catName", catName, REQUEST);

		Collection<Newsletter> results = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and newsletters
			GetDocuments dao = new GetDocuments(con);
			if ("ALL".equals(catName))
				results = dao.getNewsletters(ctx.getDB());
			else
				results = dao.getNewslettersByCategory(catName);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Calculate access for adding content
		FleetEntryAccessControl access = new FleetEntryAccessControl(ctx, null);
		access.validate();
		ctx.setAttribute("access", access, REQUEST);

		// Validate our access to the results
		access = new FleetEntryAccessControl(ctx, null);
		for (Iterator<Newsletter> i = results.iterator(); i.hasNext();) {
			Newsletter nws = i.next();
			access.setEntry(nws);
			access.validate();

			// Check that the resource exists
			if (nws.getSize() == 0) {
				log.warn(nws.getFullName() + " not found in file system!");
				if (!ctx.isUserInRole("Fleet"))
					i.remove();
			} else if (!access.getCanView())
				i.remove();
		}

		// Save the results in the request
		ctx.setAttribute("docs", results, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/fleet/newsLibrary.jsp");
		result.setSuccess(true);
	}
}