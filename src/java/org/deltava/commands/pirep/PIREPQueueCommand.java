// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display Flight Reports awaiting disposition.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class PIREPQueueCommand extends AbstractViewCommand {
	
	private static final Collection<Integer> PENDING = Arrays.asList(Integer.valueOf(FlightReport.SUBMITTED), 
			Integer.valueOf(FlightReport.HOLD));

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error (typically database) occurs
     */
	public void execute(CommandContext ctx) throws CommandException {
		
        // Get/set start/count parameters
        ViewContext vc = initView(ctx);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO
			GetFlightReports dao = new GetFlightReports(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			
			// Get the PIREPs and load the promotion type
			Collection<FlightReport> pireps = dao.getByStatus(PENDING);
			dao.getCaptEQType(pireps);
			
			// Get the Pilot IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Iterator<FlightReport> i = pireps.iterator(); i.hasNext(); ) {
				FlightReport fr = i.next();
				IDs.add(new Integer(fr.getDatabaseID(FlightReport.DBID_PILOT)));
			}
			
			// Load the Pilots
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
			
			// Load my equipment type
			GetEquipmentType eqdao = new GetEquipmentType(con);
			EquipmentType myEQ = eqdao.get(ctx.getUser().getEquipmentType());
			ctx.setAttribute("myEQ", myEQ, REQUEST);
			
			// Check if we display the scroll bar
			ctx.setAttribute("doScroll", Boolean.valueOf(pireps.size() >= vc.getCount()), REQUEST);
			
			// Split into my held PIREPs and my equipment PIREPs
			int id = ctx.getUser().getID();
			Collection<FlightReport> myEQType = new ArrayList<FlightReport>();
			Collection<FlightReport> myHeld = new ArrayList<FlightReport>();
			for (Iterator<FlightReport> i = pireps.iterator(); i.hasNext(); ) {
				FlightReport fr = i.next();
				if ((fr.getStatus() == FlightReport.HOLD) && (fr.getDatabaseID(FlightReport.DBID_DISPOSAL) == id)) {
					myHeld.add(fr);
					i.remove();
				} else if (myEQ.getPrimaryRatings().contains(fr.getEquipmentType())) {
					myEQType.add(fr);
					i.remove();
				}
			}
			
			// Save in request
			vc.setResults(pireps);
			ctx.setAttribute("myHeld", myHeld, REQUEST);
			ctx.setAttribute("myEQType", myEQType, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/pirepQueue.jsp");
		result.setSuccess(true);
	}
}