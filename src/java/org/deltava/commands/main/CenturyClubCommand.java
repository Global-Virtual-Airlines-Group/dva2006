package org.deltava.commands.main;

import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetPilotRecognition;
import org.deltava.dao.DAOException;

/**
 * A Web Site Command to display &quot;Century Club&quot; members.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CenturyClubCommand extends AbstractCommand {

	 /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO
			GetPilotRecognition dao = new GetPilotRecognition(con);
			ctx.setAttribute("roster", dao.getCenturyClub(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/roster/centuryClub.jsp");
		result.setSuccess(true);
	}
}