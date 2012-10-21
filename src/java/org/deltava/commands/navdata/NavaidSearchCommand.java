// Copyright 2005, 2007, 2008, 2009, 2010, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.util.*;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.navdata.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.ComboUtils;

/**
 * A Web Site Command to search Naivgation Data.
 * @author Luke
 * @version 5.0
 * @since 1.0
 */

public class NavaidSearchCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check for POST
		CommandResult result = ctx.getResult();
		String code = ctx.getParameter("navaidCode");
		if (code == null) {
			result.setURL("/jsp/navdata/navaidInfo.jsp");
			result.setSuccess(true);
			return;
		}

		List<NavigationDataBean> results = new ArrayList<NavigationDataBean>();
		try {
			GetNavData dao = new GetNavData(ctx.getConnection());
			NavigationDataMap ndMap = dao.get(code);
			if (ndMap == null)
				ndMap = new NavigationDataMap();

			// Save results
			results.addAll(ndMap.getAll());
			ctx.setAttribute("results", results, REQUEST);
			if (!results.isEmpty())
				ctx.setAttribute("mapCenter", results.get(0), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Translate the names
		Collection<ComboAlias> options = new ArrayList<ComboAlias>();
		for (NavigationDataBean nd : results) {
			StringBuilder buf = new StringBuilder();
			if (nd.getType() == Navaid.INT)
				buf.append(nd.getCode());
			else {
				buf.append(nd.getName());
				buf.append(" (");
				buf.append(nd.getCode());
				buf.append(')');
			}

			buf.append(" [");
			buf.append(nd.getType().getName());
			buf.append(']');
			options.add(ComboUtils.fromString(buf.toString(), nd.toString()));
		}

		// Save options and "show surrounding" setting
		ctx.setAttribute("options", options, REQUEST);
		ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
		ctx.setAttribute("showSurroundingNavaids", Boolean.valueOf(ctx.getParameter("showSurrounding")), SESSION);

		// Forward to the JSP
		result.setURL("/jsp/navdata/navaidInfo.jsp");
		result.setSuccess(true);
	}
}