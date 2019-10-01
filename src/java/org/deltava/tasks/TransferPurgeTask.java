// Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.hr.TransferRequest;
import org.deltava.beans.system.*; 
import org.deltava.beans.testing.*;

import org.deltava.dao.*;
import org.deltava.mail.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to automatically purge old Transfer Requests.
 * @author Luke
 * @version 8.7
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
	@Override
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
			
			// Loop through the old transfers
			GetPilot pdao = new GetPilot(con);
			GetExam exdao = new GetExam(con);
			SetExam exwdao = new SetExam(con);
			SetStatusUpdate swdao = new SetStatusUpdate(con);
			SetTransferRequest txwdao = new SetTransferRequest(con);
			for (TransferRequest tx : oldTX) {
				Pilot p = pdao.get(tx.getID());

				// Make a status update
				StatusUpdate upd = new StatusUpdate(tx.getID(), UpdateType.COMMENT);
				upd.setAuthorID(ctx.getUser().getID());
				upd.setDescription("Transfer to " + tx.getEquipmentType() + " program purged after " + purgeInterval + " days and " + tx.getCheckRideIDs().size() + " check rides");
				
				// Get the check ride (if any) and then delete
				CheckRide cr = exdao.getCheckRide(tx.getLatestCheckRideID());
				int crAge = (cr == null) ? 0 : (int) ((System.currentTimeMillis() - cr.getDate().toEpochMilli()) / 86400);
				if ((cr == null) || ((cr.getStatus() == TestStatus.NEW) && (crAge > 7))) {
					ctx.startTX();
					
					// Delete the checkride and the transfer request
					if (cr != null)
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
					mctxt.addData("user", ctx.getUser());
					mctxt.addData("pilot", p);
					mctxt.addData("txReq", tx);
					mctxt.addData("rejectComments", "Transfer automatically deleted after " + purgeInterval + " days");

					// Send a notification message
					Mailer mailer = new Mailer(ctx.getUser());
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