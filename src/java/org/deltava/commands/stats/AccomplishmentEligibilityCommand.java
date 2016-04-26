// Copyright 2010, 2012, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to check eligibility for particular Accomplishments.
 * @author Luke
 * @version 7.0
 * @since 3.2
 */

public class AccomplishmentEligibilityCommand extends AbstractCommand {

	public class EligibilityMessage implements ViewEntry {
		private boolean _isOK;
		private final List<Object> _missing = new ArrayList<Object>();
		private long _progress;
	
		EligibilityMessage(boolean isOK) {
			super();
			_isOK = isOK;
		}
		
		EligibilityMessage(long progress, Collection<?> missing) {
			this(false);
			_missing.addAll(missing);
			_progress = progress;
		}
		
		@Override
		public String getRowClassName() {
			return _isOK ? null : "opt2";
		}
		
		public boolean getAchieved() {
			return _isOK;
		}
		
		public long getProgress() {
			return _progress;
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
        
        ctx.setAttribute("isOurs", Boolean.valueOf(id == ctx.getUser().getID()), REQUEST);
        
        try {
        	Connection con = ctx.getConnection();
        	
			// Load the pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(id);
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + id);
			
			// Load all Accomplishment profiles
			GetAccomplishment adao = new GetAccomplishment(con);
			Collection<Accomplishment> accs = adao.getAll();
			
			// Instantiate the helper
			AccomplishmentHistoryHelper helper = new AccomplishmentHistoryHelper(p);
			
			// Load the Pilot's Flight Reports
			GetFlightReports frdao = new GetFlightReports(con);
			Collection<FlightReport> flights = frdao.getByPilot(p.getID(), null);
			frdao.getCaptEQType(flights);
			flights.forEach(fr -> helper.add(fr));
			
			// Load the Pilot's dispatch connections
			GetACARSLog acdao = new GetACARSLog(con);
			GetDispatchCalendar dcdao = new GetDispatchCalendar(con);
			List<ConnectionEntry> cons = acdao.getConnections(new LogSearchCriteria(p.getID()));
			for (ConnectionEntry ce : cons) {
				DispatchConnectionEntry dce = (DispatchConnectionEntry) ce;
				Collection<FlightInfo> dspFlights = dcdao.getDispatchedFlights(dce);
				dce.addFlights(dspFlights);
				helper.add(ce);
			}
			
			// Filter the accomplishments - we include only achieved or the lowest unachieved accomplishment per unit
			Map<AccomplishUnit, Accomplishment> accFilter = new HashMap<AccomplishUnit, Accomplishment>();
			Map<Accomplishment, EligibilityMessage> accData = new LinkedHashMap<Accomplishment, EligibilityMessage>();
			for (Accomplishment a : accs) {
				Instant dt = helper.achieved(a);
				if (dt == null) {
					Collection<?> missing = helper.missing(a);	
					long progress = helper.getProgress(a);
					accData.put(a, new EligibilityMessage(progress, missing));
					if (!a.getAlwaysDisplay())
						accFilter.put(a.getUnit(), a);
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