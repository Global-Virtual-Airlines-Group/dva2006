// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display Pilot accomplishments.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PilotRecognitionCommand extends AbstractCommand {

	 /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO
			GetStatusUpdate dao = new GetStatusUpdate(con);
			dao.setQueryMax(10);
			
			// Get the status data
			Collection<Integer> IDs = new HashSet<Integer>();
			Map<String, Collection<StatusUpdate>> updates = new HashMap<String, Collection<StatusUpdate>>();
			
			// Get promotions and rank changes
			updates.put("promotions", dao.getByType(StatusUpdate.EXTPROMOTION));
			updates.put("rankChanges", dao.getByType(StatusUpdate.INTPROMOTION));
			updates.put("ratingChanges", dao.getByType(StatusUpdate.RATING_ADD));
			updates.put("academyCerts", dao.getByType(StatusUpdate.CERT_ADD));
			updates.put("recognition", dao.getByType(StatusUpdate.RECOGNITION));
			
			// Get pilot IDs and save in the request
			for (Iterator<String> i = updates.keySet().iterator(); i.hasNext(); ) {
				String key = i.next();
				Collection<StatusUpdate> upds = updates.get(key);
				ctx.setAttribute(key, upds, REQUEST);
				
				// Save pilot IDs
				for (Iterator<StatusUpdate> ui = upds.iterator(); ui.hasNext(); ) {
					StatusUpdate upd = ui.next();
					IDs.add(new Integer(upd.getID()));
				}
			}
			
			// Load the pilot IDs
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			UserDataMap udm = uddao.get(IDs);
			ctx.setAttribute("pilots", pdao.get(udm), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/roster/pilotAccomplishments.jsp");
		result.setSuccess(true);
	}
}