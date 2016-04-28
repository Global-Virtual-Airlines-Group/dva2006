// Copyright 2005, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.Person;
import org.deltava.beans.assign.AssignmentInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.AssignmentAccessControl;

import org.deltava.util.ComboUtils;

/**
 * A Web Site Command to display a Pilot's Flight Assignments.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class MyAssignmentsCommand extends AbstractViewCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   @Override
public void execute(CommandContext ctx) throws CommandException {
      
      // Get the view context
      ViewContext vc = initView(ctx);
      try {
         // Get the DAO and the equipmentTypes
         GetAssignment dao = new GetAssignment(ctx.getConnection());
         ctx.setAttribute("eqTypes", dao.getEquipmentTypes(), REQUEST);
         
         // Set the query start/max
         dao.setQueryMax(vc.getCount());
         dao.setQueryStart(vc.getStart());
         
         // Get the assignments
         List<AssignmentInfo> results = dao.getByPilot(ctx.getUser().getID());
         vc.setResults(results);
         
         // Get the access controllers for the assignments
         List<AssignmentAccessControl> accessList = results.stream().map(ai -> { AssignmentAccessControl access = new AssignmentAccessControl(ctx, ai); access.validate(); return access; }).collect(Collectors.toList());
         
         // Save dummy map of pilot IDs - the only one we need to add is our own
         Map<Integer, Person> pilots = new HashMap<Integer, Person>();
         pilots.put(Integer.valueOf(ctx.getUser().getID()), ctx.getUser());
         ctx.setAttribute("pilots", pilots, REQUEST);
         
         // Save statuses and access controllers
         ctx.setAttribute("accessList", accessList, REQUEST);
         ctx.setAttribute("statuses", ComboUtils.fromArray(AssignmentInfo.STATUS), REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/assign/assignList.jsp");
      result.setSuccess(true);
   }
}