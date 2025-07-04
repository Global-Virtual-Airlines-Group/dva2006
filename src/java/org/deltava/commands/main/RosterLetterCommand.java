// Copyright 2005, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.util.*;

import org.deltava.beans.Pilot;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A web site command to view pilot statistics by letter.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class RosterLetterCommand extends AbstractViewCommand {

    private static final List<?> LETTERS = Arrays.asList(new String [] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"});

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    @Override
	public void execute(CommandContext ctx) throws CommandException {
        
        // Get the letter to display
        String letter = (String) ctx.getCmdParameter(Command.OPERATION, null);
        ctx.setAttribute("letters", LETTERS, REQUEST);
        
        // If no letter specified, just redirect to the JSP
        if ((letter == null) || !LETTERS.contains(letter)) {
            CommandResult result = ctx.getResult();
            result.setURL("/jsp/roster/letterRoster.jsp");
            result.setSuccess(true);
            return;
        }
        
        ViewContext<Pilot> vc = initView(ctx, Pilot.class);
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