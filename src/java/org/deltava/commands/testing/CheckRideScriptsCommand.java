// Copyright 2005, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;

import org.deltava.beans.testing.EquipmentRideScript;

import org.deltava.commands.*;

import org.deltava.dao.GetExamProfiles;
import org.deltava.dao.DAOException;

import org.deltava.security.command.EquipmentRideScriptAccessControl;

/**
 * A Web Site Command to display Equipment Program Check Ride scripts. 
 * @author Luke
 * @version 3.4
 * @since 1.0
 */

public class CheckRideScriptsCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {
      
      List<EquipmentRideScript> results = null;
      try {
         GetExamProfiles dao = new GetExamProfiles(ctx.getConnection());
         results = dao.getScripts();
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Create a map of access controllers
      Map<String, EquipmentRideScriptAccessControl> accessMap = new HashMap<String, EquipmentRideScriptAccessControl>();
      
      // Check create access
      EquipmentRideScriptAccessControl access = new EquipmentRideScriptAccessControl(ctx, null);
      access.validate();
      accessMap.put("NEW", access);

      // Check edit access
      for (Iterator<EquipmentRideScript> i = results.iterator(); i.hasNext(); ) {
    	  EquipmentRideScript sc = i.next();
         access = new EquipmentRideScriptAccessControl(ctx, sc);
         access.validate();
         accessMap.put(sc.getEquipmentType(), access);
      }
      
      // Save in request
      ctx.setAttribute("results", results, REQUEST);
      ctx.setAttribute("accessMap", accessMap, REQUEST);
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/testing/crScripts.jsp");
      result.setSuccess(true);
   }
}