// Copyright 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.*; 
import org.deltava.beans.testing.*;

import org.deltava.dao.*;
import org.deltava.mail.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to automatically purge old Transfer Requests.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class TransferPurgeTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public TransferPurgeTask() {
		super("Transfer Request Purge", TransferPurgeTask.class);
	}

	/**
	 * Executes the task.
	 */
	protected void execute(TaskContext ctx) {
		log.info("Starting");
		int purgeInterval = SystemData.getInt("users.transfer_max", 30);

		try {
			Connection con = ctx.getConnection();
			GetTransferRequest txdao = new GetTransferRequest(con);
			Collection<TransferRequest> oldTX = txdao.getAged(purgeInterval);
			
			// Get the Message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			MessageTemplate mt = mtdao.get("XFERREJECT");
			
			// Get the Pilot read DAO
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			Pilot taskBy = pdao.getByCode(SystemData.get("users.tasks_by"));
			
			// Loop through the old transfers
			GetExam exdao = new GetExam(con);
			SetExam exwdao = new SetExam(con);
			SetStatusUpdate swdao = new SetStatusUpdate(con);
			SetTransferRequest txwdao = new SetTransferRequest(con);
			for (Iterator<TransferRequest> i = oldTX.iterator(); i.hasNext(); ) {
				TransferRequest tx = i.next();
				Pilot p = pdao.get(tx.getID());

				// Make a status update
				StatusUpdate upd = new StatusUpdate(tx.getID(), StatusUpdate.COMMENT);
				upd.setAuthorID(taskBy.getID());
				upd.setDescription("Transfer to " + tx.getEquipmentType() + " program purged after " + purgeInterval + " days");
				
				// Get the check ride (if any) and then delete
				CheckRide cr = exdao.getCheckRide(tx.getCheckRideID());
				if ((cr == null) || (cr.getStatus() == Test.NEW)) {
					ctx.startTX();
					
					// Delete the checkride and the transfer request
					exwdao.delete(cr);
					txwdao.delete(tx.getID());
					swdao.write(upd);
					
					// Commit the transaction
					ctx.commitTX();
					
					// Log the deletion
					log.warn("Deleting transfer to " + tx.getEquipmentType() + " by " + p.getName() + " after " + purgeInterval + " days");
					
					// Create the e-mail message
					MessageContext mctxt = new MessageContext();
					mctxt.setTemplate(mt);
					mctxt.addData("user", taskBy);
					mctxt.addData("pilot", p);
					mctxt.addData("txReq", tx);
					mctxt.addData("rejectComments", "Transfer automatically deleted after " + purgeInterval + " days");

					// Send a notification message
					Mailer mailer = new Mailer(taskBy);
					mailer.setContext(mctxt);
					mailer.send(p);
				}
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