// Copyright 2005, 2009, 2016, 2017, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.assign.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.AssignmentAccessControl;

import org.deltava.util.EnumUtils;

/**
 * A Web Site Command to list Flight Assignments.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class AssignmentListCommand extends AbstractViewCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   @Override
   public void execute(CommandContext ctx) throws CommandException {

      // Get the view context
      ViewContext<AssignmentInfo> vc = initView(ctx, AssignmentInfo.class);
      
      // Get status and equipment type
      AssignmentStatus as = EnumUtils.parse(AssignmentStatus.class, ctx.getParameter("status"), null);
      String eqType = ctx.getParameter("eqType");
      if ("-".equals(eqType))
    	  eqType = null;

      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and save equipment types before we set queryMax/Start
         GetAssignment dao = new GetAssignment(con);
         ctx.setAttribute("eqTypes", dao.getEquipmentTypes(), REQUEST);
         
         // Set the start/count
         dao.setQueryMax(vc.getCount());
         dao.setQueryStart(vc.getStart());
         
         // Figure out what call to make
         if ((as != null) && (eqType == null))
            vc.setResults(dao.getByStatus(as));
         else if (eqType != null)
            vc.setResults(dao.getByEquipmentType(eqType, as));
         else
            vc.setResults(dao.getByStatus(AssignmentStatus.AVAILABLE));
         
         // Build a Collection of access controllers and Pilot IDs
         Collection<Integer> pilotIDs = new HashSet<Integer>();
         List<AssignmentAccessControl> accessList = new ArrayList<AssignmentAccessControl>();
         for (AssignmentInfo ai : vc.getResults()) {
         	if (ai.getPilotID() != 0)
         		pilotIDs.add(Integer.valueOf(ai.getPilotID()));
         	
         	// Calculate access to this assignment
            AssignmentAccessControl access = new AssignmentAccessControl(ctx, ai);
            access.validate();
            accessList.add(access);
         }
         
         // Get Pilot data
         GetPilot pdao = new GetPilot(con);
         ctx.setAttribute("pilots", pdao.getByID(pilotIDs, "PILOTS"), REQUEST);
         
         // Save access controllers
         ctx.setAttribute("accessList", accessList, REQUEST);
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