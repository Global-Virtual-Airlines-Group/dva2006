// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import org.deltava.commands.*;

/**
 * A Web Site Command to reset a Flight Assignment that is under construction. 
 * @author Luke
 * @version 2.7
 * @since 2.7
 */

public class ResetCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Reset session variables
		ctx.getSession().removeAttribute("buildAssign");
		ctx.getSession().removeAttribute("fafCriteria");
		ctx.getSession().removeAttribute("fafResults");
		
		// Redirect back to the find-a-flight command
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("findflight.do");
		result.setSuccess(true);
	}
}