// Copyright 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.UserDataMap;
import org.deltava.beans.fleet.Video;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.*;

/**
 * A Web Site Command to display Fleet Academy training videos.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class VideoLibraryCommand extends AbstractViewCommand {

	private static final Logger log = Logger.getLogger(VideoLibraryCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the view start/end
		ViewContext vc = initView(ctx);

		// Calculate access for adding content
		CertificationAccessControl access = new CertificationAccessControl(ctx);
		access.validate();
		ctx.setAttribute("access", access, REQUEST);

		VideoAccessControl vAccess = null;
		Collection<Video> results = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the library
			GetVideos dao = new GetVideos(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(Math.round(vc.getCount() * 1.5f));
			results = dao.getVideos();

			// Get the authors
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Iterator<Video> i = results.iterator(); i.hasNext();) {
				Video v = i.next();
				IDs.add(new Integer(v.getAuthorID()));
			}

			// Get the author data
			GetPilot pdao = new GetPilot(con);
			GetUserData uddao = new GetUserData(con);
			UserDataMap udm = uddao.get(IDs);
			ctx.setAttribute("authors", pdao.get(udm), REQUEST);

			// Populate flight academy courses
			GetAcademyCourses cdao = new GetAcademyCourses(con);
			vAccess = new VideoAccessControl(ctx, cdao.getByPilot(ctx.getUser().getID()));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Validate our access to the results
		for (Iterator<Video> i = results.iterator(); i.hasNext();) {
			Video video = i.next();
			vAccess.updateContext(video);
			vAccess.validate();
			
			// Check that the resource exists
			if (video.getSize() == 0) {
				log.warn(video.getFullName() + " not found in file system!");
				if (!access.getCanEditVideo())
					i.remove();
			} else if (!vAccess.getCanRead())
				i.remove();
		}

		// Save the results in the view context
		vc.setResults(results);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/videoLibrary.jsp");
		result.setSuccess(true);
	}
}