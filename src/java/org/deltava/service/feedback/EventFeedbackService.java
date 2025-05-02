// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.feedback;

import static javax.servlet.http.HttpServletResponse.*;

import java.time.Instant;
import java.io.IOException;
import java.sql.Connection;

import org.json.JSONObject;

import org.deltava.beans.*;
import org.deltava.beans.event.Event;

import org.deltava.security.command.EventAccessControl;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.StringUtils;

/**
 * A Web Service to receive Online Event user feedback.
 * @author Luke
 * @version 11.6
 * @since 11.6
 */

public class EventFeedbackService extends FeedbackService {

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
			
			// Get the Event
			GetEvent edao = new GetEvent(con);
			Event e = edao.get(id);
			if (e == null)
				return SC_NOT_FOUND;
			
			// Check our access
			EventAccessControl ac = new EventAccessControl(ctx, e);
			ac.validate();
			if (!ac.getCanProvideFeedback())
				return SC_FORBIDDEN;
			
			// Create the feedback object
			Feedback f = new Feedback(id, Event.class);
			f.setAuthorID(ctx.getUser().getID());
			f.setCreatedOn(Instant.now());
			f.setScore(score);
			f.setComments(ctx.getParameter("comments"));
			e.addFeedback(f);
			jo = renderScore(e, ac.getCanViewFeedback());
			
			// Write the Feedback
			SetEvent ewdao = new SetEvent(con);
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