// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;
import org.deltava.beans.UserDataMap;
import org.deltava.beans.fleet.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.FileEntryAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display user-sharable files.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class UserLibraryCommand extends AbstractViewCommand {

	private static final Logger log = Logger.getLogger(UserLibraryCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the view start/end
		ViewContext vc = initView(ctx);

		Collection<FileEntry> results = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the library
			GetLibrary dao = new GetLibrary(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(Math.round(vc.getCount() * 1.25f));
			results = dao.getFiles(SystemData.get("airline.db"));

			// Get the authors
			Set<Integer> authors = new HashSet<Integer>();
			for (Iterator<FileEntry> i = results.iterator(); i.hasNext();) {
				FileEntry e = i.next();
				authors.add(new Integer(e.getAuthorID()));
			}

			// Get the author data
			GetUserData uddao = new GetUserData(con);
			UserDataMap udmap = uddao.get(authors);
			ctx.setAttribute("userData", udmap, REQUEST);

			// Get the author profiles
			GetPilot pdao = new GetPilot(con);
			Map<Integer, Pilot> pilots = pdao.get(udmap);
			ctx.setAttribute("authors", pilots, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Calculate access for adding content
		FileEntryAccessControl access = new FileEntryAccessControl(ctx, null);
		access.validate();
		ctx.setAttribute("access", access, REQUEST);

		// Create access map
		Map<String, FileEntryAccessControl> accessMap = new HashMap<String, FileEntryAccessControl>();

		// Validate our access to the results
		for (Iterator<FileEntry> i = results.iterator(); i.hasNext();) {
			FileEntry e = i.next();
			access = new FileEntryAccessControl(ctx, e);
			access.validate();
			accessMap.put(e.getFileName(), access);

			// Check that the resource exists
			if (e.getSize() == 0) {
				log.warn(e.getFullName() + " not found in file system!");
				if (!access.getCanEdit())
					i.remove();
			} else if (!access.getCanView()) {
				i.remove();
			}
		}

		// Save the results in the request
		ctx.setAttribute("files", results, REQUEST);
		ctx.setAttribute("accessMap", accessMap, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/fleet/userLibrary.jsp");
		result.setSuccess(true);
	}
}