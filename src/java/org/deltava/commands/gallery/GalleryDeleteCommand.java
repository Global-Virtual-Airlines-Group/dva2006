// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.gallery;

import java.sql.Connection;

import org.deltava.beans.gallery.Image;

import org.deltava.commands.*;

import org.deltava.dao.GetGallery;
import org.deltava.dao.SetGalleryImage;
import org.deltava.dao.DAOException;

import org.deltava.security.command.GalleryAccessControl;

/**
 * A Web Site Command to delete Image Gallery images.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GalleryDeleteCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {

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
	        if (!access.getCanDelete())
	        	throw securityException("Cannot Delete Image " + ctx.getID());
			
	        // Get the write DAO and delete the image
	        SetGalleryImage wdao = new SetGalleryImage(con);
			wdao.delete(ctx.getID());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/gallery/imageDelete.jsp");
		result.setSuccess(true);
	}
}