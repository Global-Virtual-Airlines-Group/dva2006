// Copyright 2005, 2007, 2008, 2011, 2015, 2019, 2020, 2021, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.*;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;
import org.deltava.security.command.IssueAccessControl;

/**
 * A web site command to save new Issue Comments.
 * @author Luke
 * @version 10.2
 * @since 1.0
 */

public class IssueCommentCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();

			// Load the Issue we are attempting to comment on
			GetIssue rdao = new GetIssue(con);
			Issue i = rdao.get(ctx.getID());
			if (i == null)
				throw notFoundException("Invalid Issue ID - " + ctx.getID());

			// Check our access level
			IssueAccessControl access = new IssueAccessControl(ctx, i);
			access.validate();
			if (!access.getCanComment())
				throw securityException("Cannot comment on Issue " + ctx.getID());

			// Create the Issue comment bean
			IssueComment ic = new IssueComment(0, ctx.getParameter("comment"));
			ic.setParentID(i.getID());
			ic.setAuthorID(ctx.getUser().getID());
			
			// Create an Issue file bean if attached file
			FileUpload fu = ctx.getFile("attach", 2048 * 1024);
			if (fu != null) {
				ic.load(fu.getBuffer());
				ic.setName(fu.getName());
			}

			// Start a transaction
			ctx.startTX();

			// If the Issue is closed, reopen it
			SetIssue wdao = new SetIssue(con);
			if (i.getStatus() != IssueStatus.OPEN) {
				i.setStatus(IssueStatus.OPEN);
				wdao.write(i);
			}

			// Write the comment and commit
			wdao.write(ic);
			ctx.commitTX();

			// Check if we're sending this comment via e-mail
			boolean sendComment = Boolean.parseBoolean(ctx.getParameter("emailComment"));
			if (sendComment) {
				ctx.setAttribute("sendComment", Boolean.TRUE, REQUEST);
				Collection<Integer> pilotIDs = new HashSet<Integer>();

				// Create and populate the message context
				MessageContext mctx = new MessageContext();
				mctx.addData("issue", i);
				mctx.addData("comment", ic);
				mctx.addData("user", ctx.getUser());

				// Check if we're sending to all commenters
				boolean sendAll = Boolean.parseBoolean(ctx.getParameter("emailAll"));
				if (sendAll)
					i.getComments().stream().map(IssueComment::getAuthorID).forEach(pilotIDs::add);

				// Get the Issue creator and assignee, and remove the current user's IDs
				GetUserData uddao = new GetUserData(con);
				UserData ud = uddao.get(ctx.getUser().getID());
				pilotIDs.add(Integer.valueOf(i.getAuthorID()));
				pilotIDs.add(Integer.valueOf(i.getAssignedTo()));
				pilotIDs.removeAll(ud.getIDs());

				// Get the message template
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctx.setTemplate(mtdao.get("ISSUECOMMENT"));

				// Get the user data
				UserDataMap udm = uddao.get(pilotIDs);

				// Get the pilot profiles
				GetPilot pdao = new GetPilot(con);
				Collection<Pilot> pilots = pdao.get(udm).values();
				pilots = pilots.stream().filter(p -> (p.getStatus() == PilotStatus.ACTIVE)).collect(Collectors.toList());

				// Create the e-mail message
				Mailer mailer = new Mailer(ctx.getUser());
				mailer.setContext(mctx);
				mailer.send(pilots);
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("issue", "read", ctx.getID());
		result.setSuccess(true);
	}
}