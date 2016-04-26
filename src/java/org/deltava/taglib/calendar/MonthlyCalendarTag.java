// Copyright 2005, 2007, 2008, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.calendar;

import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.io.IOException;

import javax.servlet.jsp.*;

import org.deltava.taglib.XMLRenderer;
import org.deltava.util.*;

/**
 * A JSP tag to generate a monthly calendar table.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class MonthlyCalendarTag extends CalendarTag {
	
	/**
	 * Sets the starting date for this monthly calendar tag. This is overriden to be the first
	 * day of the month.
	 * @param dt the start date
	 * @see CalendarTag#setStartDate(ZonedDateTime)
	 */
	@Override
	public void setStartDate(ZonedDateTime dt) {
		_startDate = dt.truncatedTo(ChronoUnit.DAYS);
		calculateEndDate(ChronoUnit.MONTHS, 1);
	}
	
	/**
	 * Returns the label for the scroll backwards link.
	 */
	@Override
	protected String getBackLabel() {
		ZonedDateTime zdt = _startDate.minus(_intervalLength, _intervalType);
		return StringUtils.format(zdt, "MMMM yyyy");
	}
	
	/**
	 * Returns the label for the scroll forwards link.
	 */
	@Override
	protected String getForwardLabel() {
		ZonedDateTime zdt = _startDate.plus(_intervalLength, _intervalType);
		return StringUtils.format(zdt, "MMMM yyyy");
	}
	
	/*
	 * Opens the table cell for a new day.
	 */
	private void openTableCell() throws JspException {
		// Generate the day elements
		_day = new XMLRenderer("td");
		_day.setAttribute("class", _contentClass);
		try {
			// If we're at the start of a new week, generate the headers and open the row
			if (_currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
				renderDateRow(true);
				_out.print("<tr>");
			}
			
			// Render the day cell
			_out.print(_day.open(true));
		} catch (IOException ie) {
			throw new JspException(ie);
		}
	}
	
	private void renderDateRow(boolean openRow) throws JspException {
		
		// Generate the table cell renderer and date formatter
		XMLRenderer dayHdr = new XMLRenderer("td");
		DateTimeFormatter df = DateTimeFormatter.ofPattern(_showDaysOfWeek ? "EEE MMM dd" : "MMM dd");
		ZonedDateTime cd = ZonedDateTime.from(_currentDate);
		try {
			_out.println("<!-- Week of " + StringUtils.format(_currentDate, "MMMM dd") + " -->");
			if (openRow)
				_out.println("<tr>");
			
			// Render the table header cells, convert day of week from 1-7 Mon-Sun to 0-7 Sun-Sat
			DayOfWeek dow = _currentDate.getDayOfWeek(); int dayNumber = (dow.getValue() == 7) ? 0 : dow.getValue();
			for (int x = dayNumber; x <= DayOfWeek.SATURDAY.getValue(); x++) {
				if (cd.isBefore(_endDate)) {
					dayHdr.setAttribute("class", getHeaderClass(cd));
					_out.print(dayHdr.open(true));
					_out.print(df.format(cd)); // convert to zdt
					_out.print(dayHdr.close());
				} else {
					_out.print("<td class=\"");
					_out.print(getContentClass(cd)); // convert to zdt
					_out.println("\" rowspan=\"2\">&nbsp;</td>");
				}
				
				cd = cd.plus(1, ChronoUnit.DAYS);
			}

			_out.println("</tr>");
		} catch (IOException ie) {
			throw new JspException(ie);
		}
	}

	/**
	 * Renders the Calendar header to the JSP output stream.
	 * @return TagSupport.EVAL_BODY_INCLUDE always
	 * @throws JspException if an I/O error occurs
	 */
	@Override
	public int doStartTag() throws JspException {
		// Init the current date
		super.doStartTag();
		
		try {
			_out.println(_table.open(true));
			
			// Write the header row
			DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM yyyy");
			_out.print("<tr>");
			XMLRenderer title = new XMLRenderer("td");
			title.setAttribute("colspan", "7");
			title.setAttribute("class", _topBarClass);
			_out.print(title.open(true));
			_out.print(df.format(_startDate));
			_out.print(title.close());
			_out.println("</tr>");
			
			// Render the date row unless it's Sunday (since openTableCell() will do it then)
			if (_currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
				int dow = _currentDate.getDayOfWeek().getValue() + 1;
				
				// Generate the week row and empty table cells
				_out.print("<tr>");
				for (int x = Calendar.SUNDAY; x < dow; x++)
					_out.println("<td rowspan=\"2\">&nbsp;</td>");
				
				renderDateRow(false);
				_out.print("<tr>");
			}
		} catch (IOException ie) {
			throw new JspException(ie);
		}
		
		// Open the first table cell and start
		openTableCell();
		return EVAL_BODY_INCLUDE;
	}
	
	/**
	 * Executed after the end of each day. The day table cell is closed and the superclass tag is called
	 * to determine if further records are required.
	 * @return TagSupport#EVAL_BODY_AGAIN if not at end of month, otherwise TagSupport#SKIP_BODY
	 * @throws JspException never
	 */
	@Override
	public int doAfterBody() throws JspException {
		try {
			_out.print(_day.close());
			if (_currentDate.getDayOfWeek() == DayOfWeek.SATURDAY)
				_out.println("</tr>");
		} catch (IOException ie) {
			throw new JspException(ie);
		}
		
		// Determine if we need to open a new cell
		int result = super.doAfterBody();
		if (result == EVAL_BODY_AGAIN)
			openTableCell();
		
		return result; 
	}
	
	/**
	 * Closes the calendar table.
	 * @return TagSupport#EVAL_PAGE always
	 * @throws JspException if an I/O error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			_out.println("</tr>");
			super.doEndTag();
			_out.println(_table.close());
		} catch (IOException ie) {
			throw new JspException(ie);
		} finally {
			release();
		}

		return EVAL_PAGE;
	}
}