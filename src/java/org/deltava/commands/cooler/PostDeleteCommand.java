// Copyright 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.cooler.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.*;

import org.deltava.util.*;

/**
 * A Web Site Command to delete an individual message post.
 * @author Luke
 * @version 2.7
 * @since 1.0
 */

public class PostDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the thread/post IDs
		int threadID = ctx.getID();
		int postID = StringUtils.parseHex(ctx.getParameter("op"));
		
		// Get command results
		CommandResult result = ctx.getResult();
		try {
			Connection con = ctx.getConnection();

			// Get the message thread
			GetCoolerThreads tdao = new GetCoolerThreads(con);
			MessageThread mt = tdao.getThread(threadID, true);
			if (mt == null)
				throw notFoundException("Invalid Message Thread - " + threadID);

			// Get the Channel profile
			GetCoolerChannels cdao = new GetCoolerChannels(con);
			Channel c = cdao.get(mt.getChannel());
			if (c == null)
				throw notFoundException("Invalid Channel - " + mt.getChannel());
			
	         // Check our access - only if we're reading
	         CoolerThreadAccessControl access = new CoolerThreadAccessControl(ctx);
	         access.updateContext(mt, c);
	         access.validate();
	         if (!access.getCanDelete())
	            throw securityException("Cannot delete Message Post");
	         
	         // Ensure that the post exists
	         boolean hasPost = false;
	         Collection<Message> posts = mt.getPosts();
	         for (Iterator<Message> i = posts.iterator(); !hasPost && i.hasNext(); ) {
	        	 Message msg = i.next();
	        	 if (msg.getID() == postID) {
	        		 hasPost = true;
	        		 i.remove();
	        	 }
	         }
	         
	         // If we cannot find the post, abort
	         if (!hasPost)
	        	 throw notFoundException("Invalid Message Post - " + postID);
	         
	         // Start the transaction
	         ctx.startTX();
	         
	         // If we only have one post, nuke the thread
	         SetCoolerMessage wdao = new SetCoolerMessage(con);
	         if (posts.size() == 0) {
	        	 wdao.delete(threadID);
	        	 
	        	 // Set status and result
	        	 ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);
	        	 result.setURL("/jsp/cooler/threadUpdate.jsp");
	         } else {
	        	 wdao.delete(threadID, postID);
	        	 wdao.synchThread(mt);
	        	 result.setURL("thread", null, threadID);
	        	 result.setType(ResultType.REDIRECT);
	         }
	         
	         // Commit the transaction
	         ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		result.setSuccess(true);
	}
}