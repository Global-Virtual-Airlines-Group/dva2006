// Copyright 2005, 2006, 2007, 2010, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display Pilot accomplishments.
 * @author Luke
 * @version 8.7
 * @since 1.0
 */

public class PilotRecognitionCommand extends AbstractCommand {

	 /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
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
			updates.put("promotions", dao.getByType(UpdateType.EXTPROMOTION));
			updates.put("rankChanges", dao.getByType(UpdateType.INTPROMOTION));
			updates.put("ratingChanges", dao.getByType(UpdateType.RATING_ADD));
			updates.put("academyCerts", dao.getByType(UpdateType.CERT_ADD));
			updates.put("recognition", dao.getByType(UpdateType.RECOGNITION));
			
			// Get pilot IDs and save in the request
			for (Map.Entry<String, Collection<StatusUpdate>> me : updates.entrySet()) {
				ctx.setAttribute(me.getKey(), me.getValue(), REQUEST);
				me.getValue().forEach(upd -> IDs.add(Integer.valueOf(upd.getID())));
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