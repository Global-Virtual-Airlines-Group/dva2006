// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;

import org.deltava.dao.*;
import org.deltava.mail.*;
import org.deltava.taskman.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to display equipment program status.
 * @author Luke
 * @version 3.7
 * @since 3.7
 */

public class ProgramSummaryTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public ProgramSummaryTask() {
		super("Equipment Program Summary", ProgramSummaryTask.class);
	}

	/**
	 * Executes the Task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		try {
			Connection con = ctx.getConnection();
			
			// Get the equipment programs
			GetEquipmentType eqdao = new GetEquipmentType(con);
			Collection<EquipmentType> allEQ = eqdao.getActive();
			
			// Load members of the HR group/Senior Staff
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			Collection<Pilot> sStaff = new LinkedHashSet<Pilot>(pdao.getByRole("HR", SystemData.get("airline.db")));
			sStaff.addAll(pdao.getByRole("Senior Staff", SystemData.get("airline.db")));
			
			// Get DAOs
			GetExam exdao = new GetExam(con);
			GetPilotRecognition rdao = new GetPilotRecognition(con);
			GetTransferRequest txdao = new GetTransferRequest(con);
			GetFlightReportRecognition prdao = new GetFlightReportRecognition(con);
			
			// Build the mailer
			Mailer mailer = new Mailer(ctx.getUser());
			
			// Loop through the equipment programs
			String date = StringUtils.format(new Date(), SystemData.get("time.date_format"));
			StringBuilder ssMsgBuf = new StringBuilder();
			for (EquipmentType eq : allEQ) {
				log.info("Generating program summary for " + eq.getName() + " program");
				
				StringBuilder msgBuf = new StringBuilder("Program Summary for" + eq.getName());
				msgBuf.append(" Program on " + date);
				msgBuf.append("\n\n");

				// Get promotion queue size
				long promoQueueSize = rdao.hasPromotionQueue(eq.getName());
				if (promoQueueSize > 0)
					msgBuf.append("There are " + promoQueueSize + " pilots awaiting promotion to Captain.\n");
				
				// Get pending check rides
				int crQueueSize = prdao.getCheckRideQueueSize(eq.getName());
				if (crQueueSize > 0)
					msgBuf.append("There are " + crQueueSize + " " + eq.getName() + " check rides awaiting grading.\n");
				
				// Get exam queue size
				int examQueueSize = exdao.getSubmitted().size();
				if (examQueueSize > 0)
					msgBuf.append("There are " + examQueueSize + " pending Examinations to score.\n");
					
				// Get transfer queue size.
				int txQueueSize = txdao.getCount(eq.getName());
				if (txQueueSize > 0)
					msgBuf.append("There are " + txQueueSize + " pending Transfer Requests into the " + eq.getName() + " program.\n");
				
				// Get pending PIREPs
				int pirepQueueSize = prdao.getDisposalQueueSize(eq.getName());
				if (pirepQueueSize > 0)
					msgBuf.append("There are " + txQueueSize + " pending Flight Reports for aircraft in the " + eq.getName() + " program.\n");
				
				// Append the message to the airline-wide message
				ssMsgBuf.append(msgBuf);
				ssMsgBuf.append('\n');
				
				// Find the staff members
				Collection<Pilot> eqStaff = new LinkedHashSet<Pilot>();
				eqStaff.add(pdao.get(eq.getCPID()));
				eqStaff.addAll(pdao.getPilotsByEQ(eq, null, true, Rank.ACP));
				eqStaff.removeAll(sStaff);
				
				// Send to the equipment staff
				MessageContext mctxt = new MessageContext();
				mctxt.setSubject(eq.getName() + " Program Summary for " + date);
				mctxt.setBody(msgBuf.toString());
				mailer.setContext(mctxt);
				mailer.send(eqStaff);
			}
			
			// Send airline-wide summary
			MessageContext mctxt = new MessageContext();
			mctxt.setSubject(SystemData.get("airline.name") + " Program Summary for " + date);
			mctxt.setBody(ssMsgBuf.toString());
			mailer.send(sStaff);
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		log.info("Processing Complete");
	}
}