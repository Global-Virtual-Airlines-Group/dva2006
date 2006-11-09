// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.EquipmentAccessControl;

import org.deltava.util.*;

/**
 * A Web Site Command to edit Equipment Type profiles. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EquipmentCommand extends AbstractFormCommand {

	/**
	 * Callback method called when editing the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {

		// Get the equipment type
		String eqType = (String) ctx.getCmdParameter(Command.ID, null);
		
		// Check our access
		EquipmentAccessControl access = new EquipmentAccessControl(ctx);
		access.validate();
		if (!access.getCanEdit())
			throw securityException("Cannot modify Equipment Profile");
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the Chief Pilots
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("chiefPilots", pdao.getPilotsByRank(Ranks.RANK_CP), REQUEST);
			
			// Get the DAO and execute
			GetEquipmentType eqdao = new GetEquipmentType(con);
			EquipmentType eq = (eqType == null) ? null : eqdao.get(eqType);
			if (eq != null)
			   ctx.setAttribute("captLegs", new Integer(eq.getPromotionLegs(Ranks.RANK_C)), REQUEST);
			
			// Get the Examination names
			GetExamProfiles exdao = new GetExamProfiles(con);
			ctx.setAttribute("exams", exdao.getExamProfiles(false), REQUEST);
			
			// Save the equipment profile and access controller
			ctx.setAttribute("eqType", eq, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/eqProfile.jsp");
		result.setSuccess(true);
	}
	
	/**
	 * Callback method called when saving the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {

		// Get the equipment type
		String eqType = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = (eqType == null);

		// Check our access
		EquipmentAccessControl access = new EquipmentAccessControl(ctx);
		access.validate();
		if (!access.getCanEdit())
			throw securityException("Cannot modify Equipment Profile");

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the existing equipment type profile
			GetEquipmentType rdao = new GetEquipmentType(con);
			EquipmentType eq = isNew ? new EquipmentType(ctx.getParameter("eqType")) : rdao.get(eqType);
			
			// Update the equipment type profile from the request
			eq.setCPID(Integer.parseInt(ctx.getParameter("cp")));
			eq.setStage(Integer.parseInt(ctx.getParameter("stage")));
			eq.setActive(Boolean.valueOf(ctx.getParameter("active")).booleanValue());
			eq.setACARSPromotionLegs(Boolean.valueOf(ctx.getParameter("acarsPromote")).booleanValue());
			eq.setRanks(ctx.getParameters("ranks"));
			eq.setRatings(ctx.getParameters("pRatings"), ctx.getParameters("sRatings"));

			// Update examination names
			eq.setExamName(Ranks.RANK_FO, ctx.getParameter("examFO"));
			eq.setExamName(Ranks.RANK_C, ctx.getParameter("examC"));
			
			// Determine who is missing the ratings
			GetPilot pdao = new GetPilot(con);
			Collection<Integer> pilotIDs = rdao.getPilotsWithMissingRatings(eq);
			Collection<Pilot> pilots = pdao.getByID(pilotIDs, "PILOTS").values();
			
			// Start transaction
			ctx.startTX();
			
			// Get the DAO and write the equipment type to the database
			SetEquipmentType wdao = new SetEquipmentType(con);
			if (isNew) {
				wdao.create(eq);
				ctx.setAttribute("isCreated", Boolean.TRUE, REQUEST);
			} else {
				wdao.update(eq);
			}
			
			// Update pilot ratings
			if (!pilots.isEmpty()) {
				final Collection<String> newRatings = eq.getRatings();
				Map<Pilot, Collection<String>> updatedRatings = new LinkedHashMap<Pilot, Collection<String>>();
				Collection<StatusUpdate> updates = new ArrayList<StatusUpdate>();
				
				// Add ratings to each pilot
				SetPilot pwdao = new SetPilot(con);
				for (Iterator<Pilot> i = pilots.iterator(); i.hasNext(); ) {
					Pilot p = i.next();
					Collection<String> addedRatings = CollectionUtils.getDelta(newRatings, p.getRatings());
					if (!addedRatings.isEmpty()) {
						StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.RATING_ADD);
						upd.setAuthorID(ctx.getUser().getID());
						upd.setDescription("Added " + StringUtils.listConcat(addedRatings, ", ") + " after Equipment Program update");
						updates.add(upd);
					}
					
					// Save the pilot
					p.addRatings(addedRatings);
					updatedRatings.put(p, addedRatings);
					pwdao.addRatings(p, addedRatings);
				}
				
				// Write the status updates
				SetStatusUpdate sudao = new SetStatusUpdate(con);
				sudao.write(updates);
				
				// Save the updated pilots list
				ctx.setAttribute("updatedPilots", updatedRatings.keySet(), REQUEST);
				ctx.setAttribute("updatedRatings", updatedRatings, REQUEST);
			}
			
			// Commit the transaction
			ctx.commitTX();
			
			// Save the equipment program in the request
			ctx.setAttribute("eqType", eq, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Redirect to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/admin/eqUpdate.jsp");
		result.setSuccess(true);
	}
	
	/**
	 * Callback method called when reading the profile. <i>Opens in edit mode</i>.
	 * @param ctx the Command context
	 * @see EquipmentCommand#execEdit(CommandContext)
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}