// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.schedule;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.FileUpload;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.schedule.PreferredRoute;

import org.deltava.commands.*;

import org.deltava.dao.SetRoute;
import org.deltava.dao.DAOException;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to import Domestic Route data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 * @deprecated
 */

@Deprecated
public class RouteImportCommand extends AbstractCommand {

	private static final Logger log = Logger.getLogger(RouteImportCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the Command result
		CommandResult result = ctx.getResult();

		// If we're doing a GET, then redirect to the JSP
		FileUpload routeData = ctx.getFile("routeData");
		if (routeData == null) {
			result.setURL("/jsp/schedule/routeImport.jsp");
			result.setSuccess(true);
			return;
		}

		// Check our access level
		ScheduleAccessControl access = new ScheduleAccessControl(ctx);
		access.validate();
		if (!access.getCanImport())
			throw securityException("Cannot import Preferred Routes");

		try {
			Connection con = ctx.getConnection();

			// Get the write DAO
			SetRoute wdao = new SetRoute(con);

			// Get the CSV file - skipping the first line
			InputStream is = new ByteArrayInputStream(routeData.getBuffer());
			LineNumberReader br = new LineNumberReader(new InputStreamReader(is));
			br.readLine();

			// Iterate through the CSV data
			Collection<String> warns = new ArrayList<String>();
			int routesSaved = 0;
			while (br.ready()) {
				List<String> tokens = StringUtils.split(br.readLine(), ",");
				if (tokens.size() != 13) {
					log.warn("Possible bad data on Line #" + br.getLineNumber() + ", elements = " + tokens.size());
					warns.add("Possible bad data on Line #" + br.getLineNumber() + ", elements = " + tokens.size());
				}

				// Create the preferred route
				Airport airportD = SystemData.getAirport(tokens.get(0));
				Airport airportA = SystemData.getAirport(tokens.get(2));
				PreferredRoute pr = new PreferredRoute(airportD, airportA);
				pr.setRoute(tokens.get(1));

				// Get the source/departure ARTCCs
				Set<String> artccList = new HashSet<String>();
				artccList.add(tokens.get(tokens.size() - 1));
				artccList.add(tokens.get(tokens.size()));
				pr.setARTCC(StringUtils.listConcat(artccList, " "));

				// Save the preferred route
				if ((airportA != null) && (airportD != null)) {
					try {
						wdao.write(pr);
						routesSaved++;
					} catch (DAOException de) {
						log.error("Error saving Line #" + br.getLineNumber() + " - " + de.getMessage());
						warns.add("Error saving Line #" + br.getLineNumber() + " - " + de.getMessage());
					}
				}
			}

			// Save the route count
			ctx.setAttribute("isImport", Boolean.TRUE, REQUEST);
			ctx.setAttribute("routeCount", new Integer(routesSaved), REQUEST);
			ctx.setAttribute("warnings", warns, REQUEST);
		} catch (IOException ie) {
			throw new CommandException(ie);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/schedule/routeUpdate.jsp");
		result.setSuccess(true);
	}
}