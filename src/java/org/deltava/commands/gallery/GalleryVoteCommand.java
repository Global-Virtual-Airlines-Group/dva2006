// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.gallery;

import java.sql.Connection;

import org.deltava.beans.gallery.*;
import org.deltava.commands.*;

import org.deltava.dao.GetGallery;
import org.deltava.dao.SetGalleryImage;
import org.deltava.dao.DAOException;

import org.deltava.security.command.GalleryAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command for voting on Image Gallery images.
 * @author Luke
 * @version 1.0
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
		boolean isCooler = (ctx.getCmdParameter(Command.OPERATION, null) != null);
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the Gallery Image
			GetGallery rdao = new GetGallery(con);
			Image img = rdao.getImageData(ctx.getID());
			if (img == null)
				throw new CommandException("Unknown Image Gallery image - " + ctx.getID());
			
			// Check our access level
	        GalleryAccessControl access = new GalleryAccessControl(ctx, img);
	        access.validate();
	        if (!access.getCanVote())
	        	throw new CommandSecurityException("Cannot Vote for Image " + ctx.getID());

	        // Create our vote
	        Vote v = new Vote(ctx.getUser(), Integer.parseInt(ctx.getParameter("score")), ctx.getID());
	        
	        // Get the write DAO and save the vote
	        SetGalleryImage wdao = new SetGalleryImage(con);
			wdao.write(v);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Figure out the command and ID to redirect to
		String cmdName = (isCooler) ? "thread" : "image";
		int id = (isCooler) ? StringUtils.parseHex((String) ctx.getCmdParameter(Command.OPERATION, null)) : ctx.getID();

		// Redisplay the image
		CommandResult result = ctx.getResult();
		result.setURL(cmdName, null, id);
		result.setSuccess(true);
	}
}