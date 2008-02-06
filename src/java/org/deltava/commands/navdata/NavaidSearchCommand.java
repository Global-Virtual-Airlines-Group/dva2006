// Copyright 2005, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.navdata.*;
import org.deltava.commands.*;

import org.deltava.dao.GetNavData;
import org.deltava.dao.DAOException;

/**
 * A Web Site Command to search Naivgation Data.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class NavaidSearchCommand extends AbstractCommand {
   
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
   public void execute(CommandContext ctx) throws CommandException {
      
      // Get the navaid to search for
      String code = ctx.getParameter("navaidCode");
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and find the navaid
         GetNavData dao = new GetNavData(con);
         NavigationDataMap ndMap = dao.get(code);
         if (ndMap == null)
        	 ndMap = new NavigationDataMap();
         
         // Save results
         ctx.setAttribute("navaid", ndMap.get(code), REQUEST);
         ctx.setAttribute("results", ndMap.getAll(), REQUEST);

         // Don't do search if the navaid was not found
         Collection<NavigationDataBean> navaids = new HashSet<NavigationDataBean>();
         for (Iterator<NavigationDataBean> i = ndMap.getAll().iterator(); i.hasNext(); ) {
        	 NavigationDataBean nv = i.next();
        	 Map<String, NavigationDataBean> nMap = new HashMap<String, NavigationDataBean>();
        	 
          	// Get major items within 70 miles, and all minor items
          	nMap.putAll(dao.getObjects(nv, 140));
          	nMap.putAll(dao.getIntersections(nv, 60));

         	// Remove the primary result
         	nMap.remove(nv.getCode());
         	
         	// Add to results
         	navaids.addAll(nMap.values());
         }
         
         // Save in the request
         ctx.setAttribute("navaids", navaids, REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/navdata/navaidInfo.jsp");
      result.setSuccess(true);
   }
}