// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.help.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.HelpDeskAccessControl;

import org.deltava.util.CollectionUtils;

/**
 * A Web Site Command to update a Help Desk Issue.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class IssueUpdateCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();

			// Get the Issue
			GetHelp dao = new GetHelp(con);
			Issue i = dao.getIssue(ctx.getID());
			if (i == null)
				throw notFoundException("Invalid Issue - " + ctx.getID());

			// Check our access
			HelpDeskAccessControl ac = new HelpDeskAccessControl(ctx, i);
			ac.validate();
			if (!ac.getCanUpdateContent())
				throw securityException("Cannot update Comments/FAQ status");

			// Build a map of comments
			Map<Long, IssueComment> comments = new HashMap<Long, IssueComment>();
			for (Iterator<IssueComment> ici = i.getComments().iterator(); ici.hasNext();) {
				IssueComment ic = ici.next();
				comments.put(new Long(ic.getCreatedOn().getTime()), ic);
			}

			// Update FAQ attribute from the request
			i.setFAQ(Boolean.valueOf(ctx.getParameter("isFAQ")).booleanValue());

			// Start the transaction
			ctx.startTX();

			// Get the DAO and save the issue
			SetHelp wdao = new SetHelp(con);
			wdao.write(i);

			// Determine what items to delete
			Collection<String> deleteIDs = ctx.getParameters("deleteID");
			if (!CollectionUtils.isEmpty(deleteIDs)) {
				for (Iterator<String> di = deleteIDs.iterator(); di.hasNext();) {
					Long commentID = Long.valueOf(di.next());
					if (comments.containsKey(commentID)) {
						comments.remove(commentID);
						wdao.deleteComment(i.getID(), new Date(commentID.longValue()));
					}
				}
			}

			// Determine what item to make the FAQ answer
			Collection<String> faqIDs = ctx.getParameters("faqID");
			if (CollectionUtils.isEmpty(faqIDs)) {
				wdao.markFAQ(i.getID(), null);
			} else if (faqIDs.size() > 1) {
				throw new CommandException("Multiple FAQ answers specified!");
			} else {
				Long commentID = Long.valueOf(faqIDs.iterator().next());
				if (comments.containsKey(commentID))
					wdao.markFAQ(i.getID(), new Date(commentID.longValue()));
			}

			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("hdissue", null, ctx.getID());
		result.setSuccess(true);
	}
}