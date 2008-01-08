// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;
import org.deltava.beans.stats.ProgramMetrics;
import org.deltava.beans.system.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display program-specific statistics and data.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class ProgramRosterCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Init the view context and equipment type
		ViewContext vc = initView(ctx);
		String eqType = ctx.getParameter("eqType");
		if (eqType == null)
			eqType = ctx.getUser().getEquipmentType();
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the program members
			GetPilot pdao = new GetPilot(con);
			pdao.setQueryStart(vc.getStart());
			pdao.setQueryMax(vc.getCount());
			Map<Integer, Pilot> pilots = CollectionUtils.createMap(pdao.getPilotsByEQ(eqType, true), "ID");
			vc.setResults(pilots.values());
			
			// Load Online/ACARS totals
			GetFlightReports frdao = new GetFlightReports(con);
			frdao.getOnlineTotals(pilots, SystemData.get("airline.db"));
			
			// Load the Equipment program
			GetEquipmentType eqdao = new GetEquipmentType(con);
			EquipmentType eq = eqdao.get(eqType);
			Collection<String> examNames = eq.getExamNames();
			if (ctx.isUserInRole("HR"))
				ctx.setAttribute("eqTypes", eqdao.getActive(), REQUEST);
			
			// Load the program statistics
			Collection<Integer> IDs = new HashSet<Integer>();
			GetProgramStatistics stdao = new GetProgramStatistics(con);
			ProgramMetrics pm = stdao.getMetrics(eq);
			
			// Load pending transfer
			GetTransferRequest txdao = new GetTransferRequest(con);
			Collection<TransferRequest> txs = txdao.getByEQ(eq.getName(), "CREATED");
			for (Iterator<TransferRequest> i = txs.iterator(); i.hasNext(); ) {
				TransferRequest tx = i.next();
				IDs.add(new Integer(tx.getID()));
			}
			
			// Load pending checkrides
			GetExam exdao = new GetExam(con);
			Collection<CheckRide> rides = exdao.getCheckRideQueue(false);
			for (Iterator<CheckRide> i = rides.iterator(); i.hasNext(); ) {
				CheckRide cr = i.next();
				if (!cr.getEquipmentType().equals(eq.getName()))
					i.remove();
				else
					IDs.add(new Integer(cr.getPilotID()));
			}
			
			// Load pending exams
			Collection<Examination> exams = exdao.getSubmitted();
			for (Iterator<Examination> i = exams.iterator(); i.hasNext(); ) {
				Examination ex = i.next();
				if (!examNames.contains(ex.getName()))
						i.remove();
				else
					IDs.add(new Integer(ex.getPilotID()));
			}
			
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
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/roster/programRoster.jsp");
		result.setSuccess(true);
	}
}