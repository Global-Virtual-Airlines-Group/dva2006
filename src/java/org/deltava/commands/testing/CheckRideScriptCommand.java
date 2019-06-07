// Copyright 2005, 2006, 2009, 2010, 2016, 2017, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.EquipmentRideScriptAccessControl;

import org.deltava.util.*;

/**
 * A Web Site Command to update Check Ride scripts.
 * @author Luke
 * @version 8.6
 * @since 1.0
 */

public class CheckRideScriptCommand extends AbstractAuditFormCommand {

	/**
	 * Callback method called when saving the script.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		
		// Get the equipment type
		String id = (String) ctx.getCmdParameter(ID, null);
		boolean isCurrency = Boolean.valueOf(ctx.getParameter("isCurrency")).booleanValue();
		EquipmentRideScriptKey key = EquipmentRideScriptKey.isValid(id) ? EquipmentRideScriptKey.parse(id) : new EquipmentRideScriptKey(ctx.getParameter("programType"), ctx.getParameter("eqType"), isCurrency);
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the existing script
			GetExamProfiles dao = new GetExamProfiles(con);
			EquipmentRideScript sc = dao.getScript(key); EquipmentRideScript osc = BeanUtils.clone(sc);
			if (sc == null)
				sc = new EquipmentRideScript(key.getProgramName(), key.getEquipmentType());

			// Load the program and description
			sc.setDescription(ctx.getParameter("msgText"));
			sc.setEquipmentType(ctx.getParameter("eqType"));
			sc.setIsCurrency(isCurrency);
			ctx.getParameters("sims", Collections.emptySet()).stream().map(s ->Simulator.fromName(s, Simulator.UNKNOWN)).filter(s -> (s != Simulator.UNKNOWN)).forEach(sc::addSimulator);

			// Calculate our access
			EquipmentRideScriptAccessControl access = new EquipmentRideScriptAccessControl(ctx, sc);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot save Check Ride script");
			
			// Check audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(osc, sc);
			AuditLog ae = AuditLog.create(sc, delta, ctx.getUser().getID());
			
			// Start transaction
			ctx.startTX();

			// Get the DAO and update the script
			SetExamProfile wdao = new SetExamProfile(con);
			wdao.write(sc);
			
			// Write audit log
			writeAuditLog(ctx, ae);
			ctx.commitTX();

			// Save status attribute
			ctx.setAttribute("script", sc, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
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
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {

		EquipmentRideScriptKey key = EquipmentRideScriptKey.parse((String) ctx.getCmdParameter(ID, null));
		try {
			Connection con = ctx.getConnection();
			
			// Get equipment type DAOs
			GetAircraft acdao = new GetAircraft(con);
			GetEquipmentType eqdao = new GetEquipmentType(con);
			Collection<EquipmentType> allEQ = eqdao.getAll();
			Map<String, Collection<String>> acTypes = new LinkedHashMap<String, Collection<String>>();

			// Get the DAO and the script
			GetExamProfiles dao = new GetExamProfiles(con);
			if (key != null) {
				EquipmentRideScript sc = dao.getScript(key);
				if (sc == null)
					throw notFoundException("Invalid Check Ride script - " + key.getEquipmentType());

				// Calculate our access
				EquipmentRideScriptAccessControl access = new EquipmentRideScriptAccessControl(ctx, sc);
				access.validate();
				if (!access.getCanEdit())
					throw securityException("Cannot edit Check Ride script");

				// Save in the request
				ctx.setAttribute("script", sc, REQUEST);
				ctx.setAttribute("access", access, REQUEST);
				readAuditLog(ctx, sc);
				
				// Get primary equipment types
				EquipmentType eq = eqdao.get(sc.getProgram());
				if (eq != null) {
					ctx.setAttribute("eqTypes", Collections.singleton(eq), REQUEST);
					acTypes.put(eq.getName(), eq.getPrimaryRatings());
				} else {
					ctx.setAttribute("acTypes", acdao.getAircraftTypes(), REQUEST);
					ctx.setAttribute("eqTypes", allEQ, REQUEST);	
					allEQ.forEach(ep -> acTypes.put(ep.getName(), ep.getPrimaryRatings()));
				}
			} else {
				// Calculate our access
				EquipmentRideScriptAccessControl access = new EquipmentRideScriptAccessControl(ctx, null);
				access.validate();
				if (!access.getCanCreate())
					throw securityException("Cannot create Check Ride script");

				// Save in request
				ctx.setAttribute("access", access, REQUEST);
				
				// Save all equipment types
				allEQ.forEach(ep -> acTypes.put(ep.getName(), ep.getPrimaryRatings()));
				ctx.setAttribute("eqTypes", allEQ, REQUEST);
			}
			
			ctx.setAttribute("acTypes", acTypes, REQUEST);
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
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}