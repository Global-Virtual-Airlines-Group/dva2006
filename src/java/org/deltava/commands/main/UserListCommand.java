package org.deltava.commands.main;

import org.deltava.commands.*;

import org.deltava.security.UserPool;

/**
 * A web site command to list logged in users.
 * @author Luke
 * @version 1.0
 * @since 1.0
 * Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
 */

public class UserListCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {

        // Save the users in the request
        ctx.setAttribute("pilots", UserPool.getPilots(), REQUEST);

        // Forward to the JSP
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/pilot/onlineUsers.jsp");
        result.setSuccess(true);
    }
}