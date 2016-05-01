// Copyright 2005, 2006, 2007, 2009, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.gallery;

import java.util.*;
import java.util.stream.Collectors;
import java.time.Instant;
import java.sql.Connection;

import org.deltava.beans.gallery.Image;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.GalleryAccessControl;

import org.deltava.util.*;

/**
 * A Web Site Command to view the Image Gallery.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class GalleryCommand extends AbstractViewCommand {

	private static final String[] SORT_CODE = { "I.DATE DESC", "LC DESC" };
	static final List<?> SORT_OPTS = ComboUtils.fromArray(new String[] { "Image Date", "Like Count" }, SORT_CODE);
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get/set start/count parameters
		ViewContext<Image> vc = initView(ctx, Image.class);
		if (StringUtils.arrayIndexOf(SORT_CODE, vc.getSortType()) == -1)
			vc.setSortType(SORT_CODE[0]);

		// Check for a date
		Instant imgDate = parseDateTime(ctx, "img");
		try {
			Connection con = ctx.getConnection();

			// Get the Gallery DAO
			GetGallery dao = new GetGallery(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());

			// Get the images
			if (imgDate != null)
				vc.setResults(dao.getPictureGallery(imgDate));
			else
				vc.setResults(dao.getPictureGallery(vc.getSortType(), (String) ctx.getCmdParameter(Command.OPERATION, null)));
			
			// Load the Image Authors
			Collection<Integer> IDs = vc.getResults().stream().map(Image::getAuthorID).collect(Collectors.toSet());
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Calculate access control for new Images
		GalleryAccessControl access = new GalleryAccessControl(ctx, null);
		access.validate();
		ctx.setAttribute("access", access, REQUEST);
		
		// Save sort options
		ctx.setAttribute("sortOptions", SORT_OPTS, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/gallery/imageList.jsp");
		result.setSuccess(true);
	}
}