// Copyright 2012, 2015 Global Virtual Airlines Group. All Rights Reserved.
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
 * A Scheduled Task to send e-mail notifications when the Flight Report disposal queue 
 * exceeds a certain size.
 * @author Luke
 * @version 6.3
 * @since 5.0
 */

public class PIREPQueueWarningTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public PIREPQueueWarningTask() {
		super("PIREP Queue Warning", PIREPQueueWarningTask.class);
	}

	/**
	 * Executes the Task.
	 */
	@Override
	protected void execute(TaskContext ctx) {

		MessageContext mctxt = new MessageContext();
		Collection<EMailAddress> usrs = new ArrayList<EMailAddress>();
		try {
			Connection con = ctx.getConnection();
			
			// Get the queue size
			GetFlightReportQueue frqdao = new GetFlightReportQueue(con);
			DisposalQueueStats dqs = frqdao.getDisposalQueueStats(null);
			mctxt.addData("queueStats", dqs);
			String msg = "Queue has " + dqs.getSize() + " entries, average age " + StringUtils.format(dqs.getAdjustedAge(), "#0.00") + " hours";
			
			// If too big, load the users
			if ((dqs.getSize() > SystemData.getInt("users.pirep.warn.minSize", 25)) || (dqs.getAdjustedAge() > SystemData.getInt("users.pirep.warn.minAge", 18))) {
				log.warn(msg);
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctxt.setTemplate(mtdao.get("PIREPQUEUEWARN"));
				
				GetPilotDirectory pdao = new GetPilotDirectory(con);
				usrs.addAll(pdao.getByRole("PIREP", SystemData.get("airline.db")));
			} else
				log.info(msg);
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Send e-mail
		if (!usrs.isEmpty()) {
			Mailer m = new Mailer(ctx.getUser());
			m.setContext(mctxt);
			m.send(usrs);
		}
		
		log.info("Processing Complete");
	}
}