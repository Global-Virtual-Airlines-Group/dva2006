// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

import java.util.*;
import java.text.*;

import org.deltava.beans.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * An abstract Command class to support Calendar views.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class AbstractCalendarCommand extends AbstractCommand {

	private static final DateFormat _df = new SimpleDateFormat("MM/dd/yyyy");
	private static final List<ComboAlias> TYPE_OPTIONS = ComboUtils.fromArray(new String[] {"Month", "Week"}, 
			new String[] { "31", "7"});
	
	protected final class CalendarContext {
		
		private DateTime _startDate;
		private int _days;
		
		CalendarContext(DateTime sd, int days) {
			super();
			_startDate = sd;
			_days = days;
		}
		
		public DateTime getStartDateTime() {
			return _startDate; 
		}
		
		public Date getStartDate() {
			return _startDate.getDate();
		}
		
		public int getDays() {
			return _days;
		}
	}
	
	/**
	 * Helper method to get the start of the month.
	 */
	private Date getStartOfMonth(Date dt) {
		Calendar cld = CalendarUtils.getInstance(dt, true);
		cld.set(Calendar.DAY_OF_MONTH, 1);
		return cld.getTime();
	}

	/**
	 * Helper method to get the start of the week.
	 */
	private Date getStartOfWeek(Date dt) {
		Calendar cld = CalendarUtils.getInstance(dt, true);
		cld.add(Calendar.DATE, 1 - cld.get(Calendar.DAY_OF_WEEK));
		return cld.getTime();
	}
	
	/**
	 * Initializes the Calendar context.
	 * @param ctx the Command context
	 * @return a CalendarContext bean
	 */
	protected CalendarContext initCalendar(CommandContext ctx) {
		
		// Get the number of days and start date
		int days = 31;
		Date startDate = null;
		try {
			startDate = _df.parse(ctx.getParameter("startDate"));
			days = Integer.parseInt((String) ctx.getCmdParameter(OPERATION, "31"));
		} catch (Exception e) {
			startDate = new Date();
		}
		
		// Calculate the proper start date
		startDate = (days == 7) ? getStartOfWeek(startDate) : getStartOfMonth(startDate);
		
		// Create the current date in the user's local time and determine what the local equivalent is
		TZInfo tz = ctx.isAuthenticated() ? ctx.getUser().getTZ() : TZInfo.get(SystemData.get("time.timezone"));
		DateTime ldt = new DateTime(startDate, tz);
		ldt.convertTo(TZInfo.local());

		// Save the calendar options in the request
		ctx.setAttribute("startDate", ldt.getDate(), REQUEST);
		ctx.setAttribute("typeOptions", TYPE_OPTIONS, REQUEST);
		
		// Build and return the Calendar context
		return new CalendarContext(ldt, days);
	}
}