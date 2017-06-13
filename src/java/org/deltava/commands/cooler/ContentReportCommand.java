 // Copyright 2006, 2007, 2008, 2011, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;
import org.deltava.beans.cooler.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.CoolerThreadAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to report Water Cooler threads with questionable content.
 * @author Luke
 * @version 7.4
 * @since 1.0
 */

public class ContentReportCommand extends AbstractCommand {
	
	private static final Logger log = Logger.getLogger(ContentReportCommand.class);

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Initialize the Message Context
		MessageContext mctx = new MessageContext();
		mctx.addData("user", ctx.getUser());

		boolean isLocked = false;
		MessageThread mt = null;
		Collection<Pilot> moderators = new ArrayList<Pilot>();
		try {
			Connection con = ctx.getConnection();
			
			// Get the message thread
			GetCoolerThreads dao = new GetCoolerThreads(con);
			mt = dao.getThread(ctx.getID());
			if (mt == null)
				throw notFoundException("Invalid Message Thread - " + ctx.getID());
			
			// Get the channel
			GetCoolerChannels cdao = new GetCoolerChannels(con);
			Channel c = cdao.get(mt.getChannel());
			if (c == null)
				throw notFoundException("Invalid Water Cooler channel - " + mt.getChannel());
			
			// Check our access
			CoolerThreadAccessControl ac = new CoolerThreadAccessControl(ctx);
			ac.updateContext(mt, c);
			ac.validate();
			if (!ac.getCanReport())
				throw securityException("Cannot report Message Thread");
			
			// Add a content warning entry
			ThreadUpdate upd = new ThreadUpdate(mt.getID());
			upd.setAuthorID(ctx.getUser().getID());
			upd.setDescription("Discussion Thread reported for potential inappropriate content");
			
			// Start a transaction
			ctx.startTX();
			
			// Write the update
			SetCoolerMessage wdao = new SetCoolerMessage(con);
			wdao.write(upd);
			
			// Write the warning
			wdao.report(mt, ctx.getUser().getID());
			
			// Reload the thread from the database
			mt = dao.getThread(mt.getID());
			
			// If we hit the limit for warnings, lock the thread
			mt.addReportID(ctx.getUser().getID());
			int maxWarns = SystemData.getInt("cooler.maxreports", 4);
			if (mt.getReportCount() == maxWarns) {
				log.warn("Locking Thread \"" + mt.getSubject() + "\" after " + maxWarns + " Contentn Warnings");
				wdao.moderateThread(mt.getID(), true, true);
				
				// Mark the thread as locked
				ThreadUpdate upd2 = new ThreadUpdate(mt.getID());
				upd2.setDate(upd.getDate().plusSeconds(1));
				upd2.setAuthorID(ctx.getUser().getID());
				upd2.setDescription("Discussion Thread automatically locked/hidden after " + maxWarns + " content reports");
				wdao.write(upd2);
				isLocked = true;
			} else if (mt.getReportCount() == 1) {
				// Get the notification message
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctx.setTemplate(mtdao.get("CONTENTWARN"));
				mctx.addData("thread", mt);
				mctx.addData("maxWarns", Integer.valueOf(maxWarns));

				// Get the moderators
				GetPilotDirectory pdao = new GetPilotDirectory(con);
				for (String aCode : c.getAirlines()) {
					AirlineInformation aInfo = SystemData.getApp(aCode);
					moderators.addAll(pdao.getByRole("Moderator", aInfo.getDB()));
				}
			}
			
			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Notify Moderators on first warning
		if (!moderators.isEmpty()) {
			log.warn("Sending Content Warning notification to moderators");
			Mailer mailer = new Mailer(ctx.getUser());
			mailer.setContext(mctx);
			mailer.send(moderators);
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
		if (isLocked && (!ctx.isUserInRole("Moderator")))
			result.setURL("channel", null, mt.getChannel());
		else 
			result.setURL("thread", null, mt.getID());
	}
}