// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.util.*;

import org.deltava.beans.Pilot;

import org.deltava.commands.*;

import org.deltava.comparators.PilotComparator;
import org.deltava.security.UserPool;

import org.deltava.util.*;

/**
 * A Web Site Command to list logged in users.
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
    	if (StringUtils.isEmpty(sortOpt))
    		sortOpt = PilotComparator.TYPES[PilotComparator.PILOTCODE];
    	
    	// Initialize the comparator
    	List<Pilot> users = new ArrayList<Pilot>(UserPool.getPilots());
    	Collections.sort(users, new PilotComparator(sortOpt));
    	
    	// Get maximum concurrent user data
    	ctx.setAttribute("maxUsers", new Integer(UserPool.getMaxSize()), REQUEST);
    	ctx.setAttribute("maxUserDate", UserPool.getMaxSizeDate(), REQUEST);

        // Get and save the users in the request
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