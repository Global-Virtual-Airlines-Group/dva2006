// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;

import org.deltava.beans.Applicant;
import org.deltava.beans.Pilot;
import org.deltava.beans.testing.*;
import org.deltava.beans.system.MessageTemplate;

import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.taskman.DatabaseTask;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to purge Applicants who have not submitted a questionnaire.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ApplicantPurgeTask extends DatabaseTask {

	/**
	 * Initializes the Schedued Task.
	 */
	public ApplicantPurgeTask() {
		super("Applicant Purge", ApplicantPurgeTask.class);
	}

	/**
	 * Executes the task.
	 */
	protected void execute() {

		// Get the inactivity cutoff time
		int inactiveDays = SystemData.getInt("register.auto_reject");
		Calendar cld = Calendar.getInstance();
		cld.add(Calendar.DATE, inactiveDays * -1);
		log.info("Executing");

		try {
			// Get the DAOs
			GetQuestionnaire qdao = new GetQuestionnaire(_con);
			GetApplicant dao = new GetApplicant(_con);
			SetQuestionnaire qwdao = new SetQuestionnaire(_con);
			SetApplicant wdao = new SetApplicant(_con);

			// Get the Pilot who we are operating as
			GetPilot pdao = new GetPilot(_con);
			Pilot taskBy = pdao.getByName(SystemData.get("users.tasks_by"));

			// Get the Message Template
			GetMessageTemplate mtdao = new GetMessageTemplate(_con);
			MessageTemplate mt = mtdao.get("APPPURGE");

			// Loop through the Applicants
			List applicants = dao.getByStatus(Applicant.PENDING, "CREATED");
			for (Iterator i = applicants.iterator(); i.hasNext();) {
				Applicant a = (Applicant) i.next();
				log.info("Checking " + a.getName());

				// Get the Questionnaire for the applicant
				Examination ex = qdao.getByApplicantID(a.getID());
				if (ex.getStatus() == Test.NEW) {
					if (a.getCreatedOn().before(cld.getTime())) {
						log.info("Deleting - registered on " + a.getCreatedOn() + ", cutoff = " + cld.getTime());

						// Delete the exam/applicant
						try {
							qwdao.delete(ex.getID());
							wdao.delete(a.getID());
						} catch (DAOException de) {
							log.error("Error removing " + a.getName() + " - " + de.getMessage());
						}

						// Create the message context
						MessageContext mctxt = new MessageContext();
						mctxt.setTemplate(mt);
						mctxt.addData("applicant", a);
						mctxt.addData("user", taskBy);

						// Send e-mail notification
			    		Mailer mailer = new Mailer(taskBy);
			    		mailer.setContext(mctxt);
			    		mailer.send(a);
					} else {
						log.debug("Applicant registered on " + a.getCreatedOn() + ", cutoff = " + cld.getTime());
					}
				} else {
					log.debug("Exam Status = " + Test.EXAMSTATUS[ex.getStatus()]);
				}
			}
		} catch (DAOException de) {
			log.error(de.getMessage());
		}

		log.info("Completed");
	}
}