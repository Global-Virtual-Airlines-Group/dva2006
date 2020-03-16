// Copyright 2005, 2006, 2008, 2010, 2015, 2016, 2019, 2020 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.commands.schedule;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site command to export raw Flight Schedule data in CSV format.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class ScheduleExportCommand extends AbstractCommand {

	/**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
	@Override
   public void execute(CommandContext ctx) throws CommandException {
      try {
    	  GetRawSchedule dao = new GetRawSchedule(ctx.getConnection());
    	  ctx.setAttribute("srcInfo", dao.getSources(true), REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/schedule/flightExport.jsp");
      result.setSuccess(true);
   }
}