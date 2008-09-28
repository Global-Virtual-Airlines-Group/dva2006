// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.academy.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.InstructionAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to log Flight Academy instruction flights.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InstructionFlightCommand extends AbstractFormCommand {
	
	private static Collection<String> _flightTimes;
	
	/**
	 * Initialize the command.
	 * @param id the Command ID
	 * @param cmdName the name of the Command
	 */
	public void init(String id, String cmdName) throws CommandException {
		super.init(id, cmdName);
		
		// Initialize flight times
		if (_flightTimes == null) {
			_flightTimes = new LinkedHashSet<String>();
			for (int x = 2; x < 168; x++)
				_flightTimes.add(String.valueOf(x / 10.0d));
		}
	}

	/**
	 * Method called when saving the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {

		// Check if we're creating a new entry
		boolean isNew = (ctx.getID() == 0);
		try {
			Connection con = ctx.getConnection();
			
			// Load the entry if not new
			InstructionFlight flight = null;
			if (!isNew) {
				GetAcademyCalendar cdao = new GetAcademyCalendar(con);
				flight = cdao.getFlight(ctx.getID());
				if (flight == null)
					throw notFoundException("Invalid Flight Log ID - " + ctx.getID());
			} else {
				flight = new InstructionFlight(1, StringUtils.parseHex(ctx.getParameter("courseID")));
			}
			
			// Update fields from the request
			flight.setInstructorID(StringUtils.parseHex(ctx.getParameter("instructor")));
			flight.setComments(ctx.getParameter("comments"));
			flight.setEquipmentType(ctx.getParameter("eqType"));
			flight.setDate(parseDateTime(ctx, "log", "MM/dd/yyyy", "HH:mm"));
			
			// Get the flight time
			try {
				float fTime = Float.parseFloat(ctx.getParameter("flightTime"));
				flight.setLength(Math.round(fTime * 10));
			} catch (NumberFormatException nfe) {
				CommandException ce = new CommandException("Invalid Flight Time", false);
				throw ce;
			}
			
			// Update the calendar
			SetAcademyCalendar wdao = new SetAcademyCalendar(con);
			wdao.write(flight);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("flightUpdate", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/academy/courseUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when editing the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {
		
		// Check if we're creating a new entry
		boolean isNew = (ctx.getID() == 0);
		
		// Get the current date/time in the user's local zone
		Calendar cld = Calendar.getInstance();
		TZInfo tz = ctx.isAuthenticated() ? ctx.getUser().getTZ() : TZInfo.get(SystemData.get("time.timezone"));
		cld.setTime(DateTime.convert(cld.getTime(), tz));
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the log bean
			InstructionAccessControl access = null;
			InstructionFlight flight = null;
			Course c = null;
			if (!isNew) {
				GetAcademyCalendar cdao = new GetAcademyCalendar(con);
				flight = cdao.getFlight(ctx.getID());
				if (flight == null)
					throw notFoundException("Invalid Flight Log ID - " + ctx.getID());
			
				// Get the Course
				GetAcademyCourses dao = new GetAcademyCourses(con);
				c = dao.get(flight.getCourseID());
				if (c == null)
					throw notFoundException("Invalid Course ID - " + flight.getCourseID());
				
				// Check our Access
				access = new InstructionAccessControl(ctx, flight);
				access.validate();
				if (!access.getCanEdit())
					throw securityException("Cannot edit flight log");
				
				// Save the flight bean
				ctx.setAttribute("flight", flight, REQUEST);
			} else {
				GetAcademyCourses dao = new GetAcademyCourses(con);
				c = dao.get(StringUtils.parseHex(ctx.getParameter("courseID")));
				if (c == null)
					throw notFoundException("Invalid Course ID - " + ctx.getParameter("courseID"));
				
				// Populate the flight bean
				flight = new InstructionFlight(ctx.getUser().getID(), c.getID());
				
				// Check our Access
				access = new InstructionAccessControl(ctx, flight);
				access.validate();
				if (!access.getCanCreate())
					throw securityException("Cannot create flight log");
			}
			
			// Make sure we are updating our own entry
			if (!ctx.isUserInRole("HR")) {
				if (flight.getInstructorID() != ctx.getUser().getID())
					throw securityException("Cannot update other Instructor's flight log");
			}
			
			// Set PIREP date and length
			cld.setTime(DateTime.convert((flight.getDate() == null) ? new Date() : flight.getDate(), ctx.getUser().getTZ()));
			ctx.setAttribute("flightTime", StringUtils.format(flight.getLength() / 10.0, "#0.0"), REQUEST);
			
			// Load the Instructor/Course data
			Collection<Integer> IDs = new HashSet<Integer>();
			IDs.add(new Integer(flight.getInstructorID()));
			IDs.add(new Integer(c.getPilotID()));
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
			
			// Load instructor lists
			if (ctx.isUserInRole("HR")) {
				List<Pilot> insList = pdao.getByRole("Instructor", SystemData.get("airline.db"));
				insList.addAll(pdao.getByRole("HR", SystemData.get("airline.db")));
				ctx.setAttribute("instructors", insList, REQUEST);	
			} else {
				Collection<Person> instructors = new HashSet<Person>();
				instructors.add(ctx.getUser());
				ctx.setAttribute("instructors", instructors, REQUEST);
			}
			
			// Save aircraft types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("eqTypes", acdao.getAircraftTypes(), REQUEST);
			
			// Save course/access data
			ctx.setAttribute("course", c, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set list options
		ctx.setAttribute("flightTimes", _flightTimes, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/flightEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when reading the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execRead(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();
			
			// Get the log bean
			GetAcademyCalendar cdao = new GetAcademyCalendar(con);
			InstructionFlight flight = cdao.getFlight(ctx.getID());
			if (flight == null)
				throw notFoundException("Invalid Flight Log ID - " + ctx.getID());
			
			// Get the Course
			GetAcademyCourses dao = new GetAcademyCourses(con);
			Course c = dao.get(flight.getCourseID());
			if (c == null)
				throw notFoundException("Invalid Course ID - " + flight.getCourseID());
			
			// Check our access
			InstructionAccessControl access = new InstructionAccessControl(ctx, flight);
			access.validate();
			ctx.setAttribute("access", access, REQUEST);

			// Load the Instructor/Course data
			Collection<Integer> IDs = new HashSet<Integer>();
			IDs.add(new Integer(flight.getInstructorID()));
			IDs.add(new Integer(c.getPilotID()));
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);

			// Save flight/course data
			ctx.setAttribute("flight", flight, REQUEST);
			ctx.setAttribute("course", c, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/flightView.jsp");
		result.setSuccess(true);
	}
}