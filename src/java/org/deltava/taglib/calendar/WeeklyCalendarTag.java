// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib.calendar;

import java.util.*;
import java.text.*;
import java.io.IOException;

import javax.servlet.jsp.*;

import org.deltava.taglib.XMLRenderer;
import org.deltava.util.CalendarUtils;

/**
 * A JSP tag to generate a weekly Calendar.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class WeeklyCalendarTag extends CalendarTag {

	private static final String[] DAYS = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
	
	private final DateFormat _wf = new SimpleDateFormat("MMMM dd, yyyy");
	private final DateFormat _df = new SimpleDateFormat("MMMM dd");

	private boolean _showDaysOfWeek;

	private XMLRenderer _table;
	private XMLRenderer _day;

	/**
	 * Sets the starting date for this calendar tag. This is overriden to be the first day of the week (Sunday).
	 * @param dt the start date
	 * @see CalendarTag#setStartDate(Date)
	 */
	public void setStartDate(Date dt) {
		Calendar cld = CalendarUtils.getInstance(dt);
		cld.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		_startDate = cld.getTime();
		calculateEndDate(Calendar.DATE, 7);
	}

	/**
	 * Sets wether the days of the week should be displayed below the month bar.
	 * @param showDOW TRUE if the days of the week should be displayed, otherwise FALSE
	 */
	public void setShowDaysOfWeek(boolean showDOW) {
		_showDaysOfWeek = showDOW;
	}

	private void openTableCell() throws JspException {
		_day = new XMLRenderer("td");
		_day.setAttribute("class", _contentClass);
		try {
			_out.print(_day.open(true));
		} catch (IOException ie) {
			throw new JspException(ie);
		}
	}

	private void closeTableCell() throws JspException {
		try {
			_out.print(_day.close());
		} catch (IOException ie) {
			throw new JspException(ie);
		}
	}

	/**
	 * Renders the Calendar header to the JSP output stream.
	 * @return TagSupport.EVAL_BODY_INCLUDE always
	 * @throws JspException if an I/O error occurs
	 */
	public int doStartTag() throws JspException {
		// Init the current date
		super.doStartTag();

		// Generate the view table
		_table = new XMLRenderer("table");
		_table.setAttribute("class", _tableClass);
		_table.setAttribute("cellspacing", String.valueOf(_cellSpace));
		_table.setAttribute("cellpadding", String.valueOf(_cellPad));

		_out = pageContext.getOut();
		try {
			_out.println(_table.open(true));

			// Write the header row
			_out.print("<tr>");
			XMLRenderer title = new XMLRenderer("td");
			title.setAttribute("colspan", "7");
			title.setAttribute("class", _topBarClass);
			_out.print(title.open(true));
			_out.print("Week of ");
			_out.print(_wf.format(_startDate));
			_out.print(title.close());
			_out.println("</tr>");
			
			// Write the day rows
			if (_showDaysOfWeek) {
				_out.print("<tr>");
				for (int x = 0; x < DAYS.length; x++) {
					_out.print("<td>");
					_out.print(DAYS[x]);
					_out.print("</td>");
				}
					
				_out.println("</tr>");
			}

			// Generate the week row
			_out.print("<tr>");
		} catch (IOException ie) {
			throw new JspException(ie);
		}

		// Open the first table cell and start
		openTableCell();
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Executed after the end of each day. The day table cell is closed and the superclass tag is called to determine if
	 * further records are required.
	 * @return TagSupport.EVAL_BODY_AGAIN if not at end of month, otherwise TagSupport.SKIP_BODY
	 * @throws JspException never
	 */
	public int doAfterBody() throws JspException {
		closeTableCell();
		return super.doAfterBody();
	}

	/**
	 * Closes the calendar table.
	 * @return TagSupport.EVAL_PAGE always
	 * @throws JspException if an I/O error occurs
	 */
	public int doEndTag() throws JspException {
		try {
			_out.println("</tr>");
			_table.close();
		} catch (IOException ie) {
			throw new JspException(ie);
		} finally {
			release();
		}

		return EVAL_PAGE;
	}
}