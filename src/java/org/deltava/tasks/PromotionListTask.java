// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.cooler.*;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.CalendarUtils;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to display daily promotions in the Water Cooler. 
 * @author Luke
 * @version 3.6
 * @since 3.6
 */

public class PromotionListTask extends Task {
	
	private static final List<Integer> UPD_TYPES = Arrays.asList(Integer.valueOf(StatusUpdate.CERT_ADD),
		Integer.valueOf(StatusUpdate.RECOGNITION), Integer.valueOf(StatusUpdate.EXTPROMOTION),
		Integer.valueOf(StatusUpdate.INTPROMOTION));

	/**
	 * Initializes the Task.
	 */
	public PromotionListTask() {
		super("Daily Promotion List", PromotionListTask.class);
	}

	/**
	 * Executes the Task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		
		// Get the channel to post in
		String channelName = SystemData.get("cooler.promotion.channel");
		if (channelName == null) {
			log.warn("No Water Cooler promotions channel");
			return;
		}
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the Channel
			GetCoolerChannels cdao = new GetCoolerChannels(con);
			Channel c = cdao.get(channelName);
			if (c == null)
				throw new DAOException("Unknown promotions Channel - " + channelName);
			
			// Load the updates
			GetStatusUpdate sudao = new GetStatusUpdate(con);
			Collection<StatusUpdate> upds = new ArrayList<StatusUpdate>();
			for (Iterator<Integer> i = UPD_TYPES.iterator(); i.hasNext(); ) {
				int updType = i.next().intValue();
				upds.addAll(sudao.getByType(updType, SystemData.getInt("cooler.promotion.maxAge", 24)));
			}
			
			// Create the message
			log.info("Recording " + upds.size() + " Pilot Recognition entries in Forum");
			DateTime now = new DateTime(new Date());
			now.convertTo(TZInfo.get(SystemData.get("time.timezone")));
			MessageThread mt = new MessageThread(SystemData.get("airline.name") + " promotions for "
				+ StringUtils.format(now.getDate(), SystemData.get("time.date_format")));
			mt.setChannel(c.getName());
			mt.setStickyUntil(CalendarUtils.adjust(now.getDate(), 1));

			Message msg = new Message(ctx.getUser().getID());
			msg.setRemoteAddr("127.0.0.1");
			msg.setRemoteHost("localhost");
			StringBuilder msgBuf = new StringBuilder("The following " + SystemData.get("airline.name")
				+ " pilots have received promotions, Flight Academy certifications or achieved Accomplishments:");
			msgBuf.append("\n\n");
			
			// Add the pilots
			GetPilot pdao = new GetPilot(con);
			for (Iterator<StatusUpdate> i = upds.iterator(); i.hasNext(); ) {
				StatusUpdate upd = i.next();
				Pilot p = pdao.get(upd.getID());
				
				// Add pilot name and link
				msgBuf.append(p.getRank());
				msgBuf.append(' ');
				msgBuf.append("[url=/profile.do?id=");
				msgBuf.append(p.getHexID());
				msgBuf.append(']');
				msgBuf.append(p.getName());
				msgBuf.append("[/url] (");
				msgBuf.append(p.getPilotCode());
				msgBuf.append(") - ");
				
				// Add status update description
				msgBuf.append(upd.getDescription());
				msgBuf.append('\n');
			}
			
			// Set the body
			msgBuf.append("\nPlease join me in congratulating these " + SystemData.get("airline.name") + " Pilots.\n");
			msg.setBody(msgBuf.toString());
			mt.addPost(msg);
			
			// Write the thread
			if (!upds.isEmpty()) {
				SetCoolerMessage mwdao = new SetCoolerMessage(con);
				mwdao.write(mt);
			}
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		log.info("Processing Complete");
	}
}