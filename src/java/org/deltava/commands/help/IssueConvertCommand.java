// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import java.net.*;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.help.*;
import org.deltava.beans.system.Issue;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.HelpDeskAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to convert a Help Desk Issue into a Development Issue.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class IssueConvertCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Load the original Issue
			GetHelp hdao = new GetHelp(con);
			org.deltava.beans.help.Issue hi = hdao.getIssue(ctx.getID());
			if (hi == null)
				throw notFoundException("Invalid Issue ID - " + ctx.getID());
			
			// Check if we can update status
			HelpDeskAccessControl ac = new HelpDeskAccessControl(ctx, hi);
			ac.validate();
			if (!ac.getCanUpdateStatus())
				throw securityException("Cannot convert Help Desk Issue");
			
			// Create the new issue bean
			org.deltava.beans.system.Issue i = new org.deltava.beans.system.Issue(hi.getSubject());
			i.setAuthorID(hi.getAuthorID());
			i.setCreatedOn(new Date());
			i.setSubject(hi.getSubject());
			i.setStatus(org.deltava.beans.system.Issue.STATUS_OPEN);
			i.setDescription(hi.getBody());
			i.setAssignedTo(StringUtils.parse(ctx.getParameter("assignedTo"), ctx.getUser().getID()));
			i.setArea(StringUtils.arrayIndexOf(Issue.AREA, ctx.getParameter("area"), Issue.AREA_WEBSITE));
			i.setType(StringUtils.arrayIndexOf(Issue.TYPE, ctx.getParameter("type"), Issue.TYPE_BUG));
			i.setPriority(StringUtils.arrayIndexOf(Issue.PRIORITY, ctx.getParameter("priority"), Issue.PRIORITY_MEDIUM));
			
			// Update the issue
			hi.setStatus(org.deltava.beans.help.Issue.CLOSED);
			
			// Start a transaction
			ctx.startTX();
			
			// Get the DAO and write the new Issue
			SetIssue wdao = new SetIssue(con);
			wdao.write(i);
			
			// Copy the issue comments
			for (Iterator<IssueComment> ici = hi.getComments().iterator(); ici.hasNext(); ) {
				IssueComment hic = ici.next();
				org.deltava.beans.system.IssueComment ic = new org.deltava.beans.system.IssueComment(hic.getBody());
				ic.setCreatedOn(hic.getCreatedOn());
				ic.setAuthorID(hic.getAuthorID());
				ic.setIssueID(i.getID());
				wdao.writeComment(ic);
			}
			
			// Add a dummy issue comment
			org.deltava.beans.system.IssueComment ic = new org.deltava.beans.system.IssueComment("Converted Help Desk Issue");
			ic.setAuthorID(ctx.getUser().getID());
			ic.setIssueID(i.getID());
			wdao.writeComment(ic);
			
			// Create the new Help Desk issue comment
			IssueComment hic = new IssueComment(ctx.getUser().getID());
			hic.setID(hi.getID());
			try {
				URL url = new URL("http", ctx.getRequest().getServerName(), "/issue.do?id=" + StringUtils.formatHex(i.getID()));
				hic.setBody("Converted to Development Issue at " + url.toString());
			} catch (MalformedURLException mue) {
				hic.setBody("Converted to Development Issue");
			}
			
			// Update the existing issue
			SetHelp hwdao = new SetHelp(con);
			hwdao.write(hi);
			hwdao.write(hic);
			
			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the old Issue
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REDIRECT);
		result.setURL("hdissue", null, ctx.getID());
	}
}