// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.Connection;

import org.deltava.beans.schedule.*;
import org.deltava.commands.*;

import org.deltava.dao.GetSchedule;
import org.deltava.dao.DAOException;

import org.deltava.util.system.SystemData;

/**
 * A Web Site command to search the Flight Schedule.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FindFlightCommand extends AbstractCommand {

    /**
     * Helper method to parse a numeric request parameter.
     */
    private int parse(String param) {
        if ("".equals(param))
            return 0;
        try {
            return Integer.parseInt(param);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error (typically database) occurs
     */
    public void execute(CommandContext ctx) throws CommandException {

        // Get the result JSP and redirect if we're not posting
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/schedule/findAflight.jsp");
        if (ctx.getParameter("eqType") == null) {
            result.setSuccess(true);
            return;
        }

        // Populate the search criteria from the request
        Airline a = SystemData.getAirline(ctx.getParameter("airline"));
        ScheduleSearchCriteria criteria = new ScheduleSearchCriteria(a, parse(ctx.getParameter("flightNumber")),
                parse(ctx.getParameter("flightLeg")));
        criteria.setEquipmentType(ctx.getParameter("eqType"));
        criteria.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
        criteria.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
        criteria.setDistance(parse(ctx.getParameter("distance")));
        criteria.setMaxResults(parse(ctx.getParameter("maxResults")));
        if ((criteria.getMaxResults() == 0) || (criteria.getMaxResults() > 150))
            criteria.setMaxResults(100);
        //criteria.setLength((int) (Double.parseDouble(ctx.getParameter("flightTime")) * 10));

        // Save the search criteria in the session
        ctx.setAttribute("fafCriteria", criteria, SESSION);

        // Check if we're doing a new search or returning back existing criteria
        String opName = (String) ctx.getCmdParameter(Command.OPERATION, "search");
        if (opName.equals("search")) {
            try {
                Connection con = ctx.getConnection();

                // Get the DAO and execute
                GetSchedule dao = new GetSchedule(con);
                dao.setQueryMax(criteria.getMaxResults());

                // Save results in the session - since other commands may reference these
                ctx.setAttribute("fafResults", dao.search(criteria, true), SESSION);
            } catch (DAOException de) {
                throw new CommandException(de);
            } finally {
                ctx.release();
            }
        }

        // Forward to the JSP
        result.setSuccess(true);
    }
}