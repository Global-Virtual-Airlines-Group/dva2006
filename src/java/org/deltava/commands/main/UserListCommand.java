// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.main;

import java.util.*;

import org.deltava.commands.*;

import org.deltava.comparators.PilotComparator;
import org.deltava.security.UserPool;
import org.deltava.util.ComboUtils;

/**
 * A web site command to list logged in users.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class UserListCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {
    	
    	// Get the sort type
    	String sortOpt = ctx.getParameter("sortOpt");
    	if (sortOpt == null)
    		sortOpt = PilotComparator.TYPES[PilotComparator.PILOTCODE];
    	
    	// Initialize the comparator
    	Set users = new TreeSet(new PilotComparator(sortOpt));

        // Get and save the users in the request
    	users.addAll(UserPool.getPilots());
        ctx.setAttribute("pilots", users, REQUEST);
        
        // Save combobox options
        ctx.setAttribute("sortOpt", sortOpt, REQUEST);
        ctx.setAttribute("sortOptions", ComboUtils.fromArray(PilotComparator.TYPES), REQUEST);

        // Forward to the JSP
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/pilot/onlineUsers.jsp");
        result.setSuccess(true);
    }
}