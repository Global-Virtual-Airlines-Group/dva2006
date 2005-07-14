package org.deltava.commands.main;

import java.sql.Connection;
import java.util.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * Command to display the Staff Roster.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class StaffRosterCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {
        
        try {
            Connection con = ctx.getConnection();
            
            // Get the roster
            GetStaff dao = new GetStaff(con);
            List results = dao.getStaff();

            // Sort the results and stuff in the request
            Collections.sort(results);
            ctx.setAttribute("staffRoster", results, REQUEST);
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Set the result page and return
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/roster/staffRoster.jsp");
        result.setSuccess(true);
    }
}