// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;
import org.deltava.comparators.AirportComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display a Pilot's Flight Reports.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class LogBookCommand extends AbstractViewCommand {
	
    // List of query columns we can order by
    private static final String[] SORT_CODE = {"DATE DESC, PR.SUBMITTED DESC", "EQTYPE", "DISTANCE DESC", "AIRPORT_D",
    	"AIRPORT_A", "FLIGHT_TIME DESC"};
    private static final String[] SORT_NAMES = {"Flight Date", "Equipment", "Distance", "Origin", "Destination", "Flight Time"};
    private static final List<ComboAlias> SORT_OPTIONS = ComboUtils.fromArray(SORT_NAMES, SORT_CODE);

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {
       
        // Get/set start/count parameters
        ViewContext vc = initView(ctx);
        if (StringUtils.arrayIndexOf(SORT_CODE, vc.getSortType()) == -1)
  		   	vc.setSortType(SORT_CODE[0]);
       
        // Determine who to display
        int id = ctx.getID();
        if ((id == 0) && ctx.isAuthenticated())
        	id = ctx.getUser().getID();
        
        // Determine if we display comments or not
        boolean showComments = "log".equals(ctx.getCmdParameter(Command.OPERATION, "log"));
        ctx.setAttribute("comments", Boolean.valueOf(showComments), REQUEST);
        
        // Initialize the search criteria
        ScheduleSearchCriteria criteria = new ScheduleSearchCriteria(null, 0, 0);
        criteria.addEquipmentType(ctx.getParameter("eqType"));
        criteria.setSortBy(vc.getSortType());
        criteria.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
        criteria.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
        
        // Set sort options
        ctx.setAttribute("sortTypes", SORT_OPTIONS, REQUEST);
        try {
            Connection con = ctx.getConnection();
            
            // Get the pilot profile
            GetPilot dao = new GetPilot(con);
            ctx.setAttribute("pilot", dao.get(id), REQUEST);

            // Get the DAO and set the parameters
            GetFlightReports dao2 = new GetFlightReports(con);
            dao2.setQueryStart(vc.getStart());
            dao2.setQueryMax(vc.getCount());
            
            // Get the PIREP beans and load the promotion eligibility
            Collection<FlightReport> pireps = dao2.getByPilot(id, criteria);
            dao2.getCaptEQType(pireps);
            vc.setResults(pireps);
            
            // Load the Equipment types
            GetAircraft acdao = new GetAircraft(con);
            ctx.setAttribute("eqTypes", acdao.getAircraftTypes(), REQUEST);
            
            // Load the airport options
            Collection<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
            GetAirport adao = new GetAirport(con);
            airports.addAll(adao.getByPilot(id)); 
            ctx.setAttribute("airports", airports, REQUEST);
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
      
        // Set the result page and return
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/pilot/logBook.jsp");
        result.setSuccess(true);
    }
}