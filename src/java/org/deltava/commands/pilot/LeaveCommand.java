// Copyright 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.Date;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.CalendarUtils;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command for Pilots to take a Leave of Absence.
 * @author Luke
 * @version 2.8
 * @since 1.0
 */

public class LeaveCommand extends AbstractCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occrurs.
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the pilot ID
		int id = (ctx.getID() == 0) ? ctx.getUser().getID() : ctx.getID();
		try {
			Connection con = ctx.getConnection();

			// Get the Pilot to process
			GetPilot rdao = new GetPilot(con);
			Pilot p = rdao.get(id);
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + id);

			// Check our access
			PilotAccessControl access = new PilotAccessControl(ctx, p);
			access.validate();
			if (!access.getCanTakeLeave())
				throw securityException("Insufficient Access to place Pilot On Leave");
			
			// Calculate the LOA expiry date
			Date loaExpiry = CalendarUtils.adjust(null, SystemData.getInt("users.inactive_leave_days", 180));
			ctx.setAttribute("loaExpires", loaExpiry, REQUEST);
			
			// Create status update record
			StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.LOA); 
			upd.setAuthorID(ctx.getUser().getID());
			upd.setDescription("Leave of absence until " + StringUtils.format(loaExpiry, "MM/dd/yyyy"));

			// Start the transaction
			ctx.startTX();

			// Update the Pilot's status
			SetPilot wdao = new SetPilot(con);
			wdao.onLeave(p.getID());
			p.setStatus(Pilot.ON_LEAVE);
			
			// Add an inactivity table entry
			SetInactivity idao = new SetInactivity(con);
			idao.setInactivity(p.getID(), SystemData.getInt("users.inactive_leave_days", 180), true);
			
			// Write the status updat
			SetStatusUpdate sudao = new SetStatusUpdate(con);
			sudao.write(upd);

			// Commit the transaction
			ctx.commitTX();

			// Save the pilot in the request
			ctx.setAttribute("pilot", p, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/pilot/pilotLOA.jsp");
		result.setSuccess(true);
	}
}