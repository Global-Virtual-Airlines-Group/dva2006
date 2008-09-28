// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.StaffAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to create new Staff Profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class StaffProfileCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the results
		CommandResult result = ctx.getResult();

		try {
			Connection con = ctx.getConnection();

			// Get the Pilot to modify
			GetPilot rdao = new GetPilot(con);
			Pilot p = rdao.get(ctx.getID());
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());

			// Check our access
			StaffAccessControl access = new StaffAccessControl(ctx, null);
			access.validate();
			if (!access.getCanCreate())
				throw securityException("Cannot create Staff Profile");

			// Check if we already have a Staff Profile
			GetStaff rsdao = new GetStaff(con);
			Staff s = rsdao.get(ctx.getID());
			if (s != null)
				throw securityException("Staff Profile already exists!");

			// Save the pilot object in the request
			ctx.setAttribute("pilot", p, REQUEST);

			// If we're creating a new staff profile, then redirect
			if (ctx.getParameter("staffTitle") == null) {
				ctx.release();
				result.setURL("/jsp/pilot/staffCreate.jsp");
				result.setSuccess(true);
				return;
			}

			// Create a new Staff Profile bean from the request
			s = new Staff(p.getFirstName(), p.getLastName());
			s.setID(p.getID());
			s.setTitle(ctx.getParameter("staffTitle"));
			s.setBody(ctx.getParameter("staffBody"));
			s.setSortOrder(StringUtils.parse(ctx.getParameter("staffSort"), 6));
			s.setArea(ctx.getParameter("staffArea"));

			// Save the Staff Profile
			SetStaff wdao = new SetStaff(con);
			wdao.write(s);

			// Create a status update
			StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.COMMENT);
			upd.setAuthorID(ctx.getUser().getID());
			upd.setDescription("Created Staff Roster profile");

			// Save the status update
			SetStatusUpdate swdao = new SetStatusUpdate(con);
			swdao.write(upd);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		ctx.setAttribute("spUpdated", Boolean.TRUE, REQUEST);
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/pilot/pilotUpdate.jsp");
		result.setSuccess(true);
	}
}