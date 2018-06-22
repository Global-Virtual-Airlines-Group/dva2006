// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.sql.Connection;

import org.deltava.beans.navdata.RunwayMapping;
import org.deltava.beans.schedule.Airport;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to update runway mappings.
 * @author Luke
 * @version 8.3
 * @since 8.3
 */

public class RunwayMappingCommand extends AbstractFormCommand {

	/* (non-Javadoc)
	 * @see org.deltava.commands.AbstractFormCommand#execSave(org.deltava.commands.CommandContext)
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		Airport a = null; boolean isDelete = Boolean.valueOf(ctx.getParameter("isDelete")).booleanValue();
		try {
			Connection con = ctx.getConnection();
			SetRunwayMapping rmwdao = new SetRunwayMapping(con);
			
			// Get the mapping
			if (isDelete) {
				String oldCode = String.valueOf(ctx.getCmdParameter(ID, null));
				GetRunwayMapping rmdao = new GetRunwayMapping(con);
				RunwayMapping rm = rmdao.get(a, oldCode);
				if (rm == null)
					throw notFoundException("Invalid Runway mapping - " + a.getICAO() + " " + oldCode);
				
				rmwdao.delete(rm);
			} else {
				
				
			}
			
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("rwymappings", null, a.getICAO());
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}

	/* (non-Javadoc)
	 * @see org.deltava.commands.AbstractFormCommand#execEdit(org.deltava.commands.CommandContext)
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		
		// Get the airport
		Airport a = SystemData.getAirport(ctx.getParameter("airport"));
		if (a == null)
			throw notFoundException("Invalid Airport - " + ctx.getParameter("airport"));
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the mapping
			String oldCode = String.valueOf(ctx.getCmdParameter(ID, null));
			GetRunwayMapping rmdao = new GetRunwayMapping(con);
			RunwayMapping rm = rmdao.get(a, oldCode);
			
			// Save in the request
			ctx.setAttribute("airport", a, REQUEST);
			
			
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/navdata/runwayMapping.jsp");
		result.setSuccess(true);
	}

	/* (non-Javadoc)
	 * @see org.deltava.commands.AbstractFormCommand#execRead(org.deltava.commands.CommandContext)
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}