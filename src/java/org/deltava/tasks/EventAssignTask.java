// Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.assign.*;
import org.deltava.beans.event.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.Airline;

import org.deltava.dao.*;
import org.deltava.mail.*;
import org.deltava.taskman.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to automatically assign flghts to Online Event participants.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class EventAssignTask extends Task {

	/**
	 * Initializes the Scheduled Task.
	 */
	public EventAssignTask() {
		super("Event Assignment", EventAssignTask.class);
	}
	
	/*
	 * Checks whether flight reports hav been assigned for this event.
	 */
	private static boolean hasFlightReports(Connection c, int eventID, UserDataMap udmap) throws DAOException {
		
		GetFlightReports frdao = new GetFlightReports(c);
		for (String tableName : udmap.getTableNames()) {
			if (frdao.getByEvent(eventID, tableName).size() > 0)
				return true;
		}
		
		return false;
	}

	/**
	 * Executes the Task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		
		MessageContext mctxt = new MessageContext();
		try {
			Connection con = ctx.getConnection();
			
			// Determining who we are operating as
			EMailAddress from = Mailer.makeAddress(SystemData.get("airline.mail.events"), SystemData.get("airline.name") + " Events");
			String aCode = SystemData.get("airline.code");
			
			// Get the DAOs
			GetEvent dao = new GetEvent(con);
			GetUserData usrdao = new GetUserData(con);
			
			// Initialize the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("EVENTASSIGN"));
			
			// Get the events to check
			Collection<Event> events = dao.getAssignableEvents();
			for (Event e : events) {
				UserDataMap usrmap = usrdao.getByEvent(e.getID());
				
				// Filter out other airlines
				for (Iterator<UserData> i = usrmap.values().iterator(); i.hasNext(); ) {
					UserData ud = i.next();
					if (!ud.getAirlineCode().equals(aCode))
						i.remove();
				}
				
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
					for (Signup s : e.getSignups()) {
						UserData usrData = usrmap.get(s.getPilotID());
						Pilot usr = pdao.get(usrData);
						mctxt.addData("pilot", usr);
						if (usr == null)
							continue;
						
						// Log assignment creation
						log.info("Assigning Event flight for " + usr.getName());

						// Create a Flight Assignment
						AssignmentInfo ai = new AssignmentInfo(s.getEquipmentType());
						ai.setAssignDate(Instant.now());
						ai.setPilotID(s.getPilotID());
						ai.setEventID(e.getID());
						ai.setStatus(AssignmentInfo.RESERVED);
						
						// Get the airline
						Airline a = SystemData.getAirline(usrData.getAirlineCode());
						if (a == null)
							a = SystemData.getAirline(aCode);
						
						// Calculate the flight number
						int flightID = usr.getPilotNumber();
						if (flightID == 0) {
							Calendar cld = Calendar.getInstance();
							flightID = cld.get(Calendar.YEAR) + cld.get(Calendar.DAY_OF_YEAR);
						} else if (flightID > 10000)
							flightID %= 10000;
						
						// Create an Assignment Leg
						AssignmentLeg leg = new AssignmentLeg(a, flightID, 1);
						leg.setEquipmentType(s.getEquipmentType());
						leg.setAirportD(s.getAirportD());
						leg.setAirportA(s.getAirportA());
						ai.addAssignment(leg);
						
						// Write the Flight Assignment
						awdao.write(ai, usrData.getDB());

						// Create a Flight Report
						FlightReport fr = new FlightReport(leg);
						fr.setRank(usr.getRank());
						fr.setDatabaseID(DatabaseID.PILOT, s.getPilotID());
						fr.setDatabaseID(DatabaseID.ASSIGN, ai.getID());
						fr.setDatabaseID(DatabaseID.EVENT, e.getID());
						fr.setDate(e.getStartTime());
						fr.setNetwork(e.getNetwork());
						
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
					if (!e.getContactAddrs().isEmpty() && e.getOwner().equals(aCode)) {
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
						for (String addr : e.getContactAddrs())
							addrs.add(Mailer.makeAddress(addr));
						
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

		log.info("Processing Complete");
	}
}