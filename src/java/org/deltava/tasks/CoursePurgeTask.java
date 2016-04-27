// Copyright 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.time.*;
import java.sql.Connection;

import org.deltava.beans.academy.*;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to suspend inactive Flight Academy Courses. 
 * @author Luke
 * @version 7.0
 * @since 6.3
 */

public class CoursePurgeTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public CoursePurgeTask() {
		super("Inactive Course Suspend", CoursePurgeTask.class);
	}

	/**
	 * Executes the Task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		
		int purgeDays = SystemData.getInt("academy.coursePurge", 240);
		Instant pd = ZonedDateTime.now().minusDays(purgeDays).toInstant();
		log.warn("Purging Flight Academy Courses inactive since before " + pd);
		
		try {
			Connection con = ctx.getConnection();
			GetAcademyCourses cdao = new GetAcademyCourses(con);
			SetAcademy wdao = new SetAcademy(con); SetExam ewdao = new SetExam(con);
			ctx.startTX();
			
			// Load the inactive courses
			GetAcademyInactivity idao = new GetAcademyInactivity(con);
			Map<Integer, Integer> courseIDs = idao.getInactiveCourses(purgeDays, SystemData.get("airline.db"));
			
			// Invalidate the courses
			for (Map.Entry<Integer, Integer> me : courseIDs.entrySet()) {
				Course c = cdao.get(me.getKey().intValue());
				c.setStatus(Status.ABANDONED);
				
				// Create a status entry
				CourseComment cc = new CourseComment(c.getID(), ctx.getUser().getID());
				cc.setCreatedOn(Instant.now());
				cc.setText("Automatically abandoned after no activity in " + me.getValue() + " days");
				
				// Update the course and comments
				ewdao.deleteCheckRides(c.getID());
				wdao.comment(cc);
				wdao.write(c);
			}
			
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		log.info("Processing Complete");
	}
}