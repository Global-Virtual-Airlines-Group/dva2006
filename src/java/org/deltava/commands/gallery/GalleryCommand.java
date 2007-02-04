// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.gallery;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.cooler.*;
import org.deltava.beans.gallery.Image;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.*;

import org.deltava.util.*;

/**
 * A Web Site Command to view the Image Gallery.
 * @author Luke
 * @version 1.0
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

		// Save sort options
		ctx.setAttribute("sortOptions", SORT_OPTS, REQUEST);

		try {
			Connection con = ctx.getConnection();

			// Get the Gallery DAO
			GetGallery dao = new GetGallery(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(Math.round(vc.getCount() * 1.2f));

			// Get the images
			List<Image> results = dao.getPictureGallery(vc.getSortType(), (String) ctx.getCmdParameter(Command.OPERATION, null));
			
			// Get the Water Cooler DAOs
			GetCoolerChannels chdao = new GetCoolerChannels(con);
			GetCoolerThreads tdao = new GetCoolerThreads(con);
			
			// Validate our access and get author IDs
			Collection<Integer> authorIDs = new HashSet<Integer>();
			CoolerThreadAccessControl tAccess = new CoolerThreadAccessControl(ctx);
			for (Iterator<Image> i = results.iterator(); i.hasNext();) {
				Image img = i.next();
				if (img.getThreadID() != 0) {
					MessageThread mt = tdao.getThread(img.getThreadID(), false);
					Channel ch = chdao.get(mt.getChannel());
					tAccess.updateContext(mt, ch);
					tAccess.validate();
					if (!tAccess.getCanRead())
						i.remove();
					else
						authorIDs.add(new Integer(img.getAuthorID()));
				} else
					authorIDs.add(new Integer(img.getAuthorID()));
			}

			// Save the results
			vc.setResults(results);

			// Load the Image Authors
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(authorIDs, "PILOTS"), REQUEST);

			// Save the list of months
			ctx.setAttribute("months", dao.getMonths(), REQUEST);
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