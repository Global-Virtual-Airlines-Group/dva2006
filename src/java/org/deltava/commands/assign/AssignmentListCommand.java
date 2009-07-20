// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.assign.AssignmentInfo;

import org.deltava.commands.*;

import org.deltava.dao.GetAssignment;
import org.deltava.dao.GetPilot;
import org.deltava.dao.DAOException;

import org.deltava.security.command.AssignmentAccessControl;

import org.deltava.util.ComboUtils;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command to list Flight Assignments.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class AssignmentListCommand extends AbstractViewCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Get the view context
      ViewContext vc = initView(ctx);
      
      // Get status and equipment type
      String status = ctx.getParameter("status");
      int statusCode = StringUtils.arrayIndexOf(AssignmentInfo.STATUS, status);
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
         if ((status != null) && (eqType == null))
            vc.setResults(dao.getByStatus(statusCode));
         else if (eqType != null)
            vc.setResults(dao.getByEquipmentType(eqType, statusCode));
         else
            vc.setResults(dao.getByStatus(AssignmentInfo.AVAILABLE));
         
         // Build a Collection of access controllers and Pilot IDs
         Collection<Integer> pilotIDs = new HashSet<Integer>();
         List<AssignmentAccessControl> accessList = new ArrayList<AssignmentAccessControl>();
         for (Iterator<?> i = vc.getResults().iterator(); i.hasNext(); ) {
         	AssignmentInfo ai = (AssignmentInfo) i.next();
         	if (ai.getPilotID() != 0)
         		pilotIDs.add(new Integer(ai.getPilotID()));
         	
         	// Calculate access to this assignment
            AssignmentAccessControl access = new AssignmentAccessControl(ctx, ai);
            access.validate();
            
            // Add to list
            accessList.add(access);
         }
         
         // Get Pilot data
         GetPilot pdao = new GetPilot(con);
         ctx.setAttribute("pilots", pdao.getByID(pilotIDs, "PILOTS"), REQUEST);
         
         // Save statuses
         ctx.setAttribute("statuses", ComboUtils.fromArray(AssignmentInfo.STATUS), REQUEST);
         
         // Save the access controllers
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