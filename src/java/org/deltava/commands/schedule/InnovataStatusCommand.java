// Copyright 2006, 2007, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.io.*;

import org.deltava.commands.*;

import org.deltava.dao.DAOException;
import org.deltava.dao.file.innovata.GetImportStatus;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display the results of a scheduled Innovata Schedule download.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class InnovataStatusCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the command results
		CommandResult result = ctx.getResult();

		// Get the data
		File f = new File(SystemData.get("schedule.innovata.cache"), SystemData.get("airline.code") + ".import.status.txt");
		if (!f.exists()) {
			result.setURL("/jsp/schedule/innovataStatus.jsp");
			result.setSuccess(true);
			return;
		}
		
		try (InputStream fs = new FileInputStream(f)) {
			GetImportStatus dao = new GetImportStatus(fs);
			dao.load();
			ctx.setAttribute("msgs", dao.getMessages(), REQUEST);
			ctx.setAttribute("airlines", dao.getUnknownAirlines(), REQUEST);
			ctx.setAttribute("airports", dao.getUnknownAirports(), REQUEST);
			ctx.setAttribute("eqTypes", dao.getUnknownEquipment(), REQUEST);
		} catch (IOException ie) {
			throw new CommandException(ie);
		} catch (DAOException de) {
			throw new CommandException(de);
		}

		// Forward to the JSP
		result.setURL("/jsp/schedule/innovataStatus.jsp");
		result.setSuccess(true);
	}
}