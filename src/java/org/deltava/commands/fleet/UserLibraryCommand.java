// Copyright 2005, 2006, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.fleet.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.FileEntryAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display user-sharable files.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class UserLibraryCommand extends AbstractViewCommand {

	private static final Logger log = Logger.getLogger(UserLibraryCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		ViewContext<FileEntry> vc = initView(ctx, FileEntry.class);
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the library
			GetLibrary dao = new GetLibrary(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(Math.round(vc.getCount() * 1.25f));
			vc.setResults(dao.getFiles(SystemData.get("airline.db")));

			// Get the author data
			Collection<Integer> IDs = vc.getResults().stream().map(FileEntry::getAuthorID).collect(Collectors.toSet());
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			UserDataMap udm = uddao.get(IDs);
			ctx.setAttribute("userData", udm, REQUEST);
			ctx.setAttribute("authors", pdao.get(udm), REQUEST);
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
		for (Iterator<FileEntry> i = vc.getResults().iterator(); i.hasNext();) {
			FileEntry e = i.next();
			access = new FileEntryAccessControl(ctx, e);
			access.validate();
			accessMap.put(e.getFileName(), access);

			// Check that the resource exists
			if (e.getSize() == 0) {
				log.warn(e.getFullName() + " not found in file system!");
				if (!access.getCanEdit())
					i.remove();
			} else if (!access.getCanView())
				i.remove();
		}

		// Save the results in the request
		ctx.setAttribute("accessMap", accessMap, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/fleet/userLibrary.jsp");
		result.setSuccess(true);
	}
}