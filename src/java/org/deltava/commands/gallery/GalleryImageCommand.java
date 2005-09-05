// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.gallery;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.gallery.*;
import org.deltava.commands.*;

import org.deltava.dao.GetGallery;
import org.deltava.dao.GetPilot;
import org.deltava.dao.DAOException;

import org.deltava.security.command.GalleryAccessControl;

/**
 * A Web Site Command to display a Image Gallery Image.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GalleryImageCommand extends AbstractCommand {
	
	private static final List SCORES = Arrays.asList(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"});

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the command result
		CommandResult result = ctx.getResult();

		// Check if we are editing the image
		String opName = (String) ctx.getCmdParameter(Command.OPERATION, "read");
		boolean isEdit = ("edit".equals(opName));
		
		// Check if we're creating a new image
		if (isEdit && (ctx.getID() == 0)) {
			ctx.setAttribute("author", ctx.getUser(), REQUEST);
			
			// Check our access
			GalleryAccessControl access = new GalleryAccessControl(ctx, null);
			access.validate();
			if (!access.getCanCreate())
				throw securityException("Cannot create Gallery Image");

			// Save the access controller
			ctx.setAttribute("access", access, REQUEST);
			
			// Forward to the JSP
			result.setURL("/jsp/gallery/imageEdit.jsp");
			result.setSuccess(true);
			return;
		}
		
		Image img = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the image
			GetGallery dao = new GetGallery(con);
			img = dao.getImageData(ctx.getID());
			if (img == null)
				throw new CommandException("Cannot find Gallery image " + ctx.getID());
			
			// Get the Image author
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("author", pdao.get(img.getAuthorID()), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Get our access level and check our edit access
        GalleryAccessControl access = new GalleryAccessControl(ctx, img);
        access.validate();
        isEdit = isEdit && access.getCanEdit();
		
		// Save the image metadata and access in the request
		ctx.setAttribute("img", img, REQUEST);
		ctx.setAttribute("access", access, REQUEST);
		ctx.setAttribute("scores", SCORES, REQUEST);
		
		// Forward to the JSP
		result.setURL(isEdit ? "/jsp/gallery/imageEdit.jsp" : "/jsp/gallery/imageView.jsp");
		result.setSuccess(true);
	}
}