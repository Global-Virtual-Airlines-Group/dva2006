// Copyright 2005 Luke J. Kolin. All Rights Reserved.
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
 * @version 1.0
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
         NavigationDataBean nv = dao.get(code);
         ctx.setAttribute("navaid", nv, REQUEST);
         
         // Find all the navaids within 60 miles in each direction of this one
         if (nv != null) {
         	Map navaids = new HashMap();
         	navaids.putAll(dao.getObjects(nv, 140));
         	dao.setQueryMax(40);
         	navaids.putAll(dao.getIntersections(nv, 60));
         	navaids.keySet().remove(nv.getCode());
			ctx.setAttribute("navaids", navaids.values(), REQUEST);
         }
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/schedule/navaidInfo.jsp");
      result.setSuccess(true);
   }
}