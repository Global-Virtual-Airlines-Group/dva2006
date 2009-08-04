// Copyright 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.*;
import java.util.*;

import org.deltava.beans.schedule.Aircraft;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle Aircraft profiles.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class AircraftCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the Aircraft profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {

		// Get the aircraft code
		String aCode = (String) ctx.getCmdParameter(ID, null);
		boolean isNew = (aCode == null);
		try {
			Connection con = ctx.getConnection();
			
			// If we're editing an existing aircraft, load it
			Aircraft a = null; String oldName = null;
			if (!isNew) {
				GetAircraft dao = new GetAircraft(con);
				a = dao.get(aCode);
				if (a == null)
					throw notFoundException("Unknown Aircraft - " + aCode);

				oldName = a.getName();
				a.setName(ctx.getParameter("name"));
			} else
				a = new Aircraft(ctx.getParameter("name"));

			// Update the aircraft from the request
			a.setFullName(ctx.getParameter("fullName"));
			a.setRange(StringUtils.parse(ctx.getParameter("range"), 0));
			a.setMaxWeight(StringUtils.parse(ctx.getParameter("maxWeight"), 0));
			a.setMaxTakeoffWeight(StringUtils.parse(ctx.getParameter("maxTWeight"), 0));
			a.setMaxLandingWeight(StringUtils.parse(ctx.getParameter("maxLWeight"), 0));
			a.setIATA(StringUtils.split(ctx.getParameter("iataCodes"), "\n"));
			a.setHistoric(Boolean.valueOf(ctx.getParameter("isHistoric")).booleanValue());
			a.setETOPS(Boolean.valueOf(ctx.getParameter("isETOPS")).booleanValue());
			a.setEngines((byte) StringUtils.parse(ctx.getParameter("engineCount"), 2));
			a.setEngineType(ctx.getParameter("engineType"));
			a.setCruiseSpeed(StringUtils.parse(ctx.getParameter("cruiseSpeed"), 0));
			a.setFuelFlow(StringUtils.parse(ctx.getParameter("fuelFlow"), 0));
			a.setBaseFuel(StringUtils.parse(ctx.getParameter("baseFuel"), 0));
			a.setTaxiFuel(StringUtils.parse(ctx.getParameter("taxiFuel"), 0));
			a.setTanks(Aircraft.PRIMARY, ctx.getParameters("pTanks"));
			a.setPct(Aircraft.PRIMARY, StringUtils.parse(ctx.getParameter("pPct"), 100));
			a.setTanks(Aircraft.SECONDARY, ctx.getParameters("sTanks"));
			a.setPct(Aircraft.SECONDARY, StringUtils.parse(ctx.getParameter("sPct"), 0));
			a.setTanks(Aircraft.OTHER, ctx.getParameters("oTanks"));
			
			// Update the web applications
			a.clearApps();
			Collection<String> apps = ctx.getParameters("airlines");
			if (apps != null) {
				for (Iterator<String> i = apps.iterator(); i.hasNext(); ) {
					AirlineInformation ai = SystemData.getApp(i.next());
					a.addApp(ai);
				}
			}
			
			// Get the DAO and update the database
			SetSchedule wdao = new SetSchedule(con);
			if (isNew) {
				wdao.create(a);
				ctx.setAttribute("aircraftCreate", Boolean.TRUE, REQUEST);
			} else {
				wdao.update(a, oldName);
				ctx.setAttribute("aircraftUpdate", Boolean.TRUE, REQUEST);
			}
			
			// Save the aircraft in the request
			ctx.setAttribute("aircraft", a, REQUEST);
		} catch (DAOException de) {
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
	protected void execEdit(CommandContext ctx) throws CommandException {

		// Get the aircraft code
		String aCode = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = (aCode == null);

		// If we're editing an existing aircraft, load it
		if (!isNew) {
			try {
				Connection con = ctx.getConnection();
			
				// Get the DAO and the Aircraft
				GetAircraft dao = new GetAircraft(con);
				Aircraft a = dao.get(aCode);
				if (a == null)
					throw notFoundException("Unknown Aircraft - " + aCode);
				
				ctx.setAttribute("aircraft", a, REQUEST);
				ctx.setAttribute("iataCodes", StringUtils.listConcat(a.getIATA(), "\n"), REQUEST);
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
		}
		
		// Save tank names
		ctx.setAttribute("tankNames", ComboUtils.fromArray(Aircraft.TANK_NAMES), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/aircraftEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the Aircraft profile.
	 * @param ctx the Command context
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}