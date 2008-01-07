// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.fleet.*;
import org.deltava.beans.academy.Course;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site command to display the Document Library. Note that this command will display library entries from other
 * Airlines, with the proviso that <i>all files are in the same library path</i>.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class DocumentLibraryCommand extends AbstractLibraryCommand {

	private static final Logger log = Logger.getLogger(DocumentLibraryCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Calculate access for adding content
		FleetEntryAccessControl access = null;
		Map<AirlineInformation, Collection<Manual>> results = new LinkedHashMap<AirlineInformation, Collection<Manual>>();
		try {
			Connection con = ctx.getConnection();

			// Get the document libraries from the other airlines
			GetDocuments dao = new GetDocuments(con);
			Map apps = (Map) SystemData.getObject("apps");
			for (Iterator i = apps.values().iterator(); i.hasNext();) {
				AirlineInformation info = (AirlineInformation) i.next();
				if (info.getDB().equalsIgnoreCase(SystemData.get("airline.db")))
					results.put(info, dao.getManuals(info.getDB(), false));
				else {
					Collection<Manual> entries = dao.getManuals(info.getDB(), false);
					appendDB(entries, info.getDB());
					results.put(info, entries);
				}
			}

			// Load the user's Flight Academy courses
			if (SystemData.getBoolean("academy.enabled")) {
				GetAcademyCourses cdao = new GetAcademyCourses(con);
				access = new ManualAccessControl(ctx, cdao.getByPilot(ctx.getUser().getID()));
			} else
				access = new ManualAccessControl(ctx, new HashSet<Course>());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Validate our access to the results
		for (Iterator<Collection<Manual>> ci = results.values().iterator(); ci.hasNext();) {
			Collection<Manual> manuals = ci.next();
			for (Iterator<Manual> i = manuals.iterator(); i.hasNext();) {
				Manual e = i.next();
				access.setEntry(e);
				access.validate();

				// Check that the resource exists
				if (e.getSize() == 0) {
					log.warn(e.getFullName() + " not found in file system!");
					if (!ctx.isUserInRole("Fleet"))
						i.remove();
				} else if (!access.getCanView()) {
					i.remove();
				}
			}
		}

		// Save the results in the request
		ctx.setAttribute("docs", results, REQUEST);
		ctx.setAttribute("access", access, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/fleet/docLibrary.jsp");
		result.setSuccess(true);
	}
}