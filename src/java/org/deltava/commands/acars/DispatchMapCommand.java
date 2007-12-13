// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import org.deltava.commands.*;

/**
 * A Web Site Command to display the ACARS dispatch map.
 * @author Luke
 * @verison 2.1
 * @since 2.1
 */

public class DispatchMapCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/acars/dispatchMap.jsp");
		result.setSuccess(true);
	}
}