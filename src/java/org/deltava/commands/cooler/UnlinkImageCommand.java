// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.Map;
import java.sql.Connection;

import org.deltava.beans.cooler.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

import org.deltava.util.*;

/**
 * A Web Site Command to unlink a Water Cooler linked image. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class UnlinkImageCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the sequence number
		int seq = StringUtils.parse((String) ctx.getCmdParameter(OPERATION, "0"), 0); 
		try {
			Connection con = ctx.getConnection();
			
			// Get the Message Thread
			GetCoolerThreads tdao = new GetCoolerThreads(con);
			MessageThread mt = tdao.getThread(ctx.getID());
			if (mt == null)
				throw notFoundException("Unknown Message Thread - " + ctx.getID());
			
			// Get the Channel
			GetCoolerChannels chdao = new GetCoolerChannels(con);
			Channel ch = chdao.get(mt.getChannel());
			if (ch == null)
				throw notFoundException("Unknown Channel - " + mt.getChannel());
			
			// Get the linked images
			GetCoolerLinks ldao = new GetCoolerLinks(con);
			Map<Integer, LinkedImage> imgs = CollectionUtils.createMap(ldao.getURLs(mt.getID()), "ID");
			LinkedImage imgURL = imgs.get(new Integer(seq));
			if (imgURL == null)
				throw notFoundException("Unknown Linked Image sequence - " + seq);
			
			// Validate our access
			CoolerThreadAccessControl access = new CoolerThreadAccessControl(ctx);
			access.updateContext(mt, ch);
			access.validate();
			if (!access.getCanDelete() && !access.getCanLock() && !access.getCanUnlock())
				throw securityException("Cannot Delete Linked Image");
			
			// Create the status message
			ThreadUpdate upd = new ThreadUpdate(mt.getID());
			upd.setAuthorID(ctx.getUser().getID());
			upd.setMessage("Removed link to " + imgURL.getURL());

			// Start a transaction
			ctx.startTX();
			
			// Write the status message
			SetCoolerMessage mwdao = new SetCoolerMessage(con);
			mwdao.write(upd);
			
			// Delete the image
			SetCoolerLinks wdao = new SetCoolerLinks(con);
			wdao.delete(mt.getID(), seq);
			
			// Commit
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward back to the thread
		CommandResult result = ctx.getResult();
		result.setURL("thread", null, ctx.getID());
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}