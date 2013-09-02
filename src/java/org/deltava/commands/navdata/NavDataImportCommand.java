// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.sql.Connection;

import org.deltava.beans.navdata.CycleInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * An abstract Command to help loading Navigation Data.
 * @author Luke
 * @version 5.1
 * @since 5.1
 */

abstract class NavDataImportCommand extends AbstractCommand {

	/**
	 * Retrieves the currently loaded navigation data cycle.
	 * @param ctx a CommandContext
	 * @return a CycleInfo bean, or null if none
	 * @throws CommandException if a JDBC error occurs
	 */
	protected CycleInfo getCurrrentCycle(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the current cycle ID
			GetMetadata mddao = new GetMetadata(con);
			String cycleID = mddao.get("navdata.cycle");
			
			// Get the data
			GetNavCycle nvdao = new GetNavCycle(con);
			return nvdao.getCycle(cycleID);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
	}
}