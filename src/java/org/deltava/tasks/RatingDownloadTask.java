// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.Collection;

import org.deltava.beans.servinfo.PilotRating;

import org.deltava.dao.*;
import org.deltava.dao.http.GetATOData;

import org.deltava.taskman.*;

/**
 * A Scheduled Task to download VATSIM Pilot ratings.
 * @author Luke
 * @version 7.2
 * @since 7.2
 */

public class RatingDownloadTask extends Task {

	/**
	 * Initializes the Task. 
	 */
	public RatingDownloadTask() {
		super("Update Pilot Ratings", RatingDownloadTask.class);
	}

	/**
	 * Executes the Task.
	 * @param ctx the TaskContext
	 */
	@Override
	protected void execute(TaskContext ctx) {
		try {
			GetATOData atodao = new GetATOData();
			atodao.setReadTimeout(35000);
			atodao.getInstructors(); // will update cache
			atodao.reset();
			
			Collection<PilotRating> ratings = atodao.getCertificates();
			if (!ratings.isEmpty()) {
				SetOnlineTime wdao = new SetOnlineTime(ctx.getConnection());
				wdao.writeRatings(ratings);
			}
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}
	}
}