// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.gallery;

import java.sql.Connection;

import org.deltava.beans.gallery.Image;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display a Plot's Image Gallery content. 
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class MyImagesCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the ID
		CommandResult result = ctx.getResult();
		int pilotID = ctx.getID();
		if (pilotID == 0) {
			result.setURL("myimgs", null, ctx.getUser().getID());
			result.setType(ResultType.REDIRECT);
			result.setSuccess(true);
			return;
		}
		
		// Get/set start/count parameters
		ViewContext<Image> vc = initView(ctx, Image.class);
		try {
			Connection con = ctx.getConnection();
			
			// Load the images
			GetGallery dao = new GetGallery(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getUserGallyer(pilotID));
			
			// Load the pilot
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilot", pdao.get(pilotID), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		result.setURL("/jsp/gallery/myImageList.jsp");
		result.setSuccess(true);
	}
}