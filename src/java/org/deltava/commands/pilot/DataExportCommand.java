// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import org.deltava.commands.*;

/**
 * A Web Site Command to display log book export options. 
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public class DataExportCommand extends AbstractCommand {
	
	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occrurs.
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/dataExport.jsp");
		result.setSuccess(true);
	}
}