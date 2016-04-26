// Copyright 2005, 2007, 2008, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.calendar;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.io.IOException;

import javax.servlet.jsp.*;

import org.deltava.taglib.XMLRenderer;
import org.deltava.util.StringUtils;

/**
 * A JSP tag to generate a weekly Calendar.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class WeeklyCalendarTag extends CalendarTag {

	/**
	 * Sets the starting date for this calendar tag. This is overriden to be the first day of
	 * the week (Sunday).
	 * @param dt the start date
	 * @see CalendarTag#setStartDate(ZonedDateTime)
	 */
	@Override
	public void setStartDate(ZonedDateTime dt) {
		_startDate = dt.truncatedTo(ChronoUnit.DAYS);
		calculateEndDate(ChronoUnit.WEEKS, 1);
	}
	
	/**
	 * Returns the label for the scroll backwards link.
	 */
	@Override
	protected String getBackLabel() {
		ZonedDateTime zdt = _startDate.minus(_intervalLength, _intervalType);
		return "WEEK OF " + StringUtils.format(zdt, "MMM dd yyyy");
	}
	
	/**
	 * Returns the label for the scroll forwards link.
	 */
	@Override
	protected String getForwardLabel() {
		ZonedDateTime zdt = _startDate.plus(_intervalLength, _intervalType);
		return "WEEK OF " + StringUtils.format(zdt, "MMM dd yyyy");
	}

	/**
	 * Renders the Calendar header to the JSP output stream.
	 * @return TagSupport.EVAL_BODY_INCLUDE always
	 * @throws JspException if an I/O error occurs
	 */
	@Override
	public int doStartTag() throws JspException {
		super.doStartTag();

		try {
			_out.println(_table.open(true));

			// Write the header row
			_out.print("<tr>");
			DateTimeFormatter wf = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
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
				ZonedDateTime dw = ZonedDateTime.from(_currentDate);
				DateTimeFormatter df = DateTimeFormatter.ofPattern("EEE MMM dd");

				// Write the row
				_out.print("<tr>");
				for (int x = 0; x < 7; x++) {
					XMLRenderer dayHdr = new XMLRenderer("td");
					dayHdr.setAttribute("class", getHeaderClass(dw));
					_out.print(dayHdr.open(true));
					_out.print(df.format(dw));
					_out.print(dayHdr.close());
					dw = dw.plus(1, ChronoUnit.DAYS);
				}

				_out.println("</tr>");
			}

			// Generate the week row and first day cell
			_out.print("<tr>");
			_day = new XMLRenderer("td");
			_day.setAttribute("class", getContentClass(_currentDate));
			_out.print(_day.open(true));
		} catch (IOException ie) {
			throw new JspException(ie);
		}

		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Executed after the end of each day. The day table cell is closed and the superclass tag is called to determine if
	 * further records are required.
	 * @return TagSupport.EVAL_BODY_AGAIN if not at end of month, otherwise TagSupport.SKIP_BODY
	 * @throws JspException never
	 */
	@Override
	public int doAfterBody() throws JspException {

		// Determine if we need to generate more cells
		int result = super.doAfterBody();
		try {
			_out.print(_day.close());

			// Open the next cell if we need to
			if (result == EVAL_BODY_AGAIN) {
				_day.setAttribute("class", getContentClass(_currentDate));
				_out.print(_day.open(true));
			}
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