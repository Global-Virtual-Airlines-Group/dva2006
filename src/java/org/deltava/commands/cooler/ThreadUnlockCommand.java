// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.sql.Connection;

import org.deltava.beans.cooler.*;
import org.deltava.commands.*;

import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

/**
 * A Web Site Command to unlock or unhide Water Cooler message threads.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public class ThreadUnlockCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

        // Determine what operation we are performing
        String opName = (String) ctx.getCmdParameter(Command.OPERATION, "unlock");
        boolean isUnlock = opName.equals("unlock");
        boolean isUnhide = (isUnlock || opName.equals("unhide"));
		
        MessageThread thread = null;
		try {
			Connection con = ctx.getConnection();
			
            // Get the Message Thread
            GetCoolerThreads tdao = new GetCoolerThreads(con);
            thread = tdao.getThread(ctx.getID());
            if (thread == null)
                throw notFoundException("Unknown Message Thread - " + ctx.getID());
			
            // Get the channel profile
            GetCoolerChannels cdao = new GetCoolerChannels(con);
            Channel ch = cdao.get(thread.getChannel());
            if (ch == null)
            	throw notFoundException("Unknown Channel - " + thread.getChannel());
            
            // Check user access
            CoolerThreadAccessControl ac = new CoolerThreadAccessControl(ctx);
            ac.updateContext(thread, ch);
            ac.validate();
            
            // Check our access level
            if (!ac.getCanUnlock())
                throw securityException("Cannot moderate Message Thread " + ctx.getID());
            
			// Create the status update bean
			ThreadUpdate upd = new ThreadUpdate(thread.getID());
			upd.setAuthorID(ctx.getUser().getID());
			upd.setMessage("Message Thread unlocked");

			// Start a transaction
			ctx.startTX();
            
            // Get the DAO to lock/hide the message thread
            SetCoolerMessage wdao = new SetCoolerMessage(con);
            wdao.moderateThread(thread.getID(), !isUnhide, !isUnlock);
            wdao.write(upd);

            // Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
        // Save the thread and operations in the request
        ctx.setAttribute("thread", thread, REQUEST);
        ctx.setAttribute("isUnhidden", Boolean.valueOf(isUnhide), REQUEST);
        ctx.setAttribute("isUnlocked", Boolean.valueOf(isUnlock), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/cooler/threadUpdate.jsp");
		result.setSuccess(true);
	}
}