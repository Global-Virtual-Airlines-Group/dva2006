// Copyright 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.sql.Connection;
import java.util.Collections;

import org.deltava.beans.EquipmentType;
import org.deltava.beans.testing.CheckRideScript;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CheckrideScriptAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to update Check Ride scripts.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class CheckRideScriptCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the script.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {
		
		// Get the equipment type
		String id = (String) ctx.getCmdParameter(ID, null);
		if (StringUtils.isEmpty(id))
			id = ctx.getParameter("eqType");
		
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the existing script
			GetExamProfiles dao = new GetExamProfiles(con);
			CheckRideScript sc = dao.getScript(id);
			if (sc == null)
				sc = new CheckRideScript(id);

			// Load the program and description
			sc.setProgram(ctx.getParameter("programType"));
			sc.setDescription(ctx.getParameter("msgText"));
			sc.setEquipmentType(ctx.getParameter("eqType"));

			// Calculate our access
			CheckrideScriptAccessControl access = new CheckrideScriptAccessControl(ctx, sc);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot save Check Ride script");

			// Get the DAO and update the script
			SetExamProfile wdao = new SetExamProfile(con);
			wdao.write(sc);

			// Save status attribute
			ctx.setAttribute("script", sc, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status attribute
		ctx.setAttribute("isUpdate", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/testing/profileUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the script.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {

		String id = (String) ctx.getCmdParameter(ID, null);
		try {
			Connection con = ctx.getConnection();
			
			// Get equipment type DAOs
			GetAircraft acdao = new GetAircraft(con);
			GetEquipmentType eqdao = new GetEquipmentType(con);

			// Get the DAO and the script
			GetExamProfiles dao = new GetExamProfiles(con);
			if (id != null) {
				CheckRideScript sc = dao.getScript(id);
				if (sc == null)
					throw notFoundException("Invalid Check Ride script - " + id);

				// Calculate our access
				CheckrideScriptAccessControl access = new CheckrideScriptAccessControl(ctx, sc);
				access.validate();
				if (!access.getCanEdit())
					throw securityException("Cannot edit Check Ride script");

				// Save in the request
				ctx.setAttribute("script", sc, REQUEST);
				ctx.setAttribute("access", access, REQUEST);
				
				// Get primary equipment types
				EquipmentType eq = eqdao.get(sc.getProgram());
				if (eq != null) {
					ctx.setAttribute("eqTypes", Collections.singleton(eq), REQUEST);
					ctx.setAttribute("acTypes", eq.getPrimaryRatings(), REQUEST);
				} else {
					ctx.setAttribute("acTypes", acdao.getAircraftTypes(), REQUEST);
					ctx.setAttribute("eqTypes", eqdao.getAll(), REQUEST);	
				}
			} else {
				// Calculate our access
				CheckrideScriptAccessControl access = new CheckrideScriptAccessControl(ctx, null);
				access.validate();
				if (!access.getCanCreate())
					throw securityException("Cannot create Check Ride script");

				// Save in request
				ctx.setAttribute("access", access, REQUEST);
				
				// Save all equipment types
				ctx.setAttribute("acTypes", acdao.getAircraftTypes(), REQUEST);
				ctx.setAttribute("eqTypes", eqdao.getAll(), REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/testing/crScript.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the script.
	 * @param ctx the Command context
	 * @throws UnsupportedOperationException
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}