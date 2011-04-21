// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;

import org.deltava.beans.system.Issue;
import org.deltava.beans.system.IssueComment;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.IssueAccessControl;

/**
 * A Web Site Command to convert a devlopemnt Issue into a Help Desk Issue.
 * @author Luke
 * @version 3.6
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
	         hi.setStatus(org.deltava.beans.help.Issue.OPEN);
	         hi.setBody(i.getDescription());
	         
	         // Convert the comments
	         for (Iterator<IssueComment> ici = i.getComments().iterator(); ici.hasNext(); ) {
	        	 IssueComment ic = ici.next();

	        	 
	         }
	         
	         // Create new comment
	         
	         // Mark resolved
	         i.setResolvedOn(new Date());
	         i.setStatus(Issue.STATUS_WONTFIX);
	         
	         // Start transaction
	         ctx.startTX();
	         
	         // Write new Issue
	         SetHelp hwdao = new SetHelp(con);
	         
	         
	         // Close issue
	         SetIssue iwdao = new SetIssue(con);
	         iwdao.write(i);
	         
				// Add a dummy issue comment
				try {
					URL url = new URL("http", ctx.getRequest().getServerName(), "/hdissue.do?id=" + hi.getHexID());
					org.deltava.beans.system.IssueComment ic = new org.deltava.beans.system.IssueComment("Converted Help Desk Issue at " + url.toString());
					ic.setAuthorID(ctx.getUser().getID());
					ic.setIssueID(i.getID());
					wdao.writeComment(ic);
				} catch (MalformedURLException mue) {
					// empty
				}
			
	         // Commit
	         ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		
		
	}
}