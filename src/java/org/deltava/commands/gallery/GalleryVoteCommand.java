// Copyright 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.gallery;

import java.sql.Connection;

import org.deltava.beans.gallery.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.GalleryAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command for voting on Image Gallery images.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class GalleryVoteCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check if we are posting from the Water Cooler
		boolean isCooler = (ctx.getCmdParameter(OPERATION, null) != null);
		try {
			Connection con = ctx.getConnection();
			
			// Get the Gallery Image
			GetGallery rdao = new GetGallery(con);
			Image img = rdao.getImageData(ctx.getID());
			if (img == null)
				throw notFoundException("Unknown Image Gallery image - " + ctx.getID());
			
			// Check our access level
	        GalleryAccessControl access = new GalleryAccessControl(ctx, img);
	        access.validate();
	        if (!access.getCanVote())
	        	throw securityException("Cannot Vote for Image " + ctx.getID());

	        // Create our vote and save it
	        Vote v = new Vote(ctx.getUser(), StringUtils.parse(ctx.getParameter("score"), 0), ctx.getID());
	        if (v.getScore() > 0) {
	        	SetGalleryImage wdao = new SetGalleryImage(con);
	        	wdao.write(v);
	        }
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Figure out the command and ID to redirect to
		String cmdName = (isCooler) ? "thread" : "image";
		int id = (isCooler) ? StringUtils.parseHex((String) ctx.getCmdParameter(OPERATION, null)) : ctx.getID();

		// Redisplay the image
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL(cmdName, null, id);
		result.setSuccess(true);
	}
}