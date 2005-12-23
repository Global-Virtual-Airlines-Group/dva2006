// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib.calendar;

import java.util.*;
import java.io.IOException;

import javax.servlet.jsp.*;

/**
 * A JSP tag to generate a monthly calendar table.
 * @author Luke
 * @version 1.0
 * @since v1.0
 */

public class MonthlyCalendarTag extends CalendarTag {

	/* (non-Javadoc)
	 * @see org.deltava.taglib.calendar.CalendarTag#setStartDate(java.util.Date)
	 */
	@Override
	public void setStartDate(Date dt) {
		_startDate = dt;
		calculateEndDate(Calendar.MONTH, 1);
	}

	/* (non-Javadoc)
	 * @see org.deltava.taglib.calendar.CalendarTag#renderTableCell()
	 */
	@Override
	void openTableCell() throws IOException {
		// TODO Auto-generated method stub

	}
	
	/* (non-Javadoc)
	 * @see org.deltava.taglib.calendar.CalendarTag#renderTableCell()
	 */
	@Override
	void closeTableCell() throws IOException {
		// TODO Auto-generated method stub

	}

	public int doStartTag() throws JspException {
		
		// Render the view table
		JspWriter out = pageContext.getOut();
		try {
			out.print("<table cellspacing=\"");
			out.print(String.valueOf(_cellSpace));
			out.print("\" cellpadding=\"");
			out.print(String.valueOf(_cellPad));
			out.println("\">");
		} catch (IOException ie) {
			throw new JspException(ie);
		}
		
		return super.doStartTag();
	}
	
	public int doEndTag() throws JspException {
		
		// Figure out what day of week it is
		int dow = _currentDate.get(Calendar.DAY_OF_WEEK);
		
		// Render empty cells until we reach the end of the row
		JspWriter out = pageContext.getOut();
		try {
			while (dow <= Calendar.SATURDAY) {
				out.println("<td>&nbsp;</td>");
				dow++;
			}

			// Close the row and the table
			out.print("</tr></table>");
		} catch (IOException ie) {
			throw new JspException(ie);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}