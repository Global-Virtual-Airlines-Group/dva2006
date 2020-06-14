// Copyright 2015, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.time.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.academy.*;
import org.deltava.beans.schedule.Aircraft;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to suspend inactive Flight Academy Courses. 
 * @author Luke
 * @version 9.0
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
			GetAcademyCertifications ccdao = new GetAcademyCertifications(con);
			GetAcademyCourses cdao = new GetAcademyCourses(con); GetPilot pdao = new GetPilot(con);
			SetAcademy wdao = new SetAcademy(con); SetExam ewdao = new SetExam(con); SetPilot pwdao = new SetPilot(con);
			SetStatusUpdate upwdao = new SetStatusUpdate(con);
			
			// Load academy aircraft
			GetAircraft acdao = new GetAircraft(con);
			Collection<String> academyEQ = acdao.getAll().stream().filter(ac -> ac.getAcademyOnly()).map(Aircraft::getName).collect(Collectors.toSet());
			
			// Start transaction
			ctx.startTX();
			
			// Load the inactive courses
			GetAcademyInactivity idao = new GetAcademyInactivity(con);
			Map<Integer, Integer> courseIDs = idao.getInactiveCourses(purgeDays, SystemData.get("airline.db"));
			
			// Invalidate the courses
			for (Map.Entry<Integer, Integer> me : courseIDs.entrySet()) {
				Course c = cdao.get(me.getKey().intValue());
				Certification cert = ccdao.get(c.getName());
				c.setStatus(Status.ABANDONED);
				
				// Check if we need to remove ratings
				Pilot p  = pdao.get(c.getPilotID());
				Collection<String> rRatings = cert.getRideEQ().stream().filter(r -> academyEQ.contains(r) && p.hasRating(r)).collect(Collectors.toSet());
				if (!rRatings.isEmpty()) {
					p.removeRatings(rRatings);
					pwdao.write(p);
					
					StatusUpdate upd = new StatusUpdate(c.getPilotID(), UpdateType.ACADEMY);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription("Ratings removed: " + StringUtils.listConcat(academyEQ, ", ") + " for " + c.getName());
					upwdao.write(upd);
				}
				
				// Create a status entry
				CourseComment cc = new CourseComment(c.getID(), ctx.getUser().getID());
				cc.setBody("Automatically abandoned after no activity in " + me.getValue() + " days");
				
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