// Copyright 2005, 2006, 2008, 2009, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.cooler.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.*;
import org.deltava.security.command.CoolerThreadAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to handle Water Cooler response posting and editing.
 * @author Luke
 * @version 3.6
 * @since 1.0
 */

public class ThreadReplyCommand extends AbstractCommand {

	private static final Logger log = Logger.getLogger(ThreadReplyCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Initialze the Mailer context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());
		
		// Determine if we are editing the last post
		boolean doEdit = Boolean.valueOf(ctx.getParameter("doEdit")).booleanValue();
		Map<UserData, Person> notifyList = new LinkedHashMap<UserData, Person>();
		try {
			Connection con = ctx.getConnection();

			// Get the Message Thread
			GetCoolerThreads tdao = new GetCoolerThreads(con);
			MessageThread mt = tdao.getThread(ctx.getID());
			if (mt == null)
				throw notFoundException("Unknown Message Thread - " + ctx.getID());

			// Get the channel profile
			GetCoolerChannels cdao = new GetCoolerChannels(con);
			Channel ch = cdao.get(mt.getChannel());
			
			// Load the poll options (if any)
			GetCoolerPolls tpdao = new GetCoolerPolls(con);
			mt.addOptions(tpdao.getOptions(mt.getID()));
			mt.addVotes(tpdao.getVotes(mt.getID()));

			// Check user access
			CoolerThreadAccessControl ac = new CoolerThreadAccessControl(ctx);
			ac.updateContext(mt, ch);
			ac.validate();
			if (!ac.getCanReply())
				throw securityException("Cannot post in Message Thread " + ctx.getID());
			else if (doEdit && !ac.getCanEdit())
				throw securityException("Cannot update Message Thread post in " + ctx.getID());

			// Get the notification entries, and remove our own
			ThreadNotifications nt = tdao.getNotifications(mt.getID());
			nt.removeUser(ctx.getUser());
			
			// Get the DAOs
			GetPilot pdao = new GetPilot(con);
			GetUserData uddao = new GetUserData(con);

			// If we are set to notify people for this thread, then load the data
			if (!doEdit && (!nt.getIDs().isEmpty())) {
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctxt.setTemplate(mtdao.get("THREADNOTIFY"));

				// Get the users to notify
				UserDataMap udm = uddao.get(nt.getIDs());
				Map<Integer, Pilot> pilots = pdao.get(udm);
				for (Iterator<Map.Entry<Integer, Pilot>> i = pilots.entrySet().iterator(); i.hasNext(); ) {
					Map.Entry<Integer, Pilot> me = i.next();
					UserData ud = udm.get(me.getKey());
					if (ud != null)
						notifyList.put(ud, me.getValue());
				}

				// Filter out users who can no longer read this thread
				MultiUserSecurityContext sctx = new MultiUserSecurityContext(ctx);
				for (Iterator<Person> i = notifyList.values().iterator(); i.hasNext();) {
					Person usr = i.next();
					sctx.setUser(usr);

					// Validate this user's access
					ac.updateContxt(sctx);
					ac.validate();
					if (!ac.getCanRead()) {
						log.warn(usr.getName() + " can no longer read " + mt.getSubject());
						i.remove();
					}
				}
			}

			// Create the new reply bean
			Message msg = doEdit ? mt.getLastPost() : new Message(ctx.getUser().getID());
			msg.setThreadID(mt.getID());
			msg.setRemoteAddr(ctx.getRequest().getRemoteAddr());
			msg.setRemoteHost(ctx.getRequest().getRemoteHost());
			msg.setBody(ctx.getParameter("msgText"));
			
			// Start the transaction
			ctx.startTX();
			
			// Add the response to the thread
			SetCoolerMessage wdao = new SetCoolerMessage(con);
			if (!StringUtils.isEmpty(msg.getBody())) {
				if (doEdit) {
					ctx.setAttribute("isEdit", Boolean.TRUE, REQUEST);
					wdao.update(msg);
				} else {
					ctx.setAttribute("isReply", Boolean.TRUE, REQUEST);
					wdao.write(msg);
					mt.addPost(msg);
				}
				
				wdao.synchThread(mt);
			} else
				notifyList.clear();
			
			// Get our vote on the thread
			if (ac.getCanVote() && (!StringUtils.isEmpty(ctx.getParameter("pollVote")))) {
				PollVote v = new PollVote(mt.getID(), ctx.getUser().getID());
				v.setOptionID(StringUtils.parseHex(ctx.getParameter("pollVote")));
				wdao.vote(v);
				ctx.setAttribute("isVote", Boolean.TRUE, REQUEST);
			}
			
			// Commit the transaction
			ctx.commitTX();
			
			// Mark this thread as read
			@SuppressWarnings("unchecked")
			Map<Integer, Date> threadIDs = (Map<Integer, Date>) ctx.getSession().getAttribute(CommandContext.THREADREAD_ATTR_NAME);
			if (threadIDs == null) {
				threadIDs = new HashMap<Integer, Date>();
				ctx.setAttribute(CommandContext.THREADREAD_ATTR_NAME, threadIDs, SESSION);
			}

			// Add thread and save
			threadIDs.put(Integer.valueOf(mt.getID()), new Date());

			// Save thread data
			mctxt.addData("thread", mt);
			mctxt.addData("threadID", StringUtils.formatHex(mt.getID()));

			// Save the thread in the request
			ctx.setAttribute("thread", mt, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Send the notification messages
		if (!notifyList.isEmpty()) {
			for (Iterator<Map.Entry<UserData, Person>> i = notifyList.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry<UserData, Person> me = i.next();
			
				// Create the mailer
				Mailer mailer = new Mailer(null);
				mctxt.addData("url", "http://" + me.getKey().getDomain() + "/");
				mailer.setContext(mctxt);
				mailer.send(me.getValue());
			}

			// Save notification message count
			ctx.setAttribute("notifyMsgs", Integer.valueOf(notifyList.size()), REQUEST);
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/cooler/threadUpdate.jsp");
		result.setSuccess(true);
	}
}