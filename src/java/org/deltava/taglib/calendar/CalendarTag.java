// Copyright 2005, 2007, 2008, 2010, 2012, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.calendar;

import java.util.*;
import java.util.stream.Collectors;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import javax.servlet.http.HttpServletRequest;

import org.deltava.beans.*;
import org.deltava.taglib.XMLRenderer;
import org.deltava.util.*;

/**
 * A JSP tag to display a calendar view table.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

abstract class CalendarTag extends TagSupport {

	private final DateTimeFormatter _df = DateTimeFormatter.ofPattern("MM/dd/yyyy");
	private static final Collection<String> RESERVED_PARAMS = Collections.singleton("startDate");

	protected JspWriter _out;

	// Should all be user's local time
	protected ZonedDateTime _startDate;
	protected ZonedDateTime _endDate;
	protected ZonedDateTime _currentDate;
	protected ZonedDateTime _today;
	protected ZoneId _tz;

	private String _currentDateAttr;

	protected final Collection<CalendarEntry> _entries = new TreeSet<CalendarEntry>();

	protected String _tableID;
	protected String _tableClass;
	protected String _topBarClass;
	protected String _dayBarClass;
	protected String _contentClass;
	private String _scrollRowClass;
	private String _cmdName;

	protected int _border;
	protected boolean _showDaysOfWeek = true;
	private boolean _showScrollTags = true;

	protected XMLRenderer _table;
	protected XMLRenderer _day;

	protected ChronoUnit _intervalType = ChronoUnit.DAYS;
	protected int _intervalLength = 7;

	/**
	 * Sets the start date of the data range.
	 * @param dt the start date/time
	 */
	public abstract void setStartDate(ZonedDateTime dt);

	/**
	 * Returns the label for the scroll backwards link.
	 * @return the link label
	 * @see CalendarTag#getForwardLabel()
	 */
	protected abstract String getBackLabel();

	/**
	 * Returns the label for the scroll forwards link
	 * @return the link label
	 * @see CalendarTag#getBackLabel()
	 */
	protected abstract String getForwardLabel();

	/**
	 * Calculates the CSS class(es) for the content table cell.
	 * @param dt the cell date
	 * @return a CSS class list
	 */
	protected String getContentClass(ZonedDateTime dt) {
		boolean isToday = _today.equals(dt);
		StringBuilder buf = new StringBuilder();
		if (_contentClass != null) {
			buf.append(_contentClass);
			if (isToday)
				buf.append(' ');
		}

		if (isToday)
			buf.append("calendarToday");

		return buf.toString();
	}

	/**
	 * Calculates the CSS class(es) for the header table cell.
	 * @param dt the cell date
	 * @return a CSS class list
	 */
	protected String getHeaderClass(ZonedDateTime dt) {
		boolean isToday = _today.equals(dt);
		StringBuilder buf = new StringBuilder();
		if (_dayBarClass != null) {
			buf.append(_dayBarClass);
			if (isToday)
				buf.append(' ');
		}

		if (isToday)
			buf.append("calendarToday");

		return buf.toString();
	}

	/**
	 * Calculcates the end date based on the start date and a particular interval amount. <i>This must be called by a subclass for the
	 * forward/backward links to work properly</i>.
	 * @param intervalType the interval type (use Calendar constants)
	 * @param amount the size of the interval
	 * @see java.util.Calendar
	 */
	protected void calculateEndDate(ChronoUnit intervalType, int amount) {
		_endDate = _startDate.plus(amount, intervalType);
		_intervalType = intervalType;
		_intervalLength = amount;
	}

	/**
	 * Sets the request attribute name for the current date.
	 * @param attrName the request attribute name
	 */
	public void setDate(String attrName) {
		_currentDateAttr = attrName;
	}

	/**
	 * Sets the CSS class name for the view table.
	 * @param cName the CSS class name
	 */
	public void setTableClass(String cName) {
		_tableClass = cName;
	}

	/**
	 * Sets the CSS ID for the view table.
	 * @param id the ID
	 */
	public void setTableID(String id) {
		_tableID = id;
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
	 * Sets the CSS class name for the forwad/backward scroll links.
	 * @param cName the CSS class name
	 */
	public void setScrollClass(String cName) {
		_scrollRowClass = cName;
	}

	/**
	 * Sets the command name to use when scrolling the view.
	 * @param cmdName the command name
	 */
	public void setCmd(String cmdName) {
		_cmdName = cmdName;
	}

	/**
	 * Sets whether the days of the week should be displayed below the title bar.
	 * @param showDOW TRUE if the days of the week should be displayed, otherwise FALSE
	 */
	public void setShowDaysOfWeek(boolean showDOW) {
		_showDaysOfWeek = showDOW;
	}

	/**
	 * Sets whether to display the forward/backward scroll tags.
	 * @param showScroll TRUE if forward/back links are displayed, otherwise FALSE
	 */
	public void setScrollTags(boolean showScroll) {
		_showScrollTags = showScroll;
	}

	/**
	 * Sets the BORDER value for this table.
	 * @param border the border width attribute value
	 */
	public void setBorder(int border) {
		_border = border;
	}

	/**
	 * Sets the entries to display in this calendar view table. Entries outside the table's date range will not be added.
	 * @param entries a Collection of CalendarEntry beans
	 * @see CalendarTag#getCurrentEntries()
	 */
	public final void setEntries(Collection<CalendarEntry> entries) {
		for (CalendarEntry ce : entries) {
			if ((ce.getDate().isAfter(_startDate.toInstant())) && (ce.getDate().isBefore(_endDate.toInstant())))
				_entries.add(ce);
		}
	}

	/**
	 * Sets the page context for the tag, and sets the user's time zone.
	 * @param ctx the JSP page context
	 */
	@Override
	public void setPageContext(PageContext ctx) {
		super.setPageContext(ctx);

		// Determine the user's time zone
		HttpServletRequest hreq = (HttpServletRequest) ctx.getRequest();
		Person usr = (Person) hreq.getUserPrincipal();
		_tz = (usr != null) ? usr.getTZ().getZone() : ZoneId.systemDefault();

		// Determine today's date in user's time zone
		_today = ZonedDateTime.ofInstant(Instant.now(), _tz).truncatedTo(ChronoUnit.DAYS);
	}

	/*
	 * Helper method to bundle request parameters into a URL string.
	 */
	private String buildURL(Map<String, Object> params) {
		StringBuilder url = new StringBuilder("/").append(_cmdName).append(".do?");
		for (Iterator<String> i = params.keySet().iterator(); i.hasNext();) {
			String pName = i.next();
			String[] pValues = (String[]) params.get(pName);
			url.append(StringUtils.stripInlineHTML(pName));
			url.append('=');
			url.append(StringUtils.stripInlineHTML(pValues[0]));
			if (i.hasNext())
				url.append("&amp;");
		}

		return url.toString();
	}

	/**
	 * Returns all Calendar entries for the currently rendered Date, from midnight to 11:59PM.
	 * @return a Collection of CalendarEntry beans
	 * @see CalendarTag#setEntries(Collection)
	 */
	Collection<CalendarEntry> getCurrentEntries() {
		// Calculate the start/end points of the current date in the users's local time, in UTC
		Instant sd = _currentDate.toInstant().minus(1, ChronoUnit.MILLIS);
		Instant ed = sd.plus(1, ChronoUnit.DAYS).plus(2, ChronoUnit.MILLIS);
		return _entries.stream().filter(ce -> (ce.getDate().isAfter(sd) && ce.getDate().isBefore(ed))).collect(Collectors.toList());
	}

	/**
	 * Determines if we have further days to render in the calendar. Subclasses are responsible for opening and closing the table cells.
	 * @return EVAL_BODY_AGAIN if current date is before endDate, otherwise SKIP_BODY
	 * @throws JspException never
	 */
	@Override
	public int doAfterBody() throws JspException {
		_currentDate = _currentDate.plus(1, ChronoUnit.DAYS);
		if (_currentDateAttr != null)
			pageContext.setAttribute(_currentDateAttr, _currentDate, PageContext.REQUEST_SCOPE);

		return _currentDate.isBefore(_endDate) ? EVAL_BODY_AGAIN : SKIP_BODY;
	}

	/**
	 * Starts the Calendar tag by setting the current date to render. Subclasses are responsible for opening and closing the table cells.
	 * @return TagSupport.EVAL_BODY_INCLUDE always
	 * @throws JspException never
	 */
	@Override
	public int doStartTag() throws JspException {

		// Generate the current date
		_currentDate = _startDate.truncatedTo(ChronoUnit.DAYS);

		// Save the current date in the request
		if (_currentDateAttr != null)
			pageContext.setAttribute(_currentDateAttr, _currentDate, PageContext.REQUEST_SCOPE);

		// Generate the view table
		_table = new XMLRenderer("table");
		_table.setAttribute("id", _tableID);
		_table.setAttribute("class", _tableClass);
		if (_border != 0)
			_table.setAttribute("border", String.valueOf(_border));

		// Init the renderer and return
		_out = pageContext.getOut();
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Ends the Calendar tag by adding the scroll forward/backward link row. Subclasses are responsible for closing the current table row
	 * and closing the table.
	 * @return EVAL_PAGE always
	 * @throws JspException if an I/O error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		if (!_showScrollTags)
			return EVAL_PAGE;

		// Get the URL parameters
		Map<String, Object> params = new HashMap<String, Object>(pageContext.getRequest().getParameterMap());
		params.keySet().removeAll(RESERVED_PARAMS);

		// Calculate the next and previous page startDates
		ZonedDateTime fd = _startDate.plus(_intervalLength, _intervalType);
		ZonedDateTime bd = _startDate.minus(_intervalLength, _intervalType);
		Duration d = Duration.between(Instant.now(), fd.toInstant());
		boolean hasPrev = (bd.get(ChronoField.YEAR) > 2000);
		boolean hasNext = !d.isNegative() || (d.toDays() < 740);

		try {
			// Build the backward URL
			XMLRenderer backURL = new XMLRenderer("a");
			params.put("startDate", new String[] { _df.format(bd) });
			backURL.setAttribute("href", buildURL(params));

			// Build the forward URL
			XMLRenderer fwdURL = new XMLRenderer("a");
			params.put("startDate", new String[] { _df.format(fd) });
			fwdURL.setAttribute("href", buildURL(params));

			// Render the scroll tag bar row
			XMLRenderer scrollRow = new XMLRenderer("tr");
			scrollRow.setAttribute("class", _scrollRowClass);
			_out.println(scrollRow.open(true));

			// Render the scroll back cell and link
			XMLRenderer sBackCell = new XMLRenderer("td");
			sBackCell.setAttribute("class", "left");
			sBackCell.setAttribute("colspan", "3");
			_out.print(sBackCell.open(true));
			if (hasPrev) {
				_out.print(backURL.open(true));
				_out.print(getBackLabel());
				_out.print(backURL.close());
			} else
				_out.print("&nbsp;");
			
			_out.print(sBackCell.close());

			// Render the middle cell
			_out.print("<td>&nbsp;</td>");

			// Render the scroll forward cell and link
			XMLRenderer sFwdCell = new XMLRenderer("td");
			sFwdCell.setAttribute("class", "right");
			sFwdCell.setAttribute("colspan", "3");
			_out.print(sFwdCell.open(true));
			if (hasNext) {
				_out.print(fwdURL.open(true));
				_out.print(getForwardLabel());
				_out.print(fwdURL.close());
			} else
				_out.print("&nbsp;");
			
			_out.print(sFwdCell.close());

			// Close the row
			_out.println(scrollRow.close());
		} catch (Exception ie) {
			throw new JspException(ie);
		} finally {
			release();
		}

		return EVAL_PAGE;
	}

	/**
	 * Releases the tag's state variables.
	 */
	@Override
	public void release() {
		super.release();
		_border = 0;
		_showDaysOfWeek = true;
		_showScrollTags = true;
		_tableClass = null;
		_topBarClass = null;
		_dayBarClass = null;
		_contentClass = null;
		_currentDateAttr = null;
		_intervalType = ChronoUnit.DAYS;
		_intervalLength = 7;
		_entries.clear();
	}
}