// Copyright 2005, 2006, 2016, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.Connection;

import org.deltava.beans.schedule.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.*;

/**
 * A Web Site Command to delete Flight Schedule entries.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class ScheduleDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the source/line
		ScheduleSource src = EnumUtils.parse(ScheduleSource.class, ctx.getParameter("src"), ScheduleSource.MANUAL);
		int srcLine = StringUtils.parse(ctx.getParameter("srcLine"), -1);
		if (srcLine < 0)
			throw notFoundException("Invalid Source Line - " + srcLine);

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the entry
			GetRawSchedule dao = new GetRawSchedule(con);
			RawScheduleEntry entry = dao.get(src, srcLine);
			if (entry == null)
				throw notFoundException("Invalid Schedule Entry - " + src.getDescription() + " Line " + srcLine);
			
			// Check our access
			ScheduleAccessControl ac = new ScheduleAccessControl(ctx, entry);
			ac.validate();
			if (!ac.getCanDelete())
				throw securityException("Cannot modify Flight Schedule");

			// Delete the entry
			SetSchedule wdao = new SetSchedule(con);
			wdao.delete(entry);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status attributes
		ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);
		ctx.setAttribute("src", src, REQUEST);
		ctx.setAttribute("srcLine", Integer.valueOf(srcLine), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/schedule/scheduleUpdate.jsp");
		result.setSuccess(true);
	}
}