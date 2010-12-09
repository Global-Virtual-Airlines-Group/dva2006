// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.help;

import org.deltava.commands.*;

/**
 * A Web Site command to display the privacy policy.
 * @author Luke
 * @version 3.4
 * @since 3.4
 */

public class PrivacyInfoCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/help/privacyPolicy.jsp");
		result.setSuccess(true);
	}
}