// Copyright 2006, 2007, 2008, 2009, 2011, 2012, 2016, 2017, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.*;
import java.util.*;

import org.deltava.beans.AuditLog;
import org.deltava.beans.flight.ETOPS;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.AircraftAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle Aircraft profiles.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class AircraftCommand extends AbstractAuditFormCommand {

	/**
	 * Callback method called when saving the Aircraft profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {

		// Get the aircraft code
		String aCode = (String) ctx.getCmdParameter(ID, null);
		boolean isNew = (aCode == null);
		try {
			Connection con = ctx.getConnection();
			
			// If we're editing an existing aircraft, load it
			Aircraft a = null; Aircraft oa = null; String oldName = null;
			if (!isNew) {
				GetAircraft dao = new GetAircraft(con);
				a = dao.get(aCode);
				if (a == null)
					throw notFoundException("Unknown Aircraft - " + aCode);

				oa = BeanUtils.clone(a); oldName = a.getName();
				a.setName(ctx.getParameter("name"));
			} else
				a = new Aircraft(ctx.getParameter("name"));
			
			// Check access control
			AircraftAccessControl ac = new AircraftAccessControl(ctx, isNew ? null : a);
			ac.validate();
			boolean acRole = isNew ? ac.getCanCreate() : ac.getCanEdit();
			if (!acRole)
				throw securityException("Cannot create/edit Aircraft profile");

			// Update the aircraft from the request
			a.setFullName(ctx.getParameter("fullName"));
			a.setFamily(ctx.getParameter("family"));
			a.setMaxWeight(StringUtils.parse(ctx.getParameter("maxWeight"), 0));
			a.setMaxZeroFuelWeight(StringUtils.parse(ctx.getParameter("maxZFW"), 0));
			a.setMaxTakeoffWeight(StringUtils.parse(ctx.getParameter("maxTWeight"), 0));
			a.setMaxLandingWeight(StringUtils.parse(ctx.getParameter("maxLWeight"), 0));
			a.setIATA(StringUtils.split(ctx.getParameter("iataCodes"), "\n"));
			a.setICAO(ctx.getParameter("icao"));
			a.setHistoric(Boolean.valueOf(ctx.getParameter("isHistoric")).booleanValue());
			a.setAcadedmyOnly(Boolean.valueOf(ctx.getParameter("academyOnly")).booleanValue());
			a.setEngines((byte) StringUtils.parse(ctx.getParameter("engineCount"), 2));
			a.setEngineType(ctx.getParameter("engineType"));
			a.setCruiseSpeed(StringUtils.parse(ctx.getParameter("cruiseSpeed"), 0));
			a.setFuelFlow(StringUtils.parse(ctx.getParameter("fuelFlow"), 0));
			a.setBaseFuel(StringUtils.parse(ctx.getParameter("baseFuel"), 0));
			a.setTaxiFuel(StringUtils.parse(ctx.getParameter("taxiFuel"), 0));
			a.setTanks(TankType.PRIMARY, ctx.getParameters("pTanks"));
			a.setPct(TankType.PRIMARY, StringUtils.parse(ctx.getParameter("pPct"), 100));
			a.setTanks(TankType.SECONDARY, ctx.getParameters("sTanks"));
			a.setPct(TankType.SECONDARY, StringUtils.parse(ctx.getParameter("sPct"), 0));
			a.setTanks(TankType.OTHER, ctx.getParameters("oTanks"));
			
			// Update the web applications
			boolean useWithApp = Boolean.valueOf(ctx.getParameter("useAircraft")).booleanValue();
			if (useWithApp) {
				AircraftPolicyOptions opts = new AircraftPolicyOptions(a.getName(), SystemData.get("airline.code"));
				opts.setRange(StringUtils.parse(ctx.getParameter("range"), 0));
				opts.setETOPS((a.getEngines() == 2) ? ETOPS.values()[StringUtils.parse(ctx.getParameter("etops"), ETOPS.INVALID.ordinal())] : ETOPS.INVALID);
				opts.setUseSoftRunways(Boolean.valueOf(ctx.getParameter("useSoftRwy")).booleanValue());
				opts.setSeats(StringUtils.parse(ctx.getParameter("seats"), 0));
				opts.setTakeoffRunwayLength(StringUtils.parse(ctx.getParameter("toRunwayLength"), 0));
				opts.setLandingRunwayLength(StringUtils.parse(ctx.getParameter("lndRunwayLength"), 0));
				a.addApp(opts);
			} else
				a.removeApp(SystemData.get("airline.code"));
			
			// Check audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(oa, a);
			AuditLog ae = AuditLog.create(a, delta, ctx.getUser().getID());
			
			// Start transaction
			ctx.startTX();
			
			// Get the DAO and update the database
			SetAirportAirline wdao = new SetAirportAirline(con);
			if (isNew) {
				wdao.create(a);
				ctx.setAttribute("aircraftCreate", Boolean.TRUE, REQUEST);
			} else {
				wdao.update(a, oldName);
				ctx.setAttribute("aircraftUpdate", Boolean.TRUE, REQUEST);
			}
			
			// Write audit log
			writeAuditLog(ctx, ae);
			ctx.commitTX();
			
			// Save the aircraft in the request
			ctx.setAttribute("aircraft", a, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("aircraftList.do");
	}

	/**
	 * Callback method called when editing the Aircraft profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {

		// Get the aircraft code
		String aCode = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = (aCode == null);

		// If we're editing an existing aircraft, load it
		if (!isNew) {
			try {
				GetAircraft dao = new GetAircraft(ctx.getConnection());
				Aircraft a = dao.get(aCode);
				if (a == null)
					throw notFoundException("Unknown Aircraft - " + aCode);
				
				readAuditLog(ctx, a);
				
				ctx.setAttribute("aircraft", a, REQUEST);
				ctx.setAttribute("opts", a.getOptions(SystemData.get("airline.code")), REQUEST);
				ctx.setAttribute("iataCodes", StringUtils.listConcat(a.getIATA(), "\n"), REQUEST);
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/aircraftEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the Aircraft profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		String aCode = (String) ctx.getCmdParameter(Command.ID, null);
		try {
			GetAircraft dao = new GetAircraft(ctx.getConnection());
			Aircraft a = dao.get(aCode);
			if (a == null)
				throw notFoundException("Unknown Aircraft - " + aCode);
			
			// Check access control
			AircraftAccessControl ac = new AircraftAccessControl(ctx, a);
			ac.validate();
			if (!ac.getCanRead())
				throw securityException("Cannot read Aircraft profile");

			// Save request variables
			readAuditLog(ctx, a);
			AircraftPolicyOptions opts = a.getOptions(SystemData.get("airline.code"));
			ctx.setAttribute("aircraft", a, REQUEST);
			ctx.setAttribute("opts", opts, REQUEST);
			ctx.setAttribute("isETOPS", Boolean.valueOf(opts.getETOPS() != ETOPS.INVALID), REQUEST);
			ctx.setAttribute("access", ac, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/aircraftView.jsp");
		result.setSuccess(true);
	}
}