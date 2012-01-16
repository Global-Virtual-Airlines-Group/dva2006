// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.Date;
import java.sql.Connection;

import org.deltava.beans.assign.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.ScheduleEntry;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to build multi-leg flight assignments.
 * @author Luke
 * @version 4.1
 * @since 4.1
 */

public class RouteAssignmentBuildCommand extends AbstractCommand {
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Create the bean
		AssignmentInfo info = new AssignmentInfo(ctx.getUser().getEquipmentType());
		info.setPilotID(ctx.getUser());
		info.setAssignDate(new Date());
		info.setRandom(true);
		info.setPurgeable(true);
		info.setStatus(AssignmentInfo.RESERVED);
		
		// Get equipment override
		String eqOv = ctx.getParameter("eqOverride");
		if ((eqOv != null) && (eqOv.length() < 3))
			eqOv = null;
		
		int legCount = StringUtils.parse(ctx.getParameter("legCount"), 1);
		try {
			Connection con = ctx.getConnection();
			
			// Get the legs
			GetSchedule sdao = new GetSchedule(con);
			for (int leg = 1; leg <= legCount; leg++) {
				String fCode = ctx.getParameter("leg" + leg);
				if (StringUtils.isEmpty(fCode))
					break;

				// Get the schedule entry and create the leg
				ScheduleEntry se = sdao.get(FlightCodeParser.parse(fCode));
				DraftFlightReport dfr = new DraftFlightReport(se);
				dfr.setTimeD(se.getTimeD());
				dfr.setTimeA(se.getTimeA());
				dfr.setRank(ctx.getUser().getRank());
				dfr.setDate(new Date());
				dfr.setEquipmentType((eqOv == null) ? se.getEquipmentType() : eqOv);
				
				// Add to the assignment
				info.addAssignment(new AssignmentLeg(se));
				info.addFlight(dfr);
			}
			
			// Start the transaction
			ctx.startTX();
			
			// Write the assignment
			SetAssignment awdao = new SetAssignment(con);
			awdao.write(info, SystemData.get("airline.db"));
			awdao.assign(info, info.getPilotID(), SystemData.get("airline.db"));
			
			// Write the flight reports
			SetFlightReport pwdao = new SetFlightReport(con);
			for (FlightReport fr : info.getFlights())
				pwdao.write(fr);

			// Commit
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status variables
		ctx.setAttribute("pilot", ctx.getUser(), REQUEST);
		ctx.setAttribute("isCreate", Boolean.TRUE, REQUEST);
		ctx.setAttribute("assign", info, REQUEST);
		ctx.setAttribute("pirepsWritten", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/assign/assignUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}