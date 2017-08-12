// Copyright 2006, 2007, 2012, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.UserData;
import org.deltava.beans.acars.ACARSError;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display an ACARS client error report.
 * @author Luke
 * @version 7.5
 * @since 1.0
 */

public class ErrorLogEntryCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the entry
			GetACARSErrors dao = new GetACARSErrors(con);
			ACARSError err = dao.get(ctx.getID());
			if (err == null)
				throw notFoundException("Invalid Error Report - " + ctx.getID());
			
			// Load the author
			GetPilot pdao = new GetPilot(con);
			GetUserData uddao = new GetUserData(con);
			UserData ud = uddao.get(err.getAuthorID());
			ctx.setAttribute("userData", ud, REQUEST);
			ctx.setAttribute("author", pdao.get(ud), REQUEST);
			
			// Convert the state data
			if (!StringUtils.isEmpty(err.getStateData())) {
				String[] state = err.getStateData().split("[||]");
				Map<String, String> stateData = new LinkedHashMap<String, String>();
				for (String s : state) {
					int pos = s.indexOf('=');
					if (pos > -1)
						stateData.put(s.substring(0, pos), s.substring(pos + 1));
				}
				
				ctx.setAttribute("stateData", stateData, REQUEST);
			}
			
			// Load the address data
			GetIPLocation ipdao = new GetIPLocation(con);
			ctx.setAttribute("ipInfo", ipdao.get(err.getRemoteAddr()), REQUEST);
			
			// Load the client data
			GetSystemInfo sysdao = new GetSystemInfo(con);
			ctx.setAttribute("acarsClientInfo", sysdao.get(ud.getID(), err.getSimulator(), err.getCreatedOn()), REQUEST);

			// Save the error report
			ctx.setAttribute("err", err, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/acars/errorReport.jsp");
		result.setSuccess(true);
	}
}