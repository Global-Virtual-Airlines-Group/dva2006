// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.time.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.testing.Examination;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to purge Applicants with invalid CAPTCHAs
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class ApplicantPurgeTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public ApplicantPurgeTask() {
		super("Applicant Purge", ApplicantPurgeTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {
		log.info("Executing");
		
		int registerTimeout = SystemData.getInt("registration.captcha_timeout", 72); Instant now = Instant.now();
		try {
			Connection con = ctx.getConnection();
			
			// Get pending connections
			GetApplicant adao = new GetApplicant(con);
			Collection<Applicant> apps = adao.getByStatus(ApplicantStatus.PENDING, "CREATED");
			for (Applicant a : apps) {
				Duration d = Duration.between(a.getCreatedOn(), now);
				if (a.getHasCAPTCHA() || (d.toHours() < registerTimeout)) continue;
				
				// Reject the applicant
				log.warn(String.format("Automatically rejecting %s for CAPTCHA failure after %d hours", a.getName(), Long.valueOf(d.toHours())));
				a.setStatus(ApplicantStatus.REJECTED);
				ctx.startTX();
				
				// Update status
				SetApplicant awdao = new SetApplicant(con);
				awdao.write(a);
				
				// Delete questionnaire if it exists
				GetQuestionnaire qdao = new GetQuestionnaire(con);
				Examination ex = qdao.getByApplicantID(a.getID());
				if (ex != null) {
					SetQuestionnaire qwdao = new SetQuestionnaire(con);
					qwdao.delete(ex.getID());
				}
				
				// Delete address validation
				SetAddressValidation avdao = new SetAddressValidation(con);
				avdao.delete(a.getID());
				ctx.commitTX();
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		log.info("Completed");
	}
}