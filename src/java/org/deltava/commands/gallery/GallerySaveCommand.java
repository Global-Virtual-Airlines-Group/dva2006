// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.gallery;

import java.sql.Connection;

import org.deltava.beans.FileUpload;
import org.deltava.beans.gallery.Image;

import org.deltava.commands.*;

import org.deltava.dao.GetGallery;
import org.deltava.dao.SetGalleryImage;
import org.deltava.dao.DAOException;

import org.deltava.util.ImageInfo;

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
		boolean isNew = "new".equals(ctx.getCmdParameter(OPERATION, null));

		Image img = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the image if we're editing an existing image
			if (!isNew) {
				GetGallery rdao = new GetGallery(con);
				img = rdao.getImageData(ctx.getID());
				if (img == null)
					throw new CommandException("Cannot find Gallery image " + ctx.getID());
				
				// Create setName() and setDescription()
				img.setName(ctx.getParameter("name"));
				img.setDescription(ctx.getParameter("desc"));
				if (ctx.isUserInRole("Gallery") || ctx.isUserInRole("Flleet"))
					img.setFleet("1".equals(ctx.getParameter("isFleet")));
			} else {
				img = new Image(ctx.getParameter("name").trim(), ctx.getParameter("desc").trim());
				img.setAuthorID(ctx.getUser().getID());
				if (ctx.isUserInRole("Gallery") || ctx.isUserInRole("Flleet"))
					img.setFleet("1".equals(ctx.getParameter("isFleet")));
				
				// Get the image itself
				FileUpload imgData = ctx.getFile("img");
				if (imgData == null)
					throw new CommandException("No Attached Image");

				// Get the image properties
				ImageInfo imgInfo = new ImageInfo(imgData.getBuffer());
				imgInfo.check();
				
				// Save the image dimensions
				img.setWidth(imgInfo.getWidth());
				img.setHeight(imgInfo.getHeight());
				img.setType(imgInfo.getFormat());
				
				// Save the image data
				img.load(imgData.getBuffer());
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
		result.setType(CommandResult.FORWARD);
		result.setSuccess(true);
	}
}