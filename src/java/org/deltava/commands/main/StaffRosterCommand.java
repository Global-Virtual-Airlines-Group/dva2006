// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.main;

import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display the Staff Roster.
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
            
            // Get the roster and stuff in the request
            GetStaff dao = new GetStaff(con);
            ctx.setAttribute("staffRoster", dao.getStaff(), REQUEST);
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