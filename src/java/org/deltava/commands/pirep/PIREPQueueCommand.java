// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Site Command to display Flight Reports awaiting disposition.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class PIREPQueueCommand extends AbstractViewCommand {
	
	private static final Collection<Integer> PENDING = Arrays.asList(Integer.valueOf(FlightReport.SUBMITTED), Integer.valueOf(FlightReport.HOLD));

	private static final String MY_EQ_SORT = "IF(IFNULL(LOCATE(?,GROUP_CONCAT(ER.EQTYPE)),0)=0,1,0), PR.DATE, PR.SUBMITTED, PR.ID";
	private static final String[] SORT_CODES = {"PR.DATE, PR.SUBMITTED, PR.ID", "P.LASTNAME, P.FIRSTNAME, PR.SUBMITTED", "PR.EQTYPE, PR.DATE, PR.SUBMITTED", "$MYEQ"};
	private static final String[] SORT_NAMES = {"Submission Date", "Pilot Name", "Equipment Type", "My Program"};
	private static final List<ComboAlias> SORT_OPTS = ComboUtils.fromArray(SORT_NAMES, SORT_CODES);

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error (typically database) occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Build dynamic sort option
		String mySort = MY_EQ_SORT.replace("?", "\'" + ctx.getUser().getEquipmentType() + "\'");
		
        // Get/set start/count parameters
        ViewContext<FlightReport> vc = initView(ctx, FlightReport.class);
        if (StringUtils.arrayIndexOf(SORT_CODES, vc.getSortType()) == -1)
        	vc.setSortType(SORT_CODES[0]);
        
        boolean isMyEQSort = (StringUtils.arrayIndexOf(SORT_CODES, vc.getSortType()) == 3); 
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO
			GetFlightReports dao = new GetFlightReports(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			
			// Get the PIREPs and load the promotion type
			vc.setResults(dao.getByStatus(PENDING, isMyEQSort ? mySort : vc.getSortType()));
			dao.getCaptEQType(vc.getResults());
			
			// Load the Pilots
			Collection<Integer> IDs = vc.getResults().stream().map(FlightReport::getAuthorID).collect(Collectors.toSet());
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
			
			// Load my equipment type
			GetEquipmentType eqdao = new GetEquipmentType(con);
			EquipmentType myEQ = eqdao.get(ctx.getUser().getEquipmentType());
			ctx.setAttribute("myEQ", myEQ, REQUEST);
			
			// Check if we display the scroll bar
			ctx.setAttribute("doScroll", Boolean.valueOf(vc.getResults().size() >= vc.getCount()), REQUEST);
			
			// Split into my held PIREPs and my equipment PIREPs
			Collection<FlightReport> myEQType = new ArrayList<FlightReport>();
			Collection<FlightReport> myHeld = new ArrayList<FlightReport>();
			for (Iterator<FlightReport> i = vc.getResults().iterator(); i.hasNext(); ) {
				FlightReport fr = i.next();
				if ((fr.getStatus() == FlightReport.HOLD) && (fr.getDatabaseID(DatabaseID.DISPOSAL) == ctx.getUser().getID())) {
					myHeld.add(fr);
					i.remove();
				} else if (myEQ.getPrimaryRatings().contains(fr.getEquipmentType())) {
					myEQType.add(fr);
					i.remove();
				}
			}
			
			// Save in request
			ctx.setAttribute("myHeld", myHeld, REQUEST);
			ctx.setAttribute("myEQType", myEQType, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save sort options
		ctx.setAttribute("sortTypes", SORT_OPTS, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/pirepQueue.jsp");
		result.setSuccess(true);
	}
}