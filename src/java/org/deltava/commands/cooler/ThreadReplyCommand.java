// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.cooler;

import java.sql.Connection;

import org.deltava.beans.cooler.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

/**
 * A Web Site Command to handle Water Cooler response posting.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ThreadReplyCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {
        
        try {
            Connection con = ctx.getConnection();
            
            // Get the Message Thread
            GetCoolerThreads tdao = new GetCoolerThreads(con);
            MessageThread thread = tdao.getThread(ctx.getID());
            if (thread == null)
                throw new CommandException("Unknown Message Thread - " + ctx.getID());
            
            // Get the channel profile
            GetCoolerChannels cdao = new GetCoolerChannels(con);
            Channel ch = cdao.get(thread.getChannel());
            
            // Check user access
            CoolerThreadAccessControl ac = new CoolerThreadAccessControl(ctx);
            ac.updateContext(thread, ch);
            ac.validate();
            
            // Check our access level
            if (!ac.getCanReply())
                throw new CommandSecurityException("Cannot post in Message Thread " + ctx.getID());
            
            // Create the new reply bean
            Message msg = new Message(ctx.getUser().getID());
            msg.setThreadID(thread.getID());
            msg.setRemoteAddr(ctx.getRequest().getRemoteAddr());
            msg.setRemoteHost(ctx.getRequest().getRemoteHost());
            msg.setBody(ctx.getParameter("msgText"));
            
            // Add the response to the thread
            thread.addPost(msg);
            
            // Start the transaction
            ctx.startTX();
            
            // Get the DAO and write the new response to the database
            SetCoolerMessage wdao = new SetCoolerMessage(con);
            wdao.writeMessage(msg);
            wdao.synchThread(thread);
            
            // Commit the transaction
            ctx.commitTX();
            
            // Save the thread in the request
            ctx.setAttribute("thread", thread, REQUEST);
            ctx.setAttribute("reply", msg, REQUEST);
        } catch (DAOException de) {
        	ctx.rollbackTX();
            throw new CommandException(de);
        } finally {
            ctx.release();
        }

        // Forward to the JSP
        CommandResult result = ctx.getResult();
        result.setType(CommandResult.REQREDIRECT);
        result.setURL("/jsp/cooler/threadUpdate.jsp");
        result.setSuccess(true);
    }
}