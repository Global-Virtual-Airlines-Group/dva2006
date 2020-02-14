// Copyright 2006, 2007, 2011, 2013, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import java.net.*;
import java.sql.Connection;

import org.deltava.beans.system.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.HelpDeskAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to convert a Help Desk Issue into a Development Issue.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class IssueConvertCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
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
			i.setCreatedOn(hi.getCreatedOn());
			i.setSubject(hi.getSubject());
			i.setStatus(IssueStatus.OPEN);
			i.setDescription(hi.getBody());
			i.addAirline(SystemData.getApp(null));
			i.setAssignedTo(StringUtils.parse(ctx.getParameter("devAssignedTo"), ctx.getUser().getID()));
			i.setArea(EnumUtils.parse(IssueArea.class, ctx.getParameter("area"), IssueArea.WEBSITE));
			i.setType(EnumUtils.parse(Issue.IssueType.class, ctx.getParameter("type"), org.deltava.beans.system.Issue.IssueType.BUG));
			i.setPriority(EnumUtils.parse(IssuePriority.class, ctx.getParameter("priority"), IssuePriority.MEDIUM));
			
			// Update the issue
			hi.setStatus(org.deltava.beans.help.Issue.CLOSED);
			
			// Start a transaction
			ctx.startTX();
			
			// Get the DAO and write the new Issue
			SetIssue wdao = new SetIssue(con);
			wdao.write(i);
			
			// Copy the issue comments
			for (org.deltava.beans.help.IssueComment hic : hi.getComments()) {
				org.deltava.beans.system.IssueComment ic = new org.deltava.beans.system.IssueComment(hic.getBody());
				ic.setCreatedOn(hic.getCreatedOn());
				ic.setAuthorID(hic.getAuthorID());
				ic.setIssueID(i.getID());
				wdao.write(ic);
			}
			
			// Add a dummy issue comment
			try {
				URL url = new URL("http", ctx.getRequest().getServerName(), "/hdissue.do?id=" + hi.getHexID());
				org.deltava.beans.system.IssueComment ic = new org.deltava.beans.system.IssueComment("Converted Help Desk Issue at " + url.toString());
				ic.setAuthorID(ctx.getUser().getID());
				ic.setIssueID(i.getID());
				wdao.write(ic);
			} catch (MalformedURLException mue) {
				// empty
			}
			
			// Create the new Help Desk issue comment
			org.deltava.beans.help.IssueComment hic = new org.deltava.beans.help.IssueComment(ctx.getUser().getID());
			hic.setID(hi.getID());
			try {
				URL url = new URL("https", ctx.getRequest().getServerName(), "/issue.do?id=" + i.getHexID());
				hic.setBody("Converted to Development Issue at " + url.toString());
			} catch (MalformedURLException mue) {
				hic.setBody("Converted to Development Issue");
			}
			
			// Update the existing issue
			SetHelp hwdao = new SetHelp(con);
			hwdao.write(hi);
			hwdao.write(hic);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the old Issue
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("hdissue", null, ctx.getID());
	}
}