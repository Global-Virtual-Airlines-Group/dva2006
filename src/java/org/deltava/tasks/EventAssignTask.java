// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.assign.*;
import org.deltava.beans.event.*;
import org.deltava.beans.system.*;
import org.deltava.beans.schedule.Airline;

import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.taskman.DatabaseTask;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to automatically assign flghts to Online Event participants
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EventAssignTask extends DatabaseTask {

	/**
	 * Initializes the Scheduled Task.
	 */
	public EventAssignTask() {
		super("Event Assignment", EventAssignTask.class);
	}
	
	private boolean hasFlightReports(Connection c, int eventID, UserDataMap udmap) throws DAOException {
		
		// Load the flight reports
		GetFlightReports frdao = new GetFlightReports(c);
		Collection<FlightReport> pireps = new ArrayList<FlightReport>();
		for (Iterator<String> i = udmap.getTableNames().iterator(); i.hasNext(); ) {
			String tableName = i.next();
			pireps.addAll(frdao.getByEvent(eventID, tableName));
		}
		
		return !pireps.isEmpty();
	}

	/**
	 * Executes the Task.
	 */
	protected void execute() {
		
		// Create the message context
		MessageContext mctxt = new MessageContext();
		try {
			Connection con = getConnection();
			
			// Determining who we are operating as
			GetPilot pdao = new GetPilot(con);
			Pilot from = pdao.getByName(SystemData.get("online.events_assigned_by"), SystemData.get("airline.db"));
			mctxt.addData("user", from);
			
			// Get the DAOs
			GetEvent dao = new GetEvent(con);
			GetUserData usrdao = new GetUserData(con);
			
			// Initialize the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("EVENTASSIGN"));
			
			// Get the events to check
			Collection<Event> events = dao.getAssignableEvents();
			for (Iterator<Event> i = events.iterator(); i.hasNext(); ) {
				Event e = i.next();
				UserDataMap usrmap = usrdao.getByEvent(e.getID());
				
				// Determine if we need to asign flights
				if ((!e.getSignups().isEmpty()) && (!hasFlightReports(con, e.getID(), usrmap))) {
					mctxt.addData("event", e);
					
					// Get the write DAOs
					SetFlightReport fwdao = new SetFlightReport(con);
					SetAssignment awdao = new SetAssignment(con);
					
					// Start the transaction
					startTX();
					
					// Get the signups for this event
					log.info("Assigning flights for Event " + e.getName());
					for (Iterator<Signup> si = e.getSignups().iterator(); si.hasNext(); ) {
						Signup s = si.next();

						// Get the Pilot
						UserData usrData = (UserData) usrmap.get(s.getPilotID());
						Pilot usr = pdao.get(usrData);
						mctxt.addData("pilot", usr);
						
						// Log assignment creation
						log.warn("Assigning Event flight for " + usr.getName());

						// Create a Flight Assignment
						AssignmentInfo ai = new AssignmentInfo(s.getEquipmentType());
						ai.setAssignDate(new Date());
						ai.setPilotID(s.getPilotID());
						ai.setEventID(e.getID());
						ai.setStatus(AssignmentInfo.RESERVED);
						
						// Get the airline
						Airline a = SystemData.getAirline(usrData.getAirlineCode());
						if (a == null)
							a = SystemData.getAirline(SystemData.get("airline.code"));
						
						// Create an Assignment Leg
						AssignmentLeg leg = new AssignmentLeg(a, usr.getPilotNumber(), 1);
						leg.setEquipmentType(s.getEquipmentType());
						leg.setAirportD(s.getAirportD());
						leg.setAirportA(s.getAirportA());
						ai.addAssignment(leg);
						
						// Write the Flight Assignment
						awdao.write(ai, usrData.getDB());

						// Create a Flight Report
						FlightReport fr = new FlightReport(leg);
						fr.setRank(usr.getRank());
						fr.setDatabaseID(FlightReport.DBID_PILOT, s.getPilotID());
						fr.setDatabaseID(FlightReport.DBID_ASSIGN, ai.getID());
						fr.setDatabaseID(FlightReport.DBID_EVENT, e.getID());
						fr.setDate(e.getStartTime());
						switch (e.getNetwork()) {
							case Event.NET_VATSIM :
								fr.setAttribute(FlightReport.ATTR_VATSIM, true);
								break;
								
							case Event.NET_IVAO :
								fr.setAttribute(FlightReport.ATTR_IVAO, true);
								break;
						}
						
						// Write the Flight Report to the proper database
						fwdao.write(fr, usrData.getDB());
						mctxt.addData("pirep", fr);
						
						// Log flight
						log.info("Assigned Flight for " + usr.getName());
						
						// Send the message
						Mailer mailer = new Mailer(from);
						mailer.setContext(mctxt);
						mailer.send(usr);
					}
					
					// Commit the transaction
					commitTX();
				}
			}
		} catch (DAOException de) {
			rollbackTX();
			log.error(de.getMessage(), de);
		} finally {
			release();
		}

		// Log completion
		log.info("Processing Complete");
	}
}