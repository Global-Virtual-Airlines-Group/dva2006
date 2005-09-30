// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.assign.*;
import org.deltava.commands.*;

/**
 * A Web Site Command to build a Flight Assignment.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class BuildAssignmentCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {

        // Get the command results and set the default return operation
        CommandResult result = ctx.getResult();
        result.setType(CommandResult.REDIRECT);
        result.setURL("findflight.do");

        // Figure out if we're just adding to or clearing the session attribute
        String opName = (String) ctx.getCmdParameter(Command.OPERATION, "build");

        // If we're reseting the session, clear stuff out and return back
        if ("reset".equals(opName)) {
            ctx.getSession().removeAttribute("buildAssign");
            ctx.getSession().removeAttribute("fafCriteria");
            ctx.getSession().removeAttribute("fafResults");

            // Redirect back to the find-a-flight command
            result.setSuccess(true);
            return;
        }

        // If we're adding flights to the in-session assignment
        if ("build".equals(opName)) {
        		List results = (List) ctx.getSession().getAttribute("fafResults");
            String[] ids = ctx.getRequest().getParameterValues("addFA");
            if ((ids == null) || (results == null)) {
            	result.setSuccess(true);
            	return;
            }
            
            // Get the flight codes to add
            List selected = Arrays.asList(ids);

            // Get the list of results and split into two - the selected, and those remaining
            List fList = new ArrayList();
            for (Iterator i = results.iterator(); i.hasNext();) {
                Flight f = (Flight) i.next();
                if (selected.contains(f.getFlightCode())) {
                    fList.add(f);
                    i.remove();
                }
            }

            // Check for the flight assignment
            AssignmentInfo info = (AssignmentInfo) ctx.getSession().getAttribute("buildAssign");

            // Build the flight assignment and save in the session
            if (!fList.isEmpty())
                ctx.setAttribute("buildAssign", build(ctx.getUser(), info, fList), SESSION);

            // Redirect back to the find-a-flight command
            result.setSuccess(true);
            return;
        }

        // If we got this far, it's an unknown opName
        throw new CommandException("Invalid Operation");
    }

    /**
     * Helper method to build a Flight Assignment from the request.
     */
    private AssignmentInfo build(Person p, AssignmentInfo assign, List flights) {

        // Get the first flight for the eq type
        Flight ff = (Flight) flights.get(0);

        // Create the assignment if it doesn't exist
        if (assign == null) {
            assign = new AssignmentInfo(ff.getEquipmentType());
            assign.setPilotID(p);
            assign.setStatus(AssignmentInfo.RESERVED);
            assign.setRandom(true);
            assign.setPurgeable(true);
            assign.setAssignDate(new Date());
        }

        // Populate the legs
        for (Iterator i = flights.iterator(); i.hasNext();) {
            Flight f = (Flight) i.next();
            assign.addAssignment(new AssignmentLeg(f));
            assign.addFlight(new FlightReport(f));
        }

        // Return the assignment
        return assign;
    }
}