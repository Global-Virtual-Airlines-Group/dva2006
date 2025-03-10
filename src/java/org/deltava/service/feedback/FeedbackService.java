// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.feedback;

import org.json.JSONObject;

import org.deltava.beans.*;

import org.deltava.service.WebService;

import org.deltava.util.JSONUtils;
import org.deltava.util.StringUtils;

/**
 * An abstract Web Service for common feedback capabilities.
 * @author Luke
 * @version 11.6
 * @since 11.6
 */

abstract class FeedbackService extends WebService {

	/**
	 * Renders a Feedback score and comments into a JSON object.
	 * @param fb a FeedbackBean
	 * @param includeComments TRUE if comments should be rendered, otherwise FALSE
	 * @return a JSONObject with the rendered results
	 */
	protected static JSONObject renderScore(FeedbackBean fb, boolean includeComments) {
		
		FeedbackScore sc = FeedbackScore.generate(fb);
		JSONObject jo = new JSONObject();
		jo.put("id", fb.getID());
		jo.put("size", sc.getSize());
		jo.put("avg", Math.round(sc.getAverage() * 1000) / 1000d);

		// Render comments
		fb.getFeedback().stream().filter(fc -> includeComments && !StringUtils.isEmpty(fc.getComments())).forEach(ff -> {
			JSONObject fo = new JSONObject();
			fo.put("createdOn", JSONUtils.formatDate(ff.getCreatedOn()));
			fo.put("score", ff.getScore());
			fo.put("comments", ff.getComments());
			jo.accumulate("comments", fo);
		});
		
		JSONUtils.ensureArrayPresent(jo, "comments");
		return jo;
	}

	@Override
	public boolean isSecure() {
		return true;
	}

	@Override
	public boolean isLogged() {
		return false;
	}
}