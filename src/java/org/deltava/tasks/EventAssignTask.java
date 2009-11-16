// Copyright 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.assign.*;
import org.deltava.beans.event.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.schedule.Airline;

import org.deltava.dao.*;
import org.deltava.mail.*;
import org.deltava.taskman.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to automatically assign flghts to Online Event participants.
 * @author Luke
 * @version 2.7
 * @since 1.0
 */

public class EventAssignTask extends Task {

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
	protected void execute(TaskContext ctx) {
		
		// Create the message context
		MessageContext mctxt = new MessageContext();
		try {
			Connection con = ctx.getConnection();
			
			// Determining who we are operating as
			EMailAddress from = Mailer.makeAddress(SystemData.get("airline.mail.events"), SystemData.get("airline.name") + " Events");
			
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
					
					// Get the DAOs
					GetPilot pdao = new GetPilot(con);
					SetFlightReport fwdao = new SetFlightReport(con);
					SetAssignment awdao = new SetAssignment(con);
					
					// Start the transaction
					ctx.startTX();
					
					// Get the signups for this event
					log.info("Assigning flights for Event " + e.getName());
					for (Iterator<Signup> si = e.getSignups().iterator(); si.hasNext(); ) {
						Signup s = si.next();

						// Get the Pilot
						UserData usrData = usrmap.get(s.getPilotID());
						Pilot usr = pdao.get(usrData);
						mctxt.addData("pilot", usr);
						
						// Log assignment creation
						log.info("Assigning Event flight for " + usr.getName());

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
						if (e.getNetwork() == OnlineNetwork.VATSIM)
							fr.setAttribute(FlightReport.ATTR_VATSIM, true);
						else if (e.getNetwork() == OnlineNetwork.IVAO)
							fr.setAttribute(FlightReport.ATTR_IVAO, true);
						
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
					ctx.commitTX();
					
					// Send ATC notification
					if (!e.getContactAddrs().isEmpty()) {
						mctxt = new MessageContext();
						
						// Get the template
						mctxt.setTemplate(mtdao.get("EVENTATCNOTIFY"));
						mctxt.addData("event", e);
						mctxt.setSubject("Online Event - " + e.getName());
						
						// Save the start/end/signup dates
						mctxt.addData("startDateTime", StringUtils.format(e.getStartTime(), "MM/dd/yyyy HH:mm"));
						mctxt.addData("endDateTime", StringUtils.format(e.getEndTime(), "MM/dd/yyyy HH:mm"));

						// Get the addresses to send to
						Collection<EMailAddress> addrs = new LinkedHashSet<EMailAddress>();
						for (Iterator<String> ai = e.getContactAddrs().iterator(); ai.hasNext(); )
							addrs.add(Mailer.makeAddress(ai.next()));
						
						// Send the message
						Mailer mailer = new Mailer(from);
						mailer.setContext(mctxt);
						mailer.send(addrs);
					}
				}
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// Log completion
		log.info("Processing Complete");
	}
}