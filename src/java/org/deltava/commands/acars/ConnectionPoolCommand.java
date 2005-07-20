// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.acars;

import org.deltava.beans.acars.ACARSAdminInfo;

import org.deltava.commands.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display ACARS connection pool information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ConnectionPoolCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error (typically database) occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Get the ACARS Connection pool data and save in the request
      ACARSAdminInfo acarsPool = (ACARSAdminInfo) SystemData.getObject(SystemData.ACARS_POOL);
      ctx.setAttribute("acarsPool", acarsPool.getPoolInfo(), REQUEST);

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/acars/poolInfo.jsp");
      result.setSuccess(true);
   }
}