// Copyright 2005, 2006, 2007, 2008, 2010, 2016, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.academy.Course;
import org.deltava.beans.fleet.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.*;

/**
 * A Web Site command to display the Document Library.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class DocumentLibraryCommand extends AbstractLibraryCommand {

	private static final Logger log = Logger.getLogger(DocumentLibraryCommand.class);
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Calculate access for adding content
		Collection<Course> courses = null;
		Collection<Manual> results = new ArrayList<Manual>();
		try {
			Connection con = ctx.getConnection();

			// Get the documents
			GetDocuments dao = new GetDocuments(con);
			results.addAll(dao.getManuals(ctx.getDB()));

			// Load the user's Flight Academy courses
			GetAcademyCourses cdao = new GetAcademyCourses(con);
			courses = cdao.getByPilot(ctx.getUser().getID());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Validate our access to the results
		Map<Manual, ManualAccessControl> acMap = new HashMap<Manual, ManualAccessControl>();
		for (Iterator<Manual> i = results.iterator(); i.hasNext();) {
			Manual e = i.next();
			ManualAccessControl ac = new ManualAccessControl(ctx, courses);
			ac.setEntry(e);
			ac.validate();

			// Check that the resource exists
			if (e.getSize() == 0) {
				log.warn(e.getFullName() + " not found in file system!");
				if (!ctx.isUserInRole("Fleet"))
					i.remove();
			} else if (!ac.getCanView())
				i.remove();

			acMap.put(e, ac);
		}

		// Save the results in the request
		ctx.setAttribute("docs", results, REQUEST);
		ctx.setAttribute("accessMap", acMap, REQUEST);
		
		// Check default acces
		ManualAccessControl access = new ManualAccessControl(ctx, courses);
		access.validate();
		ctx.setAttribute("access", access, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/fleet/docLibrary.jsp");
		result.setSuccess(true);
	}
}