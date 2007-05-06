// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
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

	/**
	 * Sets the starting date for this calendar tag. This is overriden to be the first day of
	 * the week (Sunday).
	 * @param dt the start date
	 * @see CalendarTag#setStartDate(Date)
	 */
	public void setStartDate(Date dt) {
		Calendar cld = CalendarUtils.getInstance(dt, true);
		cld.add(Calendar.DATE, 1 - cld.get(Calendar.DAY_OF_WEEK));
		_startDate = cld.getTime();
		calculateEndDate(Calendar.DATE, 7);
	}

	/**
	 * Renders the Calendar header to the JSP output stream.
	 * @return TagSupport.EVAL_BODY_INCLUDE always
	 * @throws JspException if an I/O error occurs
	 */
	public int doStartTag() throws JspException {
		// Init the current date
		super.doStartTag();

		try {
			_out.println(_table.open(true));

			// Write the header row
			_out.print("<tr>");
			DateFormat wf = new SimpleDateFormat("MMMM dd, yyyy");
			XMLRenderer title = new XMLRenderer("td");
			title.setAttribute("colspan", "7");
			title.setAttribute("class", _topBarClass);
			_out.print(title.open(true));
			_out.print("Week of ");
			_out.print(wf.format(_startDate));
			_out.print(title.close());
			_out.println("</tr>");

			// Write the day rows
			if (_showDaysOfWeek) {
				Calendar dw = CalendarUtils.getInstance(_currentDate.getTime());
				DateFormat df = new SimpleDateFormat("EEE MMM dd");

				// Write the row
				_out.print("<tr>");
				for (int x = 0; x < 7; x++) {
					XMLRenderer dayHdr = new XMLRenderer("td");
					dayHdr.setAttribute("class", _dayBarClass);
					_out.print(dayHdr.open(true));
					_out.print(df.format(dw.getTime()));
					_out.print(dayHdr.close());
					dw.add(Calendar.DATE, 1);
				}

				_out.println("</tr>");
			}

			// Generate the week row and first day cell
			_out.print("<tr>");
			_day = new XMLRenderer("td");
			_day.setAttribute("class", _contentClass);
			_out.print(_day.open(true));
		} catch (IOException ie) {
			throw new JspException(ie);
		}

		// Start the tag
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Executed after the end of each day. The day table cell is closed and the superclass tag is called to determine if
	 * further records are required.
	 * @return TagSupport.EVAL_BODY_AGAIN if not at end of month, otherwise TagSupport.SKIP_BODY
	 * @throws JspException never
	 */
	public int doAfterBody() throws JspException {

		// Determine if we need to generate more cells
		int result = super.doAfterBody();
		try {
			_out.print(_day.close());

			// Open the next cell if we need to
			if (result == EVAL_BODY_AGAIN)
				_out.print(_day.open(true));
		} catch (IOException ie) {
			throw new JspException(ie);
		}

		return result;
	}

	/**
	 * Closes the calendar table.
	 * @return TagSupport.EVAL_PAGE always
	 * @throws JspException if an I/O error occurs
	 */
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