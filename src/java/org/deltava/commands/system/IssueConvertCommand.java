// Copyright 2011, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.net.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.Pilot;
import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.IssueAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to convert a devlopemnt Issue into a Help Desk Issue.
 * @author Luke
 * @version 9.0
 * @since 3.6
 */

public class IssueConvertCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		MessageContext mctx = new MessageContext();
		mctx.addData("user", ctx.getUser());
		try {
			Connection con = ctx.getConnection();

			// Get the Issue
			GetIssue rdao = new GetIssue(con);
			Issue i = rdao.get(ctx.getID());
			if (i == null)
				throw notFoundException("Invalid Issue ID - " + ctx.getID());

			// Check our access level
			IssueAccessControl access = new IssueAccessControl(ctx, i);
			access.validate();
			if (!access.getCanResolve())
				throw securityException("Cannot convert development Issue " + ctx.getID());

			// Create the help desk bean
			org.deltava.beans.help.Issue hi = new org.deltava.beans.help.Issue(i.getSubject());
			hi.setAuthorID(i.getAuthorID());
			hi.setCreatedOn(i.getCreatedOn());
			hi.setSubject(i.getSubject());
			hi.setStatus(org.deltava.beans.help.IssueStatus.OPEN);
			hi.setBody(i.getDescription());
			mctx.addData("issue", hi);
			
			// Set default assignee
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.getPilotByCode(SystemData.getInt("helpdesk.assignto"), SystemData.get("airline.db"));
			hi.setAssignedTo(p.getID());
			mctx.addData("assignee", p);

			// Mark resolved
			i.setResolvedOn(Instant.now());
			i.setStatus(IssueStatus.WONTFIX);

			// Start transaction
			ctx.startTX();

			// Write new Issue
			SetHelp hwdao = new SetHelp(con);
			hwdao.write(hi);

			// Convert the comments
			for (IssueComment ic : i.getComments()) {
				org.deltava.beans.help.IssueComment hic = new org.deltava.beans.help.IssueComment(ic.getAuthorID());
				hic.setCreatedOn(ic.getCreatedOn());
				hic.setBody(ic.getBody());
				hic.setID(hi.getID());
				hwdao.write(hic);
			}

			// Add a dummy issue comment
			try {
				URL url = new URL("https", ctx.getRequest().getServerName(), "/issue.do?id=" + i.getHexID());
				org.deltava.beans.help.IssueComment hic = new org.deltava.beans.help.IssueComment(ctx.getUser().getID());
				hic.setBody("Converted from Development Issue at " + url.toString());
				hic.setID(hi.getID());
				hwdao.write(hic);
			} catch (MalformedURLException mue) {
				// empty
			}

			// Close issue
			SetIssue iwdao = new SetIssue(con);
			iwdao.write(i);
			
			// Add a dummy issue comment
			try {
				URL url = new URL("https", ctx.getRequest().getServerName(), "/hdissue.do?id=" + hi.getHexID());
				IssueComment ic = new IssueComment(0, "Converted to Help Desk Issue at " + url.toString());
				ic.setParentID(i.getID());
				ic.setAuthorID(ctx.getUser().getID());
				iwdao.write(ic);
			} catch (MalformedURLException mue) {
				// empty
			}

			// Commit
			ctx.commitTX();
			
			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctx.setTemplate(mtdao.get("HDISSUEASSIGN"));
			
			// Create the message
			Mailer mailer = new Mailer(ctx.getUser());
			mailer.setContext(mctx);
			mailer.send(p);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("issue", null, ctx.getID());
		result.setSuccess(true);
	}
}