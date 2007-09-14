// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.sql.Connection;
import java.util.Collection;

import org.deltava.beans.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to toggle whether a Flight Report counts for promotion to Captain.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PromotionToggleCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the Flight Report
			GetFlightReports frdao = new GetFlightReports(con);
			FlightReport fr = frdao.get(ctx.getID());
			if (fr == null)
				throw notFoundException("Invalid Flight Report - " + ctx.getID());
			
			// Check our access
			PIREPAccessControl access = new PIREPAccessControl(ctx, fr);
			access.validate();
			if (!access.getCanDispose())
				throw securityException("Cannot Toggle Promotion flag on " + ctx.getID());
			
			// Check if we set or clear
			SetFlightReport fwdao = new SetFlightReport(con);
			if (!fr.getCaptEQType().isEmpty())
				fwdao.clearPromoEQ(fr.getID());
			else {
				GetEquipmentType eqdao = new GetEquipmentType(con);
				Collection<String> pTypeNames = eqdao.getPrimaryTypes(SystemData.get("airline.db"), fr.getEquipmentType());
				fwdao.setPromoEQ(fr.getID(), pTypeNames);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward back to the PIREP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REDIRECT);
		result.setURL("pirep", null, ctx.getID());
		result.setSuccess(true);
	}
}