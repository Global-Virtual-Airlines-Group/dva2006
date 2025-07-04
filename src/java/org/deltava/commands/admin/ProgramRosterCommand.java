// Copyright 2008, 2009, 2011, 2012, 2015, 2016, 2017, 2021, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;
import org.deltava.beans.hr.TransferRequest;
import org.deltava.beans.stats.ProgramMetrics;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.*;

/**
 * A Web Site Command to display program-specific statistics and data.
 * @author Luke
 * @version 10.2
 * @since 2.1
 */

public class ProgramRosterCommand extends AbstractViewCommand {
	
	private static final String[] SORT_CODE = {"P.LASTNAME, P.FIRSTNAME", "P.CREATED", "P.LOGINS", "P.LAST_LOGIN", "P.RANKING", "LEGS", "LASTFLIGHT"};
	private static final List<?> SORT_OPTIONS = ComboUtils.fromArray(new String[] {"Pilot Name", "Hire Date", "Logins", "Last Login", "Rank", "Flight Legs", "Last Flight"}, SORT_CODE);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Init the view context
		ViewContext<Pilot> vc = initView(ctx, Pilot.class);
		if (StringUtils.arrayIndexOf(SORT_CODE, vc.getSortType()) == -1)
			   vc.setSortType(SORT_CODE[0]);
		
		// Check if doing a descending sort
		boolean isDesc = Boolean.parseBoolean(ctx.getParameter("isDesc"));
		if (isDesc)
			vc.setSortType(vc.getSortType() + " DESC");
		
		// Get the equipment type and rank
		String eqType = ctx.getParameter("eqType");
		if (eqType == null)
			eqType = ctx.getUser().getEquipmentType();
		Rank rank = Rank.fromName(ctx.getParameter("rank"));
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the eq program
			GetEquipmentType eqdao = new GetEquipmentType(con);
			EquipmentType eq = eqdao.get(eqType, ctx.getDB());
			Collection<String> examNames = eq.getExamNames();
			if (ctx.isUserInRole("HR"))
				ctx.setAttribute("eqTypes", eqdao.getActive(), REQUEST);
			
			// Get the program members
			GetPilot pdao = new GetPilot(con);
			pdao.setQueryStart(vc.getStart());
			pdao.setQueryMax(vc.getCount());
			vc.setResults(pdao.getPilotsByEQ(eq, vc.getSortType(), true, rank));
			
			// Load promotion queue
			GetPilotRecognition pqdao = new GetPilotRecognition(con);
			Collection<Integer> promoIDs = pqdao.getPromotionQueue(eqType);
			Map<Integer, Pilot> promoPilots = pdao.getByID(promoIDs, "PILOTS");
			
			// Calculate promotion queue and filter by rank
			boolean canPromote = false;
			Map<Integer, PilotAccessControl> accessMap = new HashMap<Integer, PilotAccessControl>();
			for (Pilot p : promoPilots.values()) {
				PilotAccessControl ac = new PilotAccessControl(ctx, p);
				ac.validate();
				accessMap.put(Integer.valueOf(p.getID()), ac);
				canPromote |= ac.getCanPromote();
			}
			
			// Load Online/ACARS totals
			GetFlightReports frdao = new GetFlightReports(con);
			frdao.getOnlineTotals(CollectionUtils.createMap(vc.getResults(), Pilot::getID), ctx.getDB());
			
			// Save promotion queue and access
			if (canPromote) {
				frdao.getOnlineTotals(promoPilots, ctx.getDB());
				ctx.setAttribute("promoQueue", promoPilots.values(), REQUEST);
				ctx.setAttribute("promoAccess", accessMap, REQUEST);
			}
			
			// Load the program statistics
			GetProgramStatistics stdao = new GetProgramStatistics(con);
			ProgramMetrics pm = stdao.getMetrics(eq);
			
			// Load pending transfers
			GetTransferRequest txdao = new GetTransferRequest(con);
			Collection<TransferRequest> txs = txdao.getByEQ(eq.getName(), "CREATED");
			Collection<Integer> IDs = txs.stream().map(TransferRequest::getID).collect(Collectors.toSet());
			
			// Load pending checkrides
			GetExam exdao = new GetExam(con);
			Collection<CheckRide> rides = exdao.getCheckRideQueue(false);
			for (Iterator<CheckRide> i = rides.iterator(); i.hasNext(); ) {
				CheckRide cr = i.next();
				if (!cr.getEquipmentType().equals(eq.getName()))
					i.remove();
				else
					IDs.add(Integer.valueOf(cr.getAuthorID()));
			}
			
			// Load pending exams
			Collection<Examination> exams = exdao.getSubmitted();
			for (Iterator<Examination> i = exams.iterator(); i.hasNext(); ) {
				Examination ex = i.next();
				if (!examNames.contains(ex.getName()))
					i.remove();
				else
					IDs.add(Integer.valueOf(ex.getAuthorID()));
			}
			
			// Load checkride statistics
			GetExamStatistics exsdao = new GetExamStatistics(con);
			exsdao.setQueryMax(15);
			ctx.setAttribute("crStats", exsdao.getCheckrideStatistics("DATE_FORMAT(C.CREATED, '%M %Y')", "CONCAT_WS(' ', P.FIRSTNAME, P.LASTNAME)", false, 0, eq.getName()), REQUEST);
			
			// Load flight report statistics for the past 14 days 
			GetFlightReportStatistics psdao = new GetFlightReportStatistics(con);
			psdao.setDayFilter(14);
			ctx.setAttribute("flightStatsInterval", Integer.valueOf(14), REQUEST);
			ctx.setAttribute("pirepStats", psdao.getEQPIREPStatistics(eqType, "F.EQTYPE", "SL DESC, SH DESC"), REQUEST);
			
			// Load the Pilot names
			GetUserData uddao = new GetUserData(con);
			UserDataMap udm = uddao.get(IDs);
			ctx.setAttribute("userData", udm, REQUEST);
			ctx.setAttribute("pilots", pdao.get(udm), REQUEST);
			
			// Save in the request
			ctx.setAttribute("eqType", eq, REQUEST);
			ctx.setAttribute("metrics", pm, REQUEST);
			ctx.setAttribute("examQueue", exams, REQUEST);
			ctx.setAttribute("crQueue", rides, REQUEST);
			ctx.setAttribute("txQueue", txs, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save sort options
		ctx.setAttribute("sortTypes", SORT_OPTIONS, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/roster/programRoster.jsp");
		result.setSuccess(true);
	}
}