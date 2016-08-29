// Copyright 2006, 2007, 2011, 2012, 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

import java.util.*;
import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import org.deltava.beans.*;
import org.deltava.beans.system.*;

import org.deltava.util.*;

/**
 * An abstract Command class to support Calendar views.
 * @author Luke
 * @version 7.1
 * @since 1.0
 */

public abstract class AbstractCalendarCommand extends AbstractCommand {

	private static final List<ComboAlias> TYPE_OPTIONS = ComboUtils.fromArray(new String[] {"Month", "Week"}, new String[] { "31", "7"});
	
	protected static final class CalendarContext {
		
		private final DateRange _dr;
		private final int _days;
		
		CalendarContext(DateRange dr, int days) {
			super();
			_dr = dr;
			_days = days;
		}
		
		public Instant getStartDate() {
			return _dr.getStartDate();
		}
		
		public DateRange getRange() {
			return _dr;
		}
		
		public int getDays() {
			return _days;
		}
	}
	
	/**
	 * Initializes the Calendar context.
	 * @param ctx the Command context
	 * @return a CalendarContext bean
	 * @throws CommandException if the specified start date is invalid
	 */
	protected static CalendarContext initCalendar(CommandContext ctx) throws CommandException {
		
		// Get the browser context and check for mobile
		HTTPContextData httpCtx = (HTTPContextData) ctx.getRequest().getAttribute(HTTPContext.HTTPCTXT_ATTR_NAME);
		boolean isMobile = (httpCtx != null) && (httpCtx.getDeviceType() == DeviceType.PHONE);
		
		// Get the number of days and start date
		int days = StringUtils.parse((String) ctx.getCmdParameter(OPERATION, null), isMobile ? 7 : 31);
		ZoneId tz = ctx.isAuthenticated() ? ctx.getUser().getTZ().getZone() : ZoneId.systemDefault();
		ZonedDateTime startDate = null;
		try {
			startDate = StringUtils.parseLocal(ctx.getParameter("startDate"), "MM/dd/yyyy", tz).truncatedTo(ChronoUnit.DAYS);
		} catch (Exception e) {
			startDate = ZonedDateTime.now(tz);
		}
		
		// Create the current date in the user's local time and determine what the local equivalent is - this will
		// be used to load in calendar entries, but the startDate object in the request should be the user's local
		// time and not translated to server time
		if (days == 7) {
			int daysToSub = startDate.get(ChronoField.DAY_OF_WEEK);
			if (daysToSub < 7)
				startDate = startDate.minusDays(daysToSub);
		} else
			startDate = startDate.minusDays(startDate.get(ChronoField.DAY_OF_MONTH) - 1);
		
		// Check start/end date
		if ((startDate.get(ChronoField.YEAR) < 2000) || (Duration.between(Instant.now(), startDate.toInstant()).toDays() > 730))
			throw notFoundException("Invalid start date - " + StringUtils.format(startDate, "MM/dd/yyyy"));
		
		// Save the calendar options in the request
		ctx.setAttribute("startDate", startDate, REQUEST);
		ctx.setAttribute("startDays", Integer.valueOf(days), REQUEST);
		ctx.setAttribute("typeOptions", TYPE_OPTIONS, REQUEST);
		
		// Build and return the Calendar context
		DateRange dr = (days == 7) ? DateRange.createWeek(startDate) : DateRange.createMonth(startDate);
		return new CalendarContext(dr, days);
	}
}