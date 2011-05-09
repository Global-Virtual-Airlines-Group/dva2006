// Copyright 2007, 2009, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to toggle whether a Flight Report counts for promotion to Captain.
 * @author Luke
 * @version 3.7
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
			
			// Get comments
			StringBuilder buf = new StringBuilder();
			if (!StringUtils.isEmpty(fr.getComments())) {
				buf.append(fr.getComments());
				buf.append("\r\n");
			}

			// Start transaction
			ctx.startTX();
			
			// Check if we set or clear
			SetFlightReport fwdao = new SetFlightReport(con);
			if (!fr.getCaptEQType().isEmpty()) {
				buf.append("Promotion flag cleared by " + ctx.getUser().getName());
				fwdao.clearPromoEQ(fr.getID());
			} else {
				GetEquipmentType eqdao = new GetEquipmentType(con);
				Collection<String> pTypeNames = eqdao.getPrimaryTypes(SystemData.get("airline.db"), fr.getEquipmentType());
				
				// Check the types
				FlightPromotionHelper helper = new FlightPromotionHelper(fr);
				for (Iterator<String> i = pTypeNames.iterator(); i.hasNext(); ) {
					String pType = i.next();
					EquipmentType pEQ = eqdao.get(pType, SystemData.get("airline.db"));
					if (!helper.canPromote(pEQ))
						i.remove();
				}
				
				buf.append("Promotion flag in " + StringUtils.listConcat(pTypeNames, ", ") + " set by " + ctx.getUser().getName());
				fwdao.setPromoEQ(fr.getID(), pTypeNames);
			}
			
			// Save comments and commit
			fr.setComments(buf.toString());
			fwdao.writeComments(fr);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward back to the PIREP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("pirep", null, ctx.getID());
		result.setSuccess(true);
	}
}