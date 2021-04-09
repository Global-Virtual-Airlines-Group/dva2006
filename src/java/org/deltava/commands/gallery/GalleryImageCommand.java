// Copyright 2005, 2006, 2012, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.gallery;

import java.sql.Connection;

import org.deltava.beans.FileUpload;
import org.deltava.beans.gallery.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.GalleryAccessControl;

/**
 * A Web Site Command to display a Image Gallery Image.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class GalleryImageCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the image.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {

		Image img = null;
		boolean isNew = (ctx.getID() == 0);
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the image if we're editing an existing image
			if (!isNew) {
				GetGallery rdao = new GetGallery(con);
				img = rdao.getImageData(ctx.getID(), ctx.getDB());
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
				if (ctx.isUserInRole("Fleet"))
					img.setFleet(Boolean.valueOf(ctx.getParameter("isFleet")).booleanValue());
			} else {
				// Check our access
				GalleryAccessControl access = new GalleryAccessControl(ctx, null);
				access.validate();
				if (!access.getCanCreate())
					throw securityException("Cannot add Gallery Image");

				img = new Image(ctx.getParameter("title"), ctx.getParameter("desc"));
				img.setAuthorID(ctx.getUser().getID());
				if (ctx.isUserInRole("Fleet"))
					img.setFleet(Boolean.valueOf(ctx.getParameter("isFleet")).booleanValue());

				// Get the image itself
				FileUpload imgData = ctx.getFile("img");
				if (imgData == null)
					throw new CommandException("No Attached Image", false);

				// Save the image data and load the image properties
				try {
					img.load(imgData.getBuffer());
				} catch (UnsupportedOperationException uoe) {
					throw new CommandException(uoe.getMessage(), false);
				}
			}

			// Get the write DAO and save the image
			SetGalleryImage wdao = new SetGalleryImage(con);
			if (isNew)
				wdao.write(img);
			else
				wdao.update(img);
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

	/**
	 * Callback method called when editing the image.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {

		Image img = null;
		boolean isNew = (ctx.getID() == 0);
		if (!isNew) {
			try {
				Connection con = ctx.getConnection();

				// Get the DAO and the image
				GetGallery dao = new GetGallery(con);
				img = dao.getImageData(ctx.getID(), ctx.getDB());
				if (img == null)
					throw notFoundException("Cannot find Gallery image " + ctx.getID());

				// Get the Image author
				GetPilot pdao = new GetPilot(con);
				ctx.setAttribute("author", pdao.get(img.getAuthorID()), REQUEST);
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
		} else
			ctx.setAttribute("author", ctx.getUser(), REQUEST);

		// Get our access level and check our edit access
		GalleryAccessControl access = new GalleryAccessControl(ctx, img);
		access.validate();
		if (isNew && !access.getCanCreate())
			throw securityException("Cannot create Gallery Image");
		else if (!access.getCanEdit())
			throw securityException("Cannot edit Gallery Image");

		// Save the image metadata and access in the request
		ctx.setAttribute("img", img, REQUEST);
		ctx.setAttribute("access", access, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/gallery/imageEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the image.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {

		Image img = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the image
			GetGallery dao = new GetGallery(con);
			img = dao.getImageData(ctx.getID(), ctx.getDB());
			if (img == null)
				throw notFoundException("Cannot find Gallery image " + ctx.getID());

			// Get the Image author
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("author", pdao.get(img.getAuthorID()), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Get our access level
		GalleryAccessControl access = new GalleryAccessControl(ctx, img);
		access.validate();

		// Save the image metadata and access in the request
		ctx.setAttribute("img", img, REQUEST);
		ctx.setAttribute("access", access, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/gallery/imageView.jsp");
		result.setSuccess(true);
	}
}