package org.deltava.commands.fleet;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.fleet.FleetEntry;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.commands.*;

import org.deltava.dao.GetLibrary;
import org.deltava.dao.DAOException;

import org.deltava.security.command.FleetEntryAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site command to display the Document Library. Note that this command will display library entries from other
 * Airlines, with the proviso that <i>all files are in the same library path</i>.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class DocumentLibraryCommand extends AbstractCommand {

	private static final Logger log = Logger.getLogger(DocumentLibraryCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		List results = new ArrayList();
		try {
			Connection con = ctx.getConnection();

			// Get the DAO
			GetLibrary dao = new GetLibrary(con);

			// Get the document libraries from the other airlines
			Map apps = (Map) SystemData.getObject("apps");
			for (Iterator i = apps.values().iterator(); i.hasNext();) {
				AirlineInformation info = (AirlineInformation) i.next();
				results.addAll(dao.getManuals(info.getDB()));
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Calculate access for adding content
		FleetEntryAccessControl access = new FleetEntryAccessControl(ctx, null);
		ctx.setAttribute("access", access, REQUEST);

		// Validate our access to the results
		for (Iterator i = results.iterator(); i.hasNext();) {
			FleetEntry e = (FleetEntry) i.next();
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

		// Save the results in the request
		ctx.setAttribute("docs", results, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/fleet/docLibrary.jsp");
		result.setSuccess(true);
	}
}