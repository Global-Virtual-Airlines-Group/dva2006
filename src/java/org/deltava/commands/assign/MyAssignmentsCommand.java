// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Person;
import org.deltava.beans.assign.AssignmentInfo;

import org.deltava.commands.*;

import org.deltava.dao.GetAssignment;
import org.deltava.dao.DAOException;

import org.deltava.security.command.AssignmentAccessControl;

import org.deltava.util.ComboUtils;

/**
 * A Web Site Command to display a Pilot's Flight Assignments.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MyAssignmentsCommand extends AbstractViewCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {
      
      // Get the view context
      ViewContext vc = initView(ctx);

      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the equipmentTypes
         GetAssignment dao = new GetAssignment(con);
         ctx.setAttribute("eqTypes", dao.getEquipmentTypes(), REQUEST);
         
         // Set the query start/max
         dao.setQueryMax(vc.getCount());
         dao.setQueryStart(vc.getStart());
         
         // Get the assignments
         List<AssignmentInfo> results = dao.getByPilot(ctx.getUser().getID());
         vc.setResults(results);
         
         // Get the access controllers for the assignments
         List<AssignmentAccessControl> accessList = new ArrayList<AssignmentAccessControl>(results.size());
         for (Iterator<AssignmentInfo> i = results.iterator(); i.hasNext(); ) {
         	AssignmentInfo ai = i.next();
         	
         	// Calculate access to this flight assignment
            AssignmentAccessControl access = new AssignmentAccessControl(ctx, ai);
            access.validate();
            accessList.add(access);
         }
         
         // Save dummy map of pilot IDs - the only one we need to add is our own
         Map<Integer, Person> pilots = new HashMap<Integer, Person>();
         pilots.put(new Integer(ctx.getUser().getID()), ctx.getUser());
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