// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.UserDataMap;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.ComboUtils;

/**
 * A Web Site Command to display empty ACARS log entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EmptyLogEntriesCommand extends ACARSLogViewCommand {
   
   private static final List LIST_OPTIONS = ComboUtils.fromArray(new String[] {"Flight Information", "ACARS Connections"},
         new String[] {"info", "con"});

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
   public void execute(CommandContext ctx) throws CommandException {

      // Find out what log entry type we are displaying
      boolean isInfo = "info".equals(ctx.getCmdParameter(ID, "info"));
      ViewContext vc = initView(ctx);
      
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO
         GetACARSLog dao = new GetACARSLog(con);
         dao.setQueryStart(vc.getStart());
         dao.setQueryMax(vc.getCount());
         
         // Get the empty connections
         vc.setResults(isInfo ? dao.getUnreportedFlights(18) : dao.getUnusedConnections(18));
         
         // Load the Pilot data
         GetUserData usrdao = new GetUserData(con);
         UserDataMap udm = usrdao.get(getPilotIDs(vc.getResults()));
         ctx.setAttribute("userData", udm, REQUEST);
         
			// Get the authors for each log entry
         	Map<Integer, Pilot> pilots = new HashMap<Integer, Pilot>();
			GetPilot pdao = new GetPilot(con);
			for (Iterator<String> i = udm.getTableNames().iterator(); i.hasNext(); ) {
				String dbTableName = i.next();
				pilots.putAll(pdao.getByID(udm.getByTable(dbTableName), dbTableName));
			}

			// Save the pilots in the request
         ctx.setAttribute("pilots", pilots, REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Save the display options
      ctx.setAttribute("displayType", ctx.getCmdParameter(ID, "info"), REQUEST);
      ctx.setAttribute("displayTypes", LIST_OPTIONS, REQUEST);
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL(isInfo ? "/jsp/acars/emptyFlights.jsp" : "/jsp/acars/emptyCons.jsp");
      result.setSuccess(true);
   }
}