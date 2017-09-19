// Copyright 2005, 2006, 2007, 2008, 2010, 2011, 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.*;
import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to merge two pilot profiles.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

public class DuplicatePilotMergeCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the pilot IDs to merge
		Collection<Integer> ids = new HashSet<Integer>();
		Collection<String> mergeIDs = ctx.getParameters("sourceID");
		for (Iterator<String> i = mergeIDs.iterator(); i.hasNext();)
			ids.add(Integer.valueOf(StringUtils.parseHex(i.next())));
		
		// Create the messaging context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());
		
		// Check if we're merging exams and flights
		boolean mergeFlights = Boolean.valueOf(ctx.getParameter("mergeFlights")).booleanValue();
		boolean mergeExams = Boolean.valueOf(ctx.getParameter("mergeExams")).booleanValue();
		boolean mergeCRs = Boolean.valueOf(ctx.getParameter("mergeCRs")).booleanValue();
		boolean mergeFA = Boolean.valueOf(ctx.getParameter("mergeFA")).booleanValue();
		
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the pilots
			GetPilot dao = new GetPilot(con);
			Collection<Pilot> src = dao.getByID(ids, "PILOTS").values();
			Pilot usr = dao.get(ctx.getID());
			if (usr == null)
				throw notFoundException("Invalid User - " + ctx.getID());
			
			// Save the pilot
			mctxt.addData("pilot", usr);

			// Validate our access
			Collection<String> pilotIDs = new HashSet<String>();
			pilotIDs.add(usr.getPilotCode());
			for (Iterator<Pilot> i = src.iterator(); i.hasNext();) {
				Pilot p = i.next();
				PilotAccessControl access = new PilotAccessControl(ctx, p);
				access.validate();
				if (!access.getCanChangeStatus())
					i.remove();
				else if (!StringUtils.isEmpty(p.getPilotCode()))
					pilotIDs.add(p.getPilotCode());
			}

			// Check that we can merge any pilots
			if (src.isEmpty())
				throw securityException("Cannot merge Pilots");
			
			// Ensure that we are merging into the right pilot code
			UserID newID = new UserID(ctx.getParameter("code"));
			if (!pilotIDs.contains(newID.toString()))
				throw securityException("Invalid Pilot Code - " + newID);

			// Start a JDBC transaction
			ctx.startTX();
			
			// Get the roles
			Collection<String> newRoles = new TreeSet<String>(usr.getRoles());
			
			// Get the write DAOs
			SetPilot pwdao = new SetPilot(con);
			SetPilotMerge mgdao = new SetPilotMerge(con);

			// Iterate through the Pilots, combining the roles
			Collection<String> oldNames = new LinkedHashSet<String>();
			Collection<StatusUpdate> sUpdates = new ArrayList<StatusUpdate>();
			for (Iterator<Pilot> i = src.iterator(); i.hasNext();) {
				Pilot p = i.next();
				newRoles.addAll(p.getRoles());

				// Create a status update
				if (p.getID() != usr.getID()) {
					StatusUpdate su = new StatusUpdate(p.getID(), StatusUpdate.STATUS_CHANGE);
					su.setAuthorID(ctx.getUser().getID());
					su.setDescription("Merged into " + usr.getName() + " (" + usr.getPilotCode() + ")");
					sUpdates.add(su);
					
					// Merge flights
					if (mergeFlights) {
						int flightsMerged = mgdao.mergeFlights(p, usr);
						if (flightsMerged > 0) {
							su = new StatusUpdate(usr.getID(), StatusUpdate.COMMENT);
							su.setAuthorID(ctx.getUser().getID());
							su.setDescription("Merged " + flightsMerged + " Flights from " + p.getName() + " (" + p.getPilotCode() + ")");
							sUpdates.add(su);
						}
					}
					
					// Merge exams
					if (mergeExams) {
						int examsMerged = mgdao.mergeExams(p, usr);
						if (examsMerged > 0) {
							su = new StatusUpdate(usr.getID(), StatusUpdate.COMMENT);
							su.setAuthorID(ctx.getUser().getID());
							su.setDescription("Merged " + examsMerged + " Examinations from " + p.getName() + " (" + p.getPilotCode() + ")");
							sUpdates.add(su);
						}
					}
					
					// Merge check rides
					if (mergeCRs) {
						int ridesMerged = mgdao.mergeCheckRides(p, usr);
						if (ridesMerged > 0) {
							su = new StatusUpdate(usr.getID(), StatusUpdate.COMMENT);
							su.setAuthorID(ctx.getUser().getID());
							su.setDescription("Merged " + ridesMerged + " Check Rides from " + p.getName() + " (" + p.getPilotCode() + ")");
							sUpdates.add(su);
						}
					}
					
					// Merge Flight Academy courses
					if (mergeFA) {
						int coursesMerged = mgdao.mergeCourses(p, usr);
						if (coursesMerged > 0) {
							su = new StatusUpdate(usr.getID(), StatusUpdate.COMMENT);
							su.setAuthorID(ctx.getUser().getID());
							su.setDescription("Merged " + coursesMerged + " Flight Academy Courses from " + p.getName() + " (" + p.getPilotCode() + ")");
							sUpdates.add(su);
						}
					}

					// Migrate the data
					mgdao.setStatus(p.getID(), Pilot.RETIRED);
					oldNames.add(p.getName());
				}
				
				// If this pilot has the destination pilot code, swap with usr
				if (p.getPilotNumber() == newID.getUserID()) {
					if (p.getStatus() == Pilot.ACTIVE)
						p.setStatus(Pilot.RETIRED);
					
					if (usr.getPilotNumber() > 0)
						p.setPilotNumber(usr.getPilotNumber());
					pwdao.write(p);
				} else if (p.getStatus() == Pilot.ACTIVE) {
					p.setStatus(Pilot.RETIRED);
					pwdao.write(p);
				}
			}
			
			// Create a status update listing source users
			StatusUpdate su = new StatusUpdate(usr.getID(), StatusUpdate.STATUS_CHANGE);
			su.setAuthorID(ctx.getUser().getID());
			su.setDescription(StringUtils.listConcat(oldNames, ", ") + " merged");
			sUpdates.add(su);
			
			// Update the roles and status
			boolean updatePassword = (usr.getStatus() != Pilot.ACTIVE);
			usr.addRoles(newRoles);
			usr.setStatus(Pilot.ACTIVE);
			usr.setPilotNumber(newID.getUserID());
			
			// Write the pilot profile
			pwdao.write(usr);

			// Write status updates
			SetStatusUpdate sudao = new SetStatusUpdate(con);
			sudao.write(sUpdates);
			
			// If we're not active, generate a new password
			if (updatePassword) {
				String newPwd = PasswordGenerator.generate(10);
				usr.setPassword(newPwd);
			
				// Get the authenticator and update the password
				Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
				if (auth instanceof SQLAuthenticator)
					((SQLAuthenticator) auth).setConnection(con);
				
				if (auth.contains(usr)) {
					auth.updatePassword(usr, newPwd);
					ctx.setAttribute("updatePwd", Boolean.TRUE, REQUEST);
				} else {
					auth.add(usr, newPwd);
					ctx.setAttribute("addUser", Boolean.TRUE, REQUEST);
				}
				
				if (auth instanceof SQLAuthenticator)
					((SQLAuthenticator) auth).clearConnection();
			
				// Get the message template
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctxt.setTemplate(mtdao.get("PWDRESET"));
				
				// Write the inactivity entry
				SetInactivity iwdao = new SetInactivity(con);
				iwdao.setInactivity(usr.getID(), 3, true);
				
				// Send a notification message
				Mailer mailer = new Mailer(ctx.getUser());
				mailer.setContext(mctxt);
				mailer.send(usr);
			}

			// Commit the transaction
			ctx.commitTX();

			// Save the pilots
			ctx.setAttribute("pilot", usr, REQUEST);
			ctx.setAttribute("oldPilots", src, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/roster/dupeMerge.jsp");
		result.setSuccess(true);
	}
}