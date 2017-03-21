// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;

import org.deltava.beans.event.Event;

import org.deltava.dao.*;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display Online Events as an iCal calendar.
 * @author Luke
 * @version 7.3
 * @since 7.3
 */

public class EventCalendarService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		Collection<Event> events = new TreeSet<Event>(); Collection<Integer> myIDs = new HashSet<Integer>();
		try {
			GetEvent dao = new GetEvent(ctx.getConnection());
			if (ctx.isAuthenticated()) {
				myIDs = dao.getMyEventIDs(ctx.getUser().getID());
				for (Integer ID : myIDs)
					events.add(dao.get(ID.intValue()));
			}
			
			events.addAll(dao.getFutureEvents()); // Load future events
		} catch (DAOException de) {
			throw new ServiceException(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Generate iCal header
		ctx.println("BEGIN:VCALENDAR");
		ctx.println("METHOD:PUBLISH");
		ctx.println("VERSION:2.0");
		ctx.println("PRODID:-//hacksw/handcal//NONSGML v1.0//EN");
		ctx.print("URL:https://");
		ctx.print(SystemData.get("airline.url"));
		ctx.println(ctx.getRequest().getRequestURI());
		ctx.print("DESCRIPTION:");
		ctx.print(SystemData.get("airline.name"));
		ctx.println(" Event Calendar");
		ctx.println("CALSCALE:GREGORIAN");
		
		// Write events
		for (Event e : events) {
			ctx.println("BEGIN:VEVENT");
			ctx.print("UID:");
			ctx.println(e.getHexID());
			ctx.print("DESCRIPTION:");
			ctx.println(e.getName());
			ctx.print("LOCATION:");
			ctx.println(String.valueOf(e.getNetwork()));
			ctx.print("URL:https://");
			ctx.print(SystemData.get("airline.url"));
			ctx.print("/event.do?id=");
			ctx.println(e.getHexID().substring(2));
			ctx.print("DTSTAMP:");
			ctx.println(StringUtils.format(e.getSignupDeadline(), "YYYYMMdd'T'HHmmss'Z'"));
			ctx.print("DTSTART:");
			ctx.println(StringUtils.format(e.getStartTime(), "YYYYMMdd'T'HHmmss'Z'"));
			ctx.print("DTEND:");
			ctx.println(StringUtils.format(e.getEndTime(), "YYYYMMdd'T'HHmmss'Z'"));
			ctx.println("END:VEVENT");
		}
		
		// Write footer
		ctx.println("END:VCALENDAR");
		
		// Write the data
		try {
		   ctx.setContentType("text/calendar", "utf-8");
		   ctx.setExpiry(600);
		   ctx.commit();
		} catch (Exception e) {
			throw new ServiceException(SC_CONFLICT, "I/O Error");
		}
		
		return SC_OK;
	}
}