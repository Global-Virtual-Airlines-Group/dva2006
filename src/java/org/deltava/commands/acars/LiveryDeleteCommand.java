// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.sql.Connection;
import java.util.StringTokenizer;

import org.deltava.beans.acars.Livery;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to delete an ACARS multi-player livery command.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class LiveryDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		String id = (String) ctx.getCmdParameter(ID, null);
		try {
			Connection con = ctx.getConnection();
			
			// Split the ID and get the Livery
			StringTokenizer tkns = new StringTokenizer(id, "-");
			GetACARSLivery dao = new GetACARSLivery(con);
			Livery l = dao.get(SystemData.getAirline(tkns.nextToken()), tkns.nextToken());
			if (l == null)
				throw notFoundException("Invalid Livery - " + id);
			
			// Delete the livery
			SetACARSData wdao = new SetACARSData(con);
			wdao.deleteLivery(l.getAirline().getCode(), l.getCode());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the view
		CommandResult result = ctx.getResult();
		result.setURL("liveries.do");
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}