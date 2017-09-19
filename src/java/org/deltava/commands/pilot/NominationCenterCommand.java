// Copyright 2010, 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.hr.*;
import org.deltava.beans.hr.Nomination.Status;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.NominationAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display Senior Captain nominations.
 * @author Luke
 * @version 8.0
 * @since 3.3
 */

public class NominationCenterCommand extends AbstractCommand {

	/**
	 * Executes the Command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Check if we can nominate someone
		NominationAccessControl ac = new NominationAccessControl(ctx, null);
		ac.validate();
		if (!ac.getCanNominate())
			throw securityException("Cannot create Nomination");
		
		Quarter qNow = new Quarter();
		try {
			Connection con = ctx.getConnection();
			Collection<Nomination> allNoms = new LinkedHashSet<Nomination>();
			
			// Check our nomination count
			GetNominations ndao = new GetNominations(con);
			allNoms.addAll(ndao.getByAuthor(ctx.getUser().getID()));
			ctx.setAttribute("myNoms", new ArrayList<Nomination>(allNoms) , REQUEST);
			
			// If we're in HR, load all pending nominations; if CP then show in my program
			if (ctx.isUserInRole("HR")) {
				Map<Status, Collection<Nomination>> noms = new LinkedHashMap<Status, Collection<Nomination>>();
				Collection<Nomination> allPending = ndao.getByStatus(Status.PENDING, null);
				Collection<Nomination> cqPending = ndao.getByStatus(Status.PENDING, qNow);
				noms.put(Status.PENDING, allPending);
				noms.put(Status.APPROVED, ndao.getByStatus(Status.APPROVED, qNow));
				noms.put(Status.REJECTED, ndao.getByStatus(Status.REJECTED, qNow));
				noms.values().forEach(allNoms::addAll);
				ctx.setAttribute("allNoms", noms, REQUEST);
				ctx.setAttribute("prevQuarterPending", Boolean.valueOf(allPending.size() > cqPending.size()), REQUEST);
			} else if (ctx.getUser().getRank().isCP()) {
				Collection<Nomination> myEQNoms = ndao.getByEQType(ctx.getUser().getEquipmentType());
				allNoms.addAll(myEQNoms);
				ctx.setAttribute("myEQNoms", myEQNoms, REQUEST);
			}
			
			// Fetch Pilot and Author IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			Map<Nomination, NominationAccessControl> acMap = new HashMap<Nomination, NominationAccessControl>();
			for (Nomination n : allNoms) {
				IDs.add(Integer.valueOf(n.getID()));
				for (NominationComment nc : n.getComments())
					IDs.add(Integer.valueOf(nc.getAuthorID()));
				
				// Calculate access
				NominationAccessControl access = new NominationAccessControl(ctx, n);
				access.validate();
				acMap.put(n, access);
			}
			
			// Load the pilot IDs
			GetPilot pdao = new GetPilot(con);
			GetFlightReports frdao = new GetFlightReports(con);
			Map<Integer, Pilot> pilots = pdao.getByID(IDs, "PILOTS");
			frdao.getOnlineTotals(pilots, SystemData.get("airline.db"));
			
			// Save status
			ctx.setAttribute("pilots", pilots, REQUEST);
			ctx.setAttribute("acMap", acMap, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save status variables
		ctx.setAttribute("qtr", qNow, REQUEST);
		ctx.setAttribute("access", ac, REQUEST);
		ctx.setAttribute("canSeeScore", Boolean.valueOf(ctx.getUser().getRank().isCP() || ctx.isUserInRole("HR")), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/hr/scNominateCenter.jsp");
		result.setSuccess(true);
	}
}