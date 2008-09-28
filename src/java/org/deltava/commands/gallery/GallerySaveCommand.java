// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.gallery;

import java.sql.Connection;

import org.deltava.beans.FileUpload;
import org.deltava.beans.gallery.Image;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.GalleryAccessControl;

/**
 * A Web Site Command to save Image Gallery images and metadata.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GallerySaveCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {

		// Check if we're saving a new image
		boolean isNew = (ctx.getID() == 0);

		Image img = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the image if we're editing an existing image
			if (!isNew) {
				GetGallery rdao = new GetGallery(con);
				img = rdao.getImageData(ctx.getID());
				if (img == null)
					throw notFoundException("Cannot find Gallery image " + ctx.getID());
				
				// Check our access
				GalleryAccessControl access = new GalleryAccessControl(ctx, img);
				access.validate();
				if (!access.getCanEdit())
					throw securityException("Cannot edit Gallery image");
				
				// Create setName() and setDescription()
				img.setName(ctx.getParameter("title"));
				img.setDescription(ctx.getParameter("desc"));
				if (ctx.isUserInRole("Gallery") || ctx.isUserInRole("Fleet"))
					img.setFleet(Boolean.valueOf(ctx.getParameter("isFleet")).booleanValue());
			} else {
				// Check our access
				GalleryAccessControl access = new GalleryAccessControl(ctx, null);
				access.validate();
				if (!access.getCanCreate())
					throw securityException("Cannot add Gallery Image");
				
				img = new Image(ctx.getParameter("title"), ctx.getParameter("desc"));
				img.setAuthorID(ctx.getUser().getID());
				if (ctx.isUserInRole("Gallery") || ctx.isUserInRole("Fleet"))
					img.setFleet(Boolean.valueOf(ctx.getParameter("isFleet")).booleanValue());
				
				// Get the image itself
				FileUpload imgData = ctx.getFile("img");
				if (imgData == null) {
					CommandException ce = new CommandException("No Attached Image");
					ce.setLogStackDump(false);
					throw ce;
				}

				// Save the image data and load the image properties
				try {
					img.load(imgData.getBuffer());
				} catch (UnsupportedOperationException uoe) {
					CommandException ce = new CommandException(uoe.getMessage());
					ce.setLogStackDump(false);
					throw ce;
				}
			}
			
			// Get the write DAO and save the image
			SetGalleryImage wdao = new SetGalleryImage(con);
			if (isNew) {
				wdao.write(img);
			} else {
				wdao.update(img);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the new image
		CommandResult result = ctx.getResult();
		result.setURL("image", null, img.getID());
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}