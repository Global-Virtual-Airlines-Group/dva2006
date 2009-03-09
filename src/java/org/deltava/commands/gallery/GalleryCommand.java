// Copyright 2005, 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.gallery;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.gallery.Image;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.GalleryAccessControl;

import org.deltava.util.*;

/**
 * A Web Site Command to view the Image Gallery.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public class GalleryCommand extends AbstractViewCommand {

	private static final String[] SORT_CODE = { "I.DATE DESC", "VC DESC", "SC DESC, VC DESC" };
	static final List SORT_OPTS = ComboUtils.fromArray(new String[] { "Image Date", "Feedback Count", "Average Score" }, SORT_CODE);
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get/set start/count parameters
		ViewContext vc = initView(ctx);
		if (StringUtils.arrayIndexOf(SORT_CODE, vc.getSortType()) == -1)
			vc.setSortType(SORT_CODE[0]);

		// Check for a date
		Date imgDate = parseDateTime(ctx, "img");

		// Save sort options
		ctx.setAttribute("sortOptions", SORT_OPTS, REQUEST);
		try {
			Connection con = ctx.getConnection();

			// Get the Gallery DAO
			GetGallery dao = new GetGallery(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());

			// Get the images
			Collection<Image> results = null; 
			if (imgDate != null)
				results = dao.getPictureGallery(imgDate);
			else
				results = dao.getPictureGallery(vc.getSortType(), (String) ctx.getCmdParameter(Command.OPERATION, null));
			
			// Validate our access and get author IDs
			Collection<Integer> authorIDs = new HashSet<Integer>();
			for (Iterator<Image> i = results.iterator(); i.hasNext();) {
				Image img = i.next();
				authorIDs.add(new Integer(img.getAuthorID()));
			}

			// Save the results
			vc.setResults(results);

			// Load the Image Authors
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(authorIDs, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Calculate access control for new Images
		GalleryAccessControl access = new GalleryAccessControl(ctx, null);
		access.validate();
		ctx.setAttribute("access", access, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/gallery/imageList.jsp");
		result.setSuccess(true);
	}
}