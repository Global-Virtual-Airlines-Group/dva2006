// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to manually reload the SystemData model while the application is running.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SystemDataReloadCommand extends AbstractCommand {

	private static final Logger log = Logger.getLogger(SystemData.class);

	/**
	 * Executes the Command.
	 * @param ctx the Command Context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Double check that we're an admin
		if (!ctx.isUserInRole("Admin"))
			throw securityException("Cannot Execute");

		// Get command result
		CommandResult result = ctx.getResult();

		// Check for parameters
		if (ctx.getParameter("reloadSchedule") == null) {
			result.setURL("/jsp/admin/systemDataReload.jsp");
			result.setSuccess(true);
			return;
		}

		// Reload the system data
		log.warn("SystemData reloaded by " + ctx.getUser().getName());
		SystemData.init("DEFAULT", false);

		// Save the property names in the request
		ctx.setAttribute("loader", SystemData.get(SystemData.LOADER_NAME), REQUEST);
		ctx.setAttribute("propertyNames", SystemData.getNames(), REQUEST);

		// Reload schedule data if requested
		boolean isSchedReload = Boolean.valueOf(ctx.getParameter("reloadSchedule")).booleanValue();
		if (isSchedReload) {
			try {
				Connection con = ctx.getConnection();

				// Load Database information
				log.info("Loading Cross-Application data");
				GetUserData uddao = new GetUserData(con);
				SystemData.add("apps", uddao.getAirlines(true));

				// Load active airlines
				log.info("Loading Airline Codes");
				GetAirline aldao = new GetAirline(con);
				SystemData.add("airlines", aldao.getAll());

				// Load airports
				log.info("Loading Airports");
				GetAirport apdao = new GetAirport(con);
				SystemData.add("airports", apdao.getAll());
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
			
			ctx.setAttribute("isSchedReload", Boolean.TRUE, REQUEST);
		}

		// Set status attribute
		ctx.setAttribute("isReload", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		result.setURL("/jsp/admin/systemDataReload.jsp");
		result.setSuccess(true);
	}
}