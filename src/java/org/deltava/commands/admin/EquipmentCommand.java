// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.EquipmentAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to edit Equipment Type profiles. 
 * @author Luke
 * @version 4.1
 * @since 1.0
 */

public class EquipmentCommand extends AbstractFormCommand {

	/**
	 * Callback method called when editing the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
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
			ctx.setAttribute("chiefPilots", pdao.getPilotsByRank(Rank.CP), REQUEST);
			
			// Get the DAO and execute
			GetEquipmentType eqdao = new GetEquipmentType(con);
			EquipmentType eq = (eqType == null) ? null : eqdao.get(eqType, SystemData.get("airline.db"));
			
			// Get the aircraft types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("allEQ", acdao.getAircraftTypes(), REQUEST);
			
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
	@Override
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
			EquipmentType eq = isNew ? new EquipmentType(ctx.getParameter("eqType")) : rdao.get(eqType, SystemData.get("airline.db"));
			if (isNew)
				eq.setOwner(SystemData.getApp(SystemData.get("airline.code")));
			
			// Update the equipment type profile from the request
			eq.setCPID(StringUtils.parse(ctx.getParameter("cp"), 0));
			eq.setStage(StringUtils.parse(ctx.getParameter("stage"), 1));
			eq.setActive(Boolean.valueOf(ctx.getParameter("active")).booleanValue());
			eq.setPromotionLegs(StringUtils.parse(ctx.getParameter("captLegs"), 10));
			eq.setPromotionMinLength(StringUtils.parse(ctx.getParameter("captDistance"), 0));
			eq.setPromotionSwitchLength(StringUtils.parse(ctx.getParameter("switchDistance"), 0));
			eq.setMaximumAccelTime(StringUtils.parse(ctx.getParameter("maxAccel"), 0));
			eq.setMinimum1XTime(StringUtils.parse(ctx.getParameter("min1X"), 0));
			eq.setACARSPromotionLegs(Boolean.valueOf(ctx.getParameter("acarsPromote")).booleanValue());
			eq.setRanks(ctx.getParameters("ranks"));
			eq.setRatings(ctx.getParameters("pRatings"), ctx.getParameters("sRatings"));
			
			// Update airlines
			Collection<String> aCodes = ctx.getParameters("airline");
			if (aCodes != null) {
				Collection<AirlineInformation> airlines = new HashSet<AirlineInformation>();
				for (Iterator<String> i = aCodes.iterator(); i.hasNext(); ) {
					AirlineInformation ai = SystemData.getApp(i.next());
					if (ai != null)
						airlines.add(ai);
				}
				
				eq.setAirlines(airlines);
			}
			
			// Update examination names
			eq.setExamNames(Rank.FO, ctx.getParameters("examFO"));
			eq.setExamNames(Rank.C, ctx.getParameters("examC"));
			
			// Determine who is missing the ratings
			GetPilot pdao = new GetPilot(con);
			GetExamQualifications exdao = new GetExamQualifications(con);
			Collection<Integer> pilotIDs = rdao.getPilotsWithMissingRatings(eq);
			pilotIDs.addAll(exdao.getRatedPilots(eq));
			Collection<Pilot> pilots = pdao.getByID(pilotIDs, "PILOTS").values();
			
			// Start transaction
			ctx.startTX();
			
			// Get the DAO and write the equipment type to the database
			SetEquipmentType wdao = new SetEquipmentType(con);
			if (isNew) {
				wdao.create(eq);
				ctx.setAttribute("isCreated", Boolean.TRUE, REQUEST);
			} else
				wdao.update(eq, ctx.getParameter("eqType"));

			// Update pilot ratings
			boolean updatePilots = Boolean.valueOf(ctx.getParameter("updateRatings")).booleanValue();
			if (updatePilots && (!pilots.isEmpty())) {
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
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/admin/eqUpdate.jsp");
		result.setSuccess(true);
	}
	
	/**
	 * Callback method called when reading the profile. <i>Opens in edit mode</i>.
	 * @param ctx the Command context
	 * @see EquipmentCommand#execEdit(CommandContext)
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}