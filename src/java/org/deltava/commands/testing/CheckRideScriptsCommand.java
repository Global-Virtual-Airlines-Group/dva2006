// Copyright 2005, 2009, 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;

import org.deltava.beans.testing.EquipmentRideScript;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.security.command.*;

/**
 * A Web Site Command to display Equipment Program Check Ride scripts. 
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class CheckRideScriptsCommand extends AbstractViewCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   @Override
public void execute(CommandContext ctx) throws CommandException {
      
      ViewContext<EquipmentRideScript> vctx = initView(ctx, EquipmentRideScript.class);
      try {
         GetExamProfiles dao = new GetExamProfiles(ctx.getConnection());
         dao.setQueryStart(vctx.getStart());
         dao.setQueryMax(vctx.getCount());
         vctx.setResults(dao.getScripts());
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Create a map of access controllers
      Map<String, EquipmentRideScriptAccessControl> accessMap = new HashMap<String, EquipmentRideScriptAccessControl>();
      EquipmentRideScriptAccessControl access = new EquipmentRideScriptAccessControl(ctx, null);
      access.validate();
      accessMap.put("NEW", access);

      // Check edit access
      for (Iterator<EquipmentRideScript> i = vctx.getResults().iterator(); i.hasNext(); ) {
    	  EquipmentRideScript sc = i.next();
    	  access = new EquipmentRideScriptAccessControl(ctx, sc);
    	  try {
    		  access.validate();
    		  accessMap.put(sc.getEquipmentType(), access);
    	  } catch (AccessControlException ace) {
    		  i.remove();
    	  }
      }
      
      // Save in request
      ctx.setAttribute("accessMap", accessMap, REQUEST);
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/testing/crScripts.jsp");
      result.setSuccess(true);
   }
}