// Copyright (c) 2005 Delta Virtual Airlines. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.FileUpload;
import org.deltava.beans.event.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.EventAccessControl;

import org.deltava.util.ComboUtils;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

public class EventPlanCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command Context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Save the plan types
		ctx.setAttribute("planExts", Arrays.asList(FlightPlan.PLAN_EXT), REQUEST);
		ctx.setAttribute("planTypes", ComboUtils.fromArray(FlightPlan.PLAN_TYPE), REQUEST);
		
		// Get the command results
		CommandResult result = ctx.getResult();
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the event
			GetEvent dao = new GetEvent(con);
			Event ev = dao.get(ctx.getID());
			if (ev == null)
				throw new CommandException("Invalid Event - " + ctx.getID());
			
			// Check our access
			EventAccessControl access = new EventAccessControl(ctx, ev);
			access.validate();
			if (!access.getCanAddPlan())
				throw new CommandSecurityException("Cannot add Flight Plan");
			
			// Save the event in the request
			ctx.setAttribute("event", ev, REQUEST);
			
			// Mash the destination airport into a Collection so it'll fit in a combobox
			Set airportA = new HashSet();
			airportA.add(ev.getAirportA());
			ctx.setAttribute("airportA", airportA, REQUEST);
			
			// If we're not uploading, then forward to the JSP
			if (ctx.getParameter("airportA") == null) {
				ctx.release();
				result.setURL("/jsp/event/addPlan.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Get the flight plan
			FileUpload plan = ctx.getFile("planFile");
			if (plan == null)
				throw new CommandException("No Flight Plan Attached");
			
			// Validate the flight plan type
			String fExt = plan.getName().substring(plan.getName().lastIndexOf('.') + 1);
			int planType = StringUtils.arrayIndexOf(FlightPlan.PLAN_EXT, fExt.toLowerCase());
			if (planType == - 1)
				throw new CommandException("Unknown Flight Plan type - " + fExt);
			
			// Build the flight plan bean
			FlightPlan fp = new FlightPlan(planType);
			fp.setID(ev.getID());
			fp.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
			fp.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
			fp.load(plan.getBuffer());
			
			// Save the Flight Plan in the request
			ctx.setAttribute("flightPlan", fp, REQUEST);
			
			// Get the write DAO and save the Flight plan
			SetEvent wdao = new SetEvent(con);
			wdao.save(fp);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		result.setURL("/jsp/event/planUpdate.jsp");
		result.setType(CommandResult.REQREDIRECT);
		result.setSuccess(true);
	}
}