// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib.calendar;

import java.util.*;
import java.text.*;
import java.io.IOException;

import javax.servlet.jsp.*;

import org.deltava.taglib.XMLRenderer;
import org.deltava.util.CalendarUtils;

/**
 * A JSP tag to generate a monthly calendar table.
 * @author Luke
 * @version 1.0
 * @since v1.0
 */

public class MonthlyCalendarTag extends CalendarTag {
	
	private static final String[] DAYS = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"}; 
	
	private boolean _showDaysOfWeek;
	
	private XMLRenderer _table;
	private XMLRenderer _dayTable;
	private XMLRenderer _day;

	/**
	 * Sets the starting date for this monthly calendar tag. This is overriden to be the first
	 * day of the month.
	 * @param dt the start date
	 * @see CalendarTag#setStartDate(Date)
	 */
	public void setStartDate(Date dt) {
		Calendar cld = CalendarUtils.getInstance(dt);
		cld.set(Calendar.DAY_OF_MONTH, 1);
		_startDate = cld.getTime();
		calculateEndDate(Calendar.MONTH, 1);
	}
	
	/**
	 * Sets wether the days of the week should be displayed below the month bar.
	 * @param showDOW TRUE if the days of the week should be displayed, otherwise FALSE
	 */
	public void setShowDaysOfWeek(boolean showDOW) {
		_showDaysOfWeek = showDOW;
	}

	/**
	 * Opens the table cell for a new day.
	 */
	private void openTableCell() throws JspException {
		// Since we have a day header, each cell is in its own table
		_dayTable = new XMLRenderer("table");
		_dayTable.setAttribute("border", String.valueOf(_border));
		
		// Generate the day elements
		XMLRenderer hdr = new XMLRenderer("td");
		hdr.setAttribute("class", _dayBarClass);
		_day = new XMLRenderer("td");
		_day.setAttribute("class", _contentClass);
		try {
			// If we're at the start of a new week, open the row
			if (_currentDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
				_out.print("<tr>");
			
			// Render the day cell
			_out.print(_dayTable.open(true));
			_out.print("<tr>");
			_out.print(hdr.open(true));
			_out.print(String.valueOf(_currentDate.get(Calendar.DAY_OF_MONTH)));
			_out.print(hdr.close());
			_out.print(_day.open(true));
		} catch (IOException ie) {
			throw new JspException(ie);
		}
	}
	
	/**
	 * Closes the table cell for a day. If the current day of week is a Saturday, then the
	 * calendar table row will also be closed.
	 */
	private void closeTableCell() throws JspException {
		try {
			_out.print(_day.close());
			_out.print("</tr>");
			_out.print(_dayTable.close());
			if (_currentDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
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
		
		// Generate the view table
		_table = new XMLRenderer("table");
		_table.setAttribute("class", _tableClass);
		_table.setAttribute("cellspacing", String.valueOf(_cellSpace));
		_table.setAttribute("cellpadding", String.valueOf(_cellPad));
		_table.setAttribute("border", String.valueOf(_border));
		
		_out = pageContext.getOut();
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
			
			// Generate the week row and empty table cells
			_out.print("<tr>");
			for (int x = Calendar.SUNDAY; x < _currentDate.get(Calendar.DAY_OF_WEEK); x++)
				_out.println("<td rowspan=\"2\">&nbsp;</td>");
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
			// Render empty cells until we reach the end of the row
			for (int x = _currentDate.get(Calendar.DAY_OF_WEEK); x <= Calendar.SATURDAY; x++)
				_out.println("<td rowspan=\"2\">&nbsp;</td>");

			// Close the row and the table
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