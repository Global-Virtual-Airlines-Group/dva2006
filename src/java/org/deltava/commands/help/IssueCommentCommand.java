// Copyright 2006, 2008, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.EMailAddress;
import org.deltava.beans.FileUpload;
import org.deltava.beans.help.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.HelpDeskAccessControl;

/**
 * A Web Site Command to save Flight Academy Issue comments.
 * @author Luke
 * @version 9.0
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
		
        // Create and populate the message context
        MessageContext mctx = new MessageContext();
        mctx.addData("user", ctx.getUser());

        Collection<? extends EMailAddress> recipients = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the Issue
			GetHelp dao = new GetHelp(con);
			Issue i = dao.getIssue(ctx.getID());
			if (i == null)
				throw notFoundException("Invalid Issue - " + ctx.getID());
			
			// Check our Access
			HelpDeskAccessControl ac = new HelpDeskAccessControl(ctx, i);
			ac.validate();
			if (!ac.getCanComment())
				throw securityException("Cannot comment on Issue " + i.getID());
			
			// Build the Issue Comment
			IssueComment ic = new IssueComment(ctx.getUser().getID());
			ic.setID(i.getID());
			ic.setBody(ctx.getParameter("body"));
			i.addComment(ic);
			
			// Create an Issue file bean if attached file
			FileUpload fu = ctx.getFile("attach");
			if (fu != null) {
				ic.load(fu.getBuffer());
				ic.setName(fu.getName());
			}
			
			// Get all of the recipients
			Collection<Integer> IDs = i.getComments().stream().map(IssueComment::getAuthorID).collect(Collectors.toSet());
			IDs.add(Integer.valueOf(i.getAuthorID()));
			IDs.add(Integer.valueOf(i.getAssignedTo()));
			IDs.remove(Integer.valueOf(ctx.getUser().getID()));
			
			// Update message context
	        mctx.addData("issue", i);
	        mctx.addData("comment", ic);
	        
            // Get the message template
            GetMessageTemplate mtdao = new GetMessageTemplate(con);
            mctx.setTemplate(mtdao.get("HDISSUECOMMENT"));
            
            // Start a transaction
            ctx.startTX();
            
            // If the Issue is closed, reopen it
            SetHelp iwdao = new SetHelp(con);
            if (i.getStatus() == Issue.CLOSED) {
            	i.setStatus(Issue.OPEN);
            	iwdao.write(i);
            }
			
			// Save and commit
			iwdao.write(ic);
			ctx.commitTX();
			
			// Load the recipients
			GetPilot pdao = new GetPilot(con);
			recipients = pdao.getByID(IDs, "PILOTS").values();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
        // Create the e-mail message
        Mailer mailer = new Mailer(ctx.getUser());
        mailer.setContext(mctx);
        mailer.send(recipients);

		// Redirect back to the Issue
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("hdissue", null, ctx.getID());
		result.setSuccess(true);
	}
}