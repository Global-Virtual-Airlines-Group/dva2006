// Copyright 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 11.1
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
		
		int registerTimeout = SystemData.getInt("registration.captcha_timeout", 72);
		int purgeTimeout = SystemData.getInt("registration.purge_timeout", 72);
		Instant now = Instant.now();
		try {
			Connection con = ctx.getConnection();
			
			// Purge auto-rejected junk
			GetApplicant adao = new GetApplicant(con);
			SetApplicant awdao = new SetApplicant(con);
			Collection<Applicant> apps = adao.getAutoRejected(registerTimeout + purgeTimeout);
			for (Applicant a : apps) {
				ctx.startTX();
				Duration d = Duration.between(a.getCreatedOn(), now);
				log.warn("Automatically deleting {} for CAPTCHA failure after {} hours", a.getName(), Long.valueOf(d.toHours()));
				awdao.delete(a.getID());
				ctx.commitTX();
			}
			
			// Get pending connections
			apps = adao.getByStatus(ApplicantStatus.PENDING, "CREATED");
			for (Applicant a : apps) {
				Duration d = Duration.between(a.getCreatedOn(), now);
				if (a.getHasCAPTCHA() || (d.toHours() < registerTimeout)) continue;
				
				// Reject the applicant
				log.warn("Automatically rejecting {} for CAPTCHA failure after {} hours", a.getName(), Long.valueOf(d.toHours()));
				a.setStatus(ApplicantStatus.REJECTED);
				a.setAutoReject(true);
				ctx.startTX();
				
				// Update status
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
			log.atError().withThrowable(de).log(de.getMessage());
		} finally {
			ctx.release();
		}

		log.info("Completed");
	}
}