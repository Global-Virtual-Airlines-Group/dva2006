// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;

import org.deltava.beans.testing.CheckRideScript;

import org.deltava.commands.*;

import org.deltava.dao.GetExamProfiles;
import org.deltava.dao.DAOException;

import org.deltava.security.command.CheckrideScriptAccessControl;

/**
 * A Web Site Command to display Check Ride scripts. 
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class CheckRideScriptsCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {
      
      List<CheckRideScript> results = null;
      try {
         GetExamProfiles dao = new GetExamProfiles(ctx.getConnection());
         results = dao.getScripts();
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Create a map of access controllers
      Map<String, CheckrideScriptAccessControl> accessMap = new HashMap<String, CheckrideScriptAccessControl>();
      
      // Check create access
      CheckrideScriptAccessControl access = new CheckrideScriptAccessControl(ctx, null);
      access.validate();
      accessMap.put("NEW", access);

      // Check edit access
      for (Iterator<CheckRideScript> i = results.iterator(); i.hasNext(); ) {
         CheckRideScript sc = i.next();
         access = new CheckrideScriptAccessControl(ctx, sc);
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