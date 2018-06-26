// Copyright 2005, 2016, 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.assign.AssignmentInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.AssignmentAccessControl;

/**
 * A Web Site Command to display a Pilot's Flight Assignments.
 * @author Luke
 * @version 8.3
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
      ViewContext<AssignmentInfo> vc = initView(ctx, AssignmentInfo.class);
      try {
         // Get the DAO and the equipmentTypes
         GetAssignment dao = new GetAssignment(ctx.getConnection());
         ctx.setAttribute("eqTypes", dao.getEquipmentTypes(), REQUEST);
         
         // Get the assignments
         dao.setQueryMax(vc.getCount());
         dao.setQueryStart(vc.getStart());
         vc.setResults(dao.getByPilot(ctx.getUser().getID(), null));
         
         // Get the access controllers for the assignments
         List<AssignmentAccessControl> accessList = vc.getResults().stream().map(ai -> { AssignmentAccessControl access = new AssignmentAccessControl(ctx, ai); access.validate(); return access; }).collect(Collectors.toList());
         ctx.setAttribute("accessList", accessList, REQUEST);
         
         // Save dummy map of pilot IDs - the only one we need to add is our own
         ctx.setAttribute("pilots", Collections.singletonMap(Integer.valueOf(ctx.getUser().getID()), ctx.getUser()), REQUEST);
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