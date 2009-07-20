// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.util.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A web site command to view pilot statistics by letter.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class RosterLetterCommand extends AbstractViewCommand {

    // List of letters to display
    private static final List<?> LETTERS = Arrays.asList(new String [] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"});

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {
        
        // Get/set start/count parameters
        ViewContext vc = initView(ctx);
        
        // Save the letters
        ctx.setAttribute("letters", LETTERS, REQUEST);
        
        // Get the letter to display
        String letter = (String) ctx.getCmdParameter(Command.OPERATION, null);
        
        // If no letter specified, just redirect to the JSP
        if (letter == null) {
            CommandResult result = ctx.getResult();
            result.setURL("/jsp/roster/letterRoster.jsp");
            result.setSuccess(true);
            return;
        }
        
        try {
            GetPilot dao = new GetPilot(ctx.getConnection());
            dao.setQueryStart(vc.getStart());
            dao.setQueryMax(vc.getCount());
            vc.setResults(dao.getPilotsByLetter(letter));
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
      
        // Set the result page and return
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/roster/letterRoster.jsp");
        result.setSuccess(true);
    }
}