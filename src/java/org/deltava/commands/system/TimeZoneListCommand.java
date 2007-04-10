// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;

import org.deltava.beans.TZInfo;

import org.deltava.commands.*;

/**
 * A Web Site Command to display available time zones.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TimeZoneListCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the view context
		ViewContext vc = initView(ctx);
		
		// Get the entries and shrink to fit
		List<TZInfo> entries = new ArrayList<TZInfo>(TZInfo.getAll());
		entries.removeAll(entries.subList(0, vc.getStart()));
		if (entries.size() > vc.getCount())
			entries.removeAll(entries.subList(vc.getCount() + 1, entries.size()));
		
		// Save the entries
		vc.setResults(entries);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/tzList.jsp");
		result.setSuccess(true);
	}
}