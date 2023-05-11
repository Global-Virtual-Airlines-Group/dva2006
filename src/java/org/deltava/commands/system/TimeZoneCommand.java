// Copyright 2007, 2012, 2016, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;
import java.time.*;
import java.util.stream.Collectors;

import org.deltava.beans.TZInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.gvagroup.common.*;

/**
 * A Web Site Command to edit time zone profiles.
 * @author Luke
 * @version 10.6
 * @since 1.0
 */

public class TimeZoneCommand extends AbstractFormCommand {

	private class TZComparator implements Comparator<ZoneId> {
		
		private final Instant NOW = Instant.now();

		@Override
		public int compare(ZoneId tz1, ZoneId tz2) {
			ZoneOffset zo1 = tz1.getRules().getOffset(NOW);
			ZoneOffset zo2 = tz2.getRules().getOffset(NOW);
			int tmpResult = zo1.compareTo(zo2);
			return (tmpResult == 0) ? tz1.getId().compareTo(tz2.getId()) : tmpResult;
		}
	}
	
	/**
	 * Callback method called when editing an existing Time Zone.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {

		// Get the ID
		String id = (String) ctx.getCmdParameter(ID, null);

		// Get the time zone
		TZInfo tz = TZInfo.get(id);
		if ((tz == null) && (id != null))
			throw notFoundException("Unknown Time Zone - " + id);
		
		// Get time zone IDs and convert to combo util
		List<ZoneId> zoneIDs = ZoneId.getAvailableZoneIds().stream().map(ZoneId::of).collect(Collectors.toList());
		zoneIDs.sort(new TZComparator());

		// Save the time zone and JVM options
		ctx.setAttribute("tz", tz, REQUEST);
		ctx.setAttribute("tzIDs", zoneIDs, REQUEST);
		
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
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}

	/**
	 * Callback method called when saving a Time Zone profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {

		String id = (String) ctx.getCmdParameter(ID, null);
		boolean isNew = (TZInfo.get(id) == null);
		TZInfo tz = TZInfo.init(ctx.getParameter("newID"), ctx.getParameter("name"), ctx.getParameter("abbr"));
		try {
			SetSystemData wdao = new SetSystemData(ctx.getConnection());
			if (isNew)
				wdao.write(tz);
			else
				wdao.update(id, tz);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Notify other webapps
		EventDispatcher.send(new SystemEvent(EventType.TZ_RELOAD));

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("tzones.do");
		result.setSuccess(true);
	}
}