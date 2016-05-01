// Copyright 2006, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import org.deltava.beans.help.Issue;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display the Frequently Asked Questions.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class FAQCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		ViewContext<Issue> vc = initView(ctx, Issue.class);
		try {
			GetHelp dao = new GetHelp(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getFAQ());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/help/faq.jsp");
		result.setSuccess(true);
	}
}