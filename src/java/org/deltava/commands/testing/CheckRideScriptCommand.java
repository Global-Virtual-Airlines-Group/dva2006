// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.testing.CheckRideScript;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CheckrideScriptAccessControl;

/**
 * A Web Site Command to update Check Ride scripts.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CheckRideScriptCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the script.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the existing script
			GetExamProfiles dao = new GetExamProfiles(con);
			CheckRideScript sc = dao.getScript(ctx.getParameter("eqType"));
			if (sc == null)
				sc = new CheckRideScript(ctx.getParameter("eqType"));

			// Load the program and description
			sc.setProgram(ctx.getParameter("programType"));
			sc.setDescription(ctx.getParameter("msgText"));

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
		result.setType(CommandResult.REQREDIRECT);
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

				// Create single-entry equipment type list
				Set<String> eqtypes = new HashSet<String>();
				eqtypes.add(sc.getEquipmentType());
				ctx.setAttribute("actypes", eqtypes, REQUEST);

				// Save in the request
				ctx.setAttribute("script", sc, REQUEST);
				ctx.setAttribute("access", access, REQUEST);
			} else {
				// Calculate our access
				CheckrideScriptAccessControl access = new CheckrideScriptAccessControl(ctx, null);
				access.validate();
				if (!access.getCanCreate())
					throw securityException("Cannot create Check Ride script");

				// Save in request
				ctx.setAttribute("access", access, REQUEST);

				// Save all equipment types
				GetAircraft acdao = new GetAircraft(con);
				ctx.setAttribute("actypes", acdao.getAircraftTypes(), REQUEST);
			}

			// Load equipment types
			GetEquipmentType eqdao = new GetEquipmentType(con);
			ctx.setAttribute("eqTypes", eqdao.getAll(), REQUEST);
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
	 * Callback method called when reading the script. <i>NOT IMPLEMENTED</i>
	 * @param ctx the Command context
	 * @throws UnsupportedOperationException
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		throw new UnsupportedOperationException();
	}
}