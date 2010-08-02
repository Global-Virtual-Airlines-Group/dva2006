// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to check eligibility for particular Accomplishments.
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class AccomplishmentEligibilityCommand extends AbstractCommand {

	class EligibilityMessage implements ViewEntry {
		private boolean _isOK;
		private final List<Object> _missing = new ArrayList<Object>();
	
		EligibilityMessage(boolean isOK) {
			super();
			_isOK = isOK;
		}
		
		EligibilityMessage(Collection<?> missing) {
			this(false);
			_missing.addAll(missing);
		}
		
		public String getRowClassName() {
			return _isOK ? null : "opt2";
		}
		
		public boolean getAchieved() {
			return _isOK;
		}
		
		public Class<?> getMissingClass() {
			return _missing.isEmpty() ? null : _missing.get(0).getClass();
		}
		
		public Collection<?> getMissing() {
			return _missing;
		}
	}
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

        // Determine who to display
        int id = ctx.isUserInRole("HR") ? ctx.getID() : ctx.getUser().getID();
        if (id == 0)
        	id = ctx.getUser().getID();
        
        try {
        	Connection con = ctx.getConnection();
        	
			// Load the pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(ctx.getID());
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());
			
			// Load all Accomplishment profiles
			GetAccomplishment adao = new GetAccomplishment(con);
			Collection<Accomplishment> accs = adao.getAll();
			
			// Load the Pilot's Flight Reports
			GetFlightReports frdao = new GetFlightReports(con);
			Collection<FlightReport> flights = frdao.getByPilot(p.getID(), null);
			
			// Instantiate the helper and loop through
			Map<Accomplishment, EligibilityMessage> accData = new TreeMap<Accomplishment, EligibilityMessage>();
			AccomplishmentHistoryHelper helper = new AccomplishmentHistoryHelper(p, flights);
			for (Accomplishment a : accs) {
				Date dt = helper.achieved(a);
				if (dt == null) {
					Collection<?> missing = helper.missing(a);
					accData.put(a, new EligibilityMessage(missing));
				} else
					accData.put(new DatedAccomplishment(dt, a), new EligibilityMessage(true));
			}
        	
			// Save status variables
			ctx.setAttribute("accs", accData, REQUEST);
			ctx.setAttribute("pilot", p, REQUEST);
        } catch (DAOException de) {
        	throw new CommandException(de);
        } finally {
        	ctx.release();
        }
        
        // Forward to the JSP
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/stats/accomplishmentEligibility.jsp");
        result.setSuccess(true);
	}
}