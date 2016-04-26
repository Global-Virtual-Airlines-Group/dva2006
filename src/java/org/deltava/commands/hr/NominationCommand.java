// Copyright 2010, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.hr;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.hr.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.NominationAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle Senior Captain nominations.
 * @author Luke
 * @version 7.0
 * @since 3.3
 */

public class NominationCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the nomination.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		
		// Check if enabled
		if (!SystemData.getBoolean("users.sc.active"))
			throw securityException("Nominations not active");
		
		try {
			Connection con = ctx.getConnection();

			// Load the nomination - If no nomination, create one
			GetNominations ndao = new GetNominations(con);
			Nomination n = ndao.get(ctx.getID());
			if ((n != null) && (n.getStatus() != Nomination.Status.PENDING))
				n = null;
			
			boolean isNew = (n == null);
			if (isNew) {
				n = new Nomination(ctx.getID());
				n.setQuarter(new Quarter());
			}
			
			// Load the pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(n.getID());
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + n.getID());
			
			// Check our access
			NominationAccessControl access = new NominationAccessControl(ctx, n);
			access.setPilot(p);
			access.validate();
			if (!access.getCanNominate())
				throw securityException("Cannot nominate " + p.getName());
			
			// Count our nominations
			if (isNew && !access.getCanNominateUnlimited()) {
				int maxNoms = SystemData.getInt("users.sc.maxNominations", 5);
				Collection<Nomination> myNoms = ndao.getByAuthor(ctx.getUser().getID());
				if (myNoms.size() >= maxNoms)
					throw securityException("Already nominated " + myNoms.size() + " pilots, max=" + maxNoms);
			}

			ctx.setAttribute("pilot", p, REQUEST);
			
			// Check for empty body
			String body = ctx.getParameter("body");
			if (StringUtils.isEmpty(body))
				throw new IllegalArgumentException("Body cannot be empty");
			
			// Add comment
			NominationComment nc = new NominationComment(ctx.getUser().getID(), body);
			nc.setAuthorID(ctx.getUser().getID());
			nc.setCreatedOn(Instant.now());
			if (!isNew && access.getCanObject())
				nc.setSupport(Boolean.valueOf(ctx.getParameter("support")).booleanValue());
			n.addComment(nc);
			
			// Load the authors IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			for (NominationComment ncm : n.getComments())
				IDs.add(new Integer(ncm.getAuthorID()));
			
			// Score the nomination
			Collection<Pilot> authors = pdao.getByID(IDs, "PILOTS").values();
			NominationScoreHelper helper = new NominationScoreHelper(n);
			helper.addAuthors(authors);
			n.setScore(helper.getScore());
			
			// Start a transaction
			ctx.startTX();
			
			// Save the nomination
			SetNomination nwdao = new SetNomination(con);
			if (isNew)
				nwdao.create(n);
			else
				nwdao.update(n);
			
			// Save the comment and commit
			nwdao.writeComment(n, nc);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status variable
		ctx.setAttribute("isSaved", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/hr/scNominateUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the nomination.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Load the Nomination
			GetNominations ndao = new GetNominations(con);
			Nomination n = ndao.get(ctx.getID());
			if (n == null)
				throw notFoundException("Unknown Nomination - " + ctx.getID());
			
			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(n.getID());
			ctx.setAttribute("pilot", p, REQUEST);
			
			// Check our access
			NominationAccessControl access = new NominationAccessControl(ctx, n);
			access.setPilot(p);
			access.validate();
			if (!access.getCanRead())
				throw securityException("Cannot update Nomination");
			
			// Load the equipment program
			GetEquipmentType eqdao = new GetEquipmentType(con);
			ctx.setAttribute("eqType", eqdao.get(p.getEquipmentType(), SystemData.get("airline.db")), REQUEST);
			
			// Save in request
			ctx.setAttribute("nom", n, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
			
			// Load nominee's status history
			GetStatusUpdate sudao = new GetStatusUpdate(con);
			Collection<StatusUpdate> updates = sudao.getByUser(n.getID(), SystemData.get("airline.db"));
			ctx.setAttribute("statusUpdates", updates, REQUEST);
			
			// Load nominee's exam history
			GetExam exdao = new GetExam(con);
			ctx.setAttribute("exams", exdao.getExams(n.getID()), REQUEST);
			
			// Get Academy Certifications
			GetAcademyCourses fadao = new GetAcademyCourses(con);
			ctx.setAttribute("courses", fadao.getByPilot(n.getID()), REQUEST);
			
			// Get pilot IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			IDs.add(new Integer(n.getID()));
			for (NominationComment nc : n.getComments())
				IDs.add(new Integer(nc.getAuthorID()));
			for (StatusUpdate upd : updates)
				IDs.add(new Integer(upd.getAuthorID()));
			
			// Load Pilots
			GetFlightReports frdao = new GetFlightReports(con);
			Map<Integer, Pilot> pilots = pdao.getByID(IDs, "PILOTS");
			frdao.getOnlineTotals(pilots, SystemData.get("airline.db"));
			ctx.setAttribute("authors", pilots, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/hr/scNominateRead.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the nomination.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		execRead(ctx);
	}
}