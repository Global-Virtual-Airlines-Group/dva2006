// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.calendar;

import java.util.*;
import java.text.*;
import java.io.IOException;

import javax.servlet.jsp.*;

import org.deltava.taglib.XMLRenderer;
import org.deltava.util.*;

/**
 * A JSP tag to generate a monthly calendar table.
 * @author Luke
 * @version 1.0
 * @since v1.0
 */

public class MonthlyCalendarTag extends CalendarTag {
	
	/**
	 * Sets the starting date for this monthly calendar tag. This is overriden to be the first
	 * day of the month.
	 * @param dt the start date
	 * @see CalendarTag#setStartDate(Date)
	 */
	public void setStartDate(Date dt) {
		Calendar cld = CalendarUtils.getInstance(dt, true);
		cld.set(Calendar.DAY_OF_MONTH, 1);
		_startDate = cld.getTime();
		calculateEndDate(Calendar.MONTH, 1);
	}
	
	/**
	 * Opens the table cell for a new day.
	 */
	private void openTableCell() throws JspException {
		// Generate the day elements
		_day = new XMLRenderer("td");
		_day.setAttribute("class", _contentClass);
		try {
			// If we're at the start of a new week, generate the headers and open the row
			if (_currentDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
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
		
		// Generate the table cell renderer and date formatted
		XMLRenderer dayHdr = new XMLRenderer("td");
		dayHdr.setAttribute("class", _dayBarClass);
		DateFormat df = new SimpleDateFormat(_showDaysOfWeek ? "EEE MMM dd" : "MMM dd");
		
		Calendar cd = CalendarUtils.getInstance(_currentDate.getTime());
		try {
			_out.println("<!-- Week of " + StringUtils.format(cd.getTime(), "MMMM dd") + " -->");
			if (openRow)
				_out.println("<tr>");
			
			// Render the table header cells
			for (int x = _currentDate.get(Calendar.DAY_OF_WEEK); x <= Calendar.SATURDAY; x++) {
				if (cd.getTime().before(_endDate)) {
					_out.print(dayHdr.open(true));
					_out.print(df.format(cd.getTime()));
					_out.print(dayHdr.close());
				} else {
					_out.println("<td rowspan=\"2\">&nbsp;</td>");
				}
				
				cd.add(Calendar.DATE, 1);
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
	public int doStartTag() throws JspException {
		// Init the current date
		super.doStartTag();
		
		try {
			_out.println(_table.open(true));
			
			// Write the header row
			DateFormat df = new SimpleDateFormat("MMMM yyyy");
			_out.print("<tr>");
			XMLRenderer title = new XMLRenderer("td");
			title.setAttribute("colspan", "7");
			title.setAttribute("class", _topBarClass);
			_out.print(title.open(true));
			_out.print(df.format(_startDate));
			_out.print(title.close());
			_out.println("</tr>");
			
			// Render the date row unless it's Sunday (since openTableCell() will do it then)
			if (_currentDate.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				// Generate the week row and empty table cells
				_out.print("<tr>");
				for (int x = Calendar.SUNDAY; x < _currentDate.get(Calendar.DAY_OF_WEEK); x++)
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
	public int doAfterBody() throws JspException {
		try {
			_out.print(_day.close());
			if (_currentDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
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
	public int doEndTag() throws JspException {
		try {
			// Close the row and the table
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