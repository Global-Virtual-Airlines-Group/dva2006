// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.feedback;

import static javax.servlet.http.HttpServletResponse.*;

import java.time.Instant;
import java.io.IOException;
import java.sql.Connection;

import org.json.JSONObject;

import org.deltava.beans.*;
import org.deltava.beans.stats.Tour;

import org.deltava.security.command.TourAccessControl;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.StringUtils;

/**
 * An Web Service to receive Online Event user feedback.
 * @author Luke
 * @version 11.6
 * @since 11.6
 */

public class TourFeedbackService extends FeedbackService {

	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the ID and score
		int id = StringUtils.parse(ctx.getParameter("id"), -1);
		int score = StringUtils.parse(ctx.getParameter("score"), -1);
		if ((id < 1) || (score < 0) || (score > 10))
			return SC_BAD_REQUEST;
		
		JSONObject jo = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the Tour
			GetTour tdao = new GetTour(con);
			Tour t = tdao.get(id, ctx.getDB());
			if (t == null)
				return SC_NOT_FOUND;
			
			// Check our access
			TourAccessControl ac = new TourAccessControl(ctx, t);
			ac.validate();
			if (!ac.getCanProvideFeedback())
				return SC_FORBIDDEN;
			
			// Create the feedback object
			Feedback f = new Feedback(id, Tour.class);
			f.setAuthorID(ctx.getUser().getID());
			f.setCreatedOn(Instant.now());
			f.setScore(score);
			f.setComments(ctx.getParameter("comments"));
			t.addFeedback(f);
			jo = renderScore(t, ac.getCanViewFeedback());
			
			// Write the Feedback
			SetTour ewdao = new SetTour(con);
			ewdao.write(f);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.println(jo.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}
}