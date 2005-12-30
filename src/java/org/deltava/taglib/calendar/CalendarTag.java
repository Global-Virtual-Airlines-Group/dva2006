// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib.calendar;

import java.util.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.deltava.beans.CalendarEntry;
import org.deltava.comparators.CalendarEntryComparator;

import org.deltava.util.CalendarUtils;
import org.deltava.util.system.SystemData;

/**
 * A JSP tag to display a calendar view table.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class CalendarTag extends TagSupport implements IterationTag {

	protected JspWriter _out;
	
	protected Date _startDate;
	protected Date _endDate;
	protected Calendar _currentDate;

	protected Collection<CalendarEntry> _entries = new TreeSet<CalendarEntry>(new CalendarEntryComparator());

	protected String _tableClass;
	protected String _topBarClass;
	protected String _dayBarClass;
	protected String _contentClass;
	
	protected int _cellPad = SystemData.getInt("html.table.spacing", 0);
	protected int _cellSpace = SystemData.getInt("html.table.padding", 0);

	public abstract void setStartDate(Date dt);
	
	protected void calculateEndDate(int intervalType, int amount) {
		Calendar cld = CalendarUtils.getInstance(_startDate);
		cld.add(intervalType, amount);
		_endDate = cld.getTime();
	}
	
	/**
	 * Sets the CSS class name for the view table.
	 * @param cName the CSS class name
	 */
	public void setTableClass(String cName) {
		_tableClass = cName;
	}
	
	/**
	 * Sets the CSS class name for the view table title bar.
	 * @param cName the CSS class name
	 */
	public void setTopBarClass(String cName) {
		_topBarClass = cName;
	}
	
	/**
	 * Sets the CSS class name for the view table day bar.
	 * @param cName the CSS class name
	 */
	public void setDayBarClass(String cName) {
		_dayBarClass = cName;
	}
	
	/**
	 * Sets the CSS class name for the view table content bar.
	 * @param cName the CSS class name
	 */
	public void setContentClass(String cName) {
		_contentClass = cName;
	}
	
    /**
     * Sets the CELLSPACING value for this table.
     * @param cSpacing the cellspacing attribute value.
     */
    public void setSpace(int cSpacing) {
    	_cellSpace = cSpacing;
    }

    /**
     * Sets the CELLPADDING value for this table.
     * @param cPadding the cellpadding attribute value.
     */
    public void setPad(int cPadding) {
    	_cellPad = cPadding;
    }

	/**
	 * Sets the entries to display in this calendar view table. Entries outside the table's date range will not be
	 * added.
	 * @param entries a Collection of CalendarEntry beans
	 * @see CalendarTag#getCurrentEntries()
	 */
	public final void setEntries(Collection<CalendarEntry> entries) {
		for (Iterator<CalendarEntry> i = entries.iterator(); i.hasNext();) {
			CalendarEntry ce = i.next();
			if ((ce.getDate().after(_startDate)) && (ce.getDate().before(_endDate)))
				_entries.add(ce);
		}
	}

	/**
	 * Returns all Calendar entries for the currently rendered Date, from midnight to 11:59PM.
	 * @return a Collection of CalendarEntry beans
	 * @see CalendarTag#setEntries(Collection)
	 */
	Collection<CalendarEntry> getCurrentEntries() {
		Collection<CalendarEntry> results = new ArrayList<CalendarEntry>();

		// Calculate the start/end points
		Calendar sd = (Calendar) _currentDate.clone();
		Calendar ed = (Calendar) _currentDate.clone();
		sd.add(Calendar.SECOND, -1);
		ed.add(Calendar.DATE, 1);

		// Get the entries
		for (Iterator<CalendarEntry> i = _entries.iterator(); i.hasNext();) {
			CalendarEntry ce = i.next();
			if ((ce.getDate().after(sd.getTime())) && (ce.getDate().before(ed.getTime())))
				results.add(ce);
		}

		return results;
	}

	/**
	 * Determines if we have further days to render in the calendar. Subclasses are responsible for
	 * opening and closing the table cells.
	 * @return EVAL_BODY_AGAIN if current date is before endDate, otherwise SKIP_BODY 
	 * @throws JspException never
	 */
	public int doAfterBody() throws JspException {
		_currentDate.add(Calendar.DATE, 1);
		return _currentDate.getTime().before(_endDate) ? EVAL_BODY_AGAIN : SKIP_BODY;
	}

	/**
	 * Starts the Calendar tag by setting the current date to render. Subclasses are responsible for
	 * opening and closing the table cells.
	 * @return TagSupport.EVAL_BODY_INCLUDE always
	 * @throws JspException never
	 */
	public int doStartTag() throws JspException {
		// Generate the current date
		_currentDate = CalendarUtils.getInstance(_startDate);
		_currentDate.set(Calendar.HOUR, 0);
		_currentDate.set(Calendar.MINUTE, 0);
		_currentDate.set(Calendar.SECOND, 0);
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		super.release();
		_cellPad = SystemData.getInt("html.table.spacing", 0);
		_cellSpace = SystemData.getInt("html.table.padding", 0);
		_tableClass = null;
		_topBarClass = null;
		_dayBarClass = null;
		_contentClass = null;
		_entries.clear();
	}
}