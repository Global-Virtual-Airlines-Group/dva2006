// Copyright 2005, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 2.8
 * @since 1.0
 */

public class NavaidSearchCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		Collection<NavigationDataBean> results = null;
		try {
			GetNavData dao = new GetNavData(ctx.getConnection());
			NavigationDataMap ndMap = dao.get(ctx.getParameter("navaidCode"));
			if (ndMap == null)
				ndMap = new NavigationDataMap();

			// Save results
			results = ndMap.getAll();
			ctx.setAttribute("results", results, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Translate the names
		Collection<ComboAlias> options = new ArrayList<ComboAlias>();
		for (Iterator<NavigationDataBean> i = results.iterator(); i.hasNext();) {
			NavigationDataBean nd = i.next();
			StringBuilder buf = new StringBuilder();
			if (nd.getType() == NavigationDataBean.INT)
				buf.append(nd.getCode());
			else {
				buf.append(nd.getName());
				buf.append(" (");
				buf.append(nd.getCode());
				buf.append(')');
			}

			buf.append(" [");
			buf.append(nd.getTypeName());
			buf.append(']');
			options.add(ComboUtils.fromString(buf.toString(), nd.toString()));
		}

		// Save options and "show surrounding" setting
		ctx.setAttribute("options", options, REQUEST);
		ctx.setAttribute("showSurroundingNavaids", Boolean.valueOf(ctx.getParameter("showSurrounding")), SESSION);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/navdata/navaidInfo.jsp");
		result.setSuccess(true);
	}
}