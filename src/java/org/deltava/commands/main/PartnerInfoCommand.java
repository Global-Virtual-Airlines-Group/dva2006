// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import org.deltava.commands.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display partner organization information.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class PartnerInfoCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Foward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL(String.format("/jsp/main/%sPartners.jsp", SystemData.get("airline.code").toLowerCase()));
		result.setSuccess(true);
	}
}