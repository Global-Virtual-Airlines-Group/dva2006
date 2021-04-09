// Copyright 2012, 2015, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.EMailAddress;
import org.deltava.beans.stats.DisposalQueueStats;

import org.deltava.dao.*;
import org.deltava.mail.*;
import org.deltava.taskman.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to send e-mail notifications when the Flight Report disposal queue exceeds a certain size.
 * @author Luke
 * @version 10.0
 * @since 5.0
 */

public class QueueWarningTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public QueueWarningTask() {
		super("Queue Warning", QueueWarningTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {

		Mailer m = new Mailer(ctx.getUser());
		try {
			Connection con = ctx.getConnection();
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			
			// Get the PIREP queue size
			GetFlightReportQueue frqdao = new GetFlightReportQueue(con);
			DisposalQueueStats dqs = frqdao.getDisposalQueueStats(null);
			String msg = "Queue has " + dqs.getSize() + " entries, average age " + StringUtils.format(dqs.getAdjustedAge(), "#0.0") + " hours";
			
			// If too big, load the users
			if ((dqs.getSize() > SystemData.getInt("users.pirep.warn.minSize", 25)) || (dqs.getAdjustedAge() > SystemData.getInt("users.pirep.warn.minAge", 18))) {
				log.warn(msg);
				MessageContext mctxt = new MessageContext();
				mctxt.setTemplate(mtdao.get("PIREPQUEUEWARN"));
				mctxt.addData("queueStats", dqs);
				
				// Load the recipients
				Collection<EMailAddress> usrs = new ArrayList<EMailAddress>();
				usrs.addAll(pdao.getByRole("PIREP", ctx.getDB()));
				if (!usrs.isEmpty()) {
					m.setContext(mctxt);
					m.send(usrs);
				}
			} else
				log.info(msg);
			
			// Check promotion queue
			GetPilotRecognition pqdao = new GetPilotRecognition(con);
			Collection<Integer> IDs = pqdao.getPromotionQueue(null);
			if (!IDs.isEmpty()) {
				MessageContext mctxt = new MessageContext();
				mctxt.setTemplate(mtdao.get("PROMOQUEUEWARN"));
				mctxt.addData("promoQueueSize", Integer.valueOf(IDs.size()));

				// Load the recipients
				Collection<EMailAddress> usrs = new LinkedHashSet<EMailAddress>();
				usrs.addAll(pdao.getByRole("PIREP", ctx.getDB()));
				usrs.addAll(pdao.getByRole("HR", ctx.getDB()));
				if (!usrs.isEmpty()) {
					m.setContext(mctxt);
					m.send(usrs);
				}
			}
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		log.info("Processing Complete");
	}
}