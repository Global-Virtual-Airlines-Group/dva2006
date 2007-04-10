// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.sql.Connection;
import java.util.TimeZone;

import org.deltava.beans.TZInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.ComboUtils;

/**
 * A Web Site Command to edit time zone profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TimeZoneCommand extends AbstractFormCommand {

	/**
	 * Callback method called when editing an existing Time Zone.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {

		// Get the ID
		String id = (String) ctx.getCmdParameter(ID, null);

		// Get the time zone
		TZInfo tz = TZInfo.get(id);
		if ((tz == null) && (id != null))
			throw notFoundException("Unknown Time Zone - " + id);

		// Save the time zone and JVM options
		ctx.setAttribute("tz", tz, REQUEST);
		ctx.setAttribute("tzNames", ComboUtils.fromArray(TimeZone.getAvailableIDs()), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/tzEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading an existing Time Zone.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}

	/**
	 * Callback method called when saving a Time Zone profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {

		String id = (String) ctx.getCmdParameter(ID, null);
		boolean isNew = (TZInfo.get(id) == null);
		try {
			Connection con = ctx.getConnection();

			// Get the time zone
			TZInfo tz = TZInfo.init(id, ctx.getParameter("name"), ctx.getParameter("abbr"));
			
			// Write the time zone
			SetSystemData wdao = new SetSystemData(con);
			if (isNew)
				wdao.write(tz);
			else
				wdao.update(tz);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REDIRECT);
		result.setURL("tzlist.do");
		result.setSuccess(true);
	}
}