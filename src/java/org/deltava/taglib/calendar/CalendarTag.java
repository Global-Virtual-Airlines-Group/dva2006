// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib.calendar;

import java.util.*;
import java.text.*;
import java.io.IOException;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import javax.servlet.http.HttpServletRequest;

import org.deltava.beans.*;

import org.deltava.taglib.XMLRenderer;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A JSP tag to display a calendar view table.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

abstract class CalendarTag extends TagSupport implements IterationTag {
	
	private final DateFormat _df = new SimpleDateFormat("MM/dd/yyyy");
	private static final List<String> RESERVED_PARAMS = Arrays.asList(new String[] {"startDate"});

	protected JspWriter _out;
	
	protected Date _startDate;
	protected Date _endDate;
	protected Calendar _currentDate;
	protected TZInfo _tz;
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
	
	protected int _cellPad = SystemData.getInt("html.table.spacing", 0);
	protected int _cellSpace = SystemData.getInt("html.table.padding", 0);

	public abstract void setStartDate(Date dt);
	
	/**
	 * Calculcates the end date based on the start date and a particular interval amount
	 * @param intervalType the interval type (use Calendar constants)
	 * @param amount the size of the interval
	 * @see java.util.Calendar
	 */
	protected void calculateEndDate(int intervalType, int amount) {
		Calendar cld = CalendarUtils.getInstance(_startDate);
		cld.add(intervalType, amount);
		_endDate = cld.getTime();
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
	
	public void setScrollClass(String cName) {
		_scrollRowClass = cName;
	}
	
	public void setCmd(String cmdName) {
		_cmdName = cmdName;
	}
	
	/**
	 * Sets wether the days of the week should be displayed below the title bar.
	 * @param showDOW TRUE if the days of the week should be displayed, otherwise FALSE
	 */
	public void setShowDaysOfWeek(boolean showDOW) {
		_showDaysOfWeek = showDOW;
	}
	
	public void setScrollTags(boolean showScroll) {
		_showScrollTags = showScroll;
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
     * Sets the BORDER value for this table.
     * @param border the border width attribute value
     */
    public void setBorder(int border) {
    	_border = border;
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
			DateTime edt = new DateTime(ce.getDate(), TZInfo.local());
			edt.convertTo(_tz);
			if ((edt.getDate().after(_startDate)) && (edt.getDate().before(_endDate)))
				_entries.add(ce);
		}
	}
	
	/**
	 * Sets the page context for the tag, and sets the user's time zone.
	 * @param ctx the JSP page context
	 */
	public void setPageContext(PageContext ctx) {
		super.setPageContext(ctx);
		
		// Determine the user's time zone
		HttpServletRequest hreq = (HttpServletRequest) ctx.getRequest();
		Person usr = (Person) hreq.getUserPrincipal();
		_tz = (usr != null) ? usr.getTZ() : TZInfo.get(SystemData.get("time.timezone"));
	}
	
    /**
     * Helper method to bundle request parameters into a URL string.
     */
    private String buildURL(Map<String, Object> params) {
    	// Build the URL
        StringBuilder url = new StringBuilder("/");
        url.append(_cmdName);
        url.append(".do?");
        
        // Loop through the parameters
        for (Iterator<String> i = params.keySet().iterator(); i.hasNext(); ) {
            String pName = i.next();
            String[] pValues = (String[]) params.get(pName);
            url.append(StringUtils.stripInlineHTML(pName));
            url.append('=');
            url.append(StringUtils.stripInlineHTML(pValues[0]));
            if (i.hasNext())
                url.append("&amp;");
        }
        
        // Return the string
        return url.toString();
    }

	/**
	 * Returns all Calendar entries for the currently rendered Date, from midnight to 11:59PM.
	 * @return a Collection of CalendarEntry beans
	 * @see CalendarTag#setEntries(Collection)
	 */
	Collection<CalendarEntry> getCurrentEntries() {
		Collection<CalendarEntry> results = new ArrayList<CalendarEntry>();

		// Create the current date in the user's local time and determine what the local equivalent is
		DateTime ldt = new DateTime(_currentDate.getTime(), _tz);
		ldt.convertTo(TZInfo.local());
		
		// Calculate the start/end points in the user's local time
		Calendar sd = CalendarUtils.getInstance(ldt.getDate()); 
		Calendar ed = CalendarUtils.getInstance(ldt.getDate());			
		sd.add(Calendar.SECOND, -1);
		ed.add(Calendar.DATE, 1);

		// Get the entries
		for (Iterator<CalendarEntry> i = _entries.iterator(); i.hasNext();) {
			CalendarEntry ce = i.next();
			Date entryDate = ce.getDate();
			if ((entryDate.after(sd.getTime())) && (entryDate.before(ed.getTime())))
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
		if (_currentDateAttr != null)
			pageContext.getRequest().setAttribute(_currentDateAttr, _currentDate.getTime());
			
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
		_currentDate.set(Calendar.HOUR_OF_DAY, 0);
		_currentDate.set(Calendar.MINUTE, 0);
		_currentDate.set(Calendar.SECOND, 0);
		
		// Save the current date in the request
		if (_currentDateAttr != null)
			pageContext.getRequest().setAttribute(_currentDateAttr, _currentDate.getTime());
		
		// Generate the view table
		_table = new XMLRenderer("table");
		_table.setAttribute("id", _tableID);
		_table.setAttribute("class", _tableClass);
		_table.setAttribute("cellspacing", String.valueOf(_cellSpace));
		_table.setAttribute("cellpadding", String.valueOf(_cellPad));
		if (_border != 0)
			_table.setAttribute("border", String.valueOf(_border));

		// Init the renderer and return
		_out = pageContext.getOut();
		return EVAL_BODY_INCLUDE;
	}
	
	@SuppressWarnings("unchecked")
	public int doEndTag() throws JspException {
		if (!_showScrollTags)
			return EVAL_PAGE;
		
    	// Get the URL parameters and the forward/backward start dates
        Map params = new HashMap<String, Object>(pageContext.getRequest().getParameterMap());
        params.keySet().removeAll(RESERVED_PARAMS);
        Date bd = CalendarUtils.adjust(_startDate, (Math.abs(_endDate.getTime() - _startDate.getTime()) / 86400000) * -1);
        Date fd = CalendarUtils.adjust(_startDate, Math.abs(_endDate.getTime() - _startDate.getTime()) / 86400000);
        
        try {
        	// Build the backward URL
        	XMLRenderer backURL = new XMLRenderer("a");
        	params.put("startDate", new String[] { _df.format(bd) } );
        	backURL.setAttribute("href", buildURL(params));

        	// Build the forward URL
        	XMLRenderer fwdURL = new XMLRenderer("a");
        	params.put("startDate", new String[] { _df.format(fd) } );
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
        	_out.print(backURL.open(true));
        	_out.print("GO BACK");
        	_out.print(backURL.close());
        	_out.print(sBackCell.close());
        	
        	// Render the middle cell
        	_out.print("<td>&nbsp;</td>");
        	
        	// Render the scroll forward cell and link
        	XMLRenderer sFwdCell = new XMLRenderer("td");
        	sFwdCell.setAttribute("class", "right");
        	sFwdCell.setAttribute("colspan", "3");
        	_out.print(sFwdCell.open(true));
        	_out.print(fwdURL.open(true));
        	_out.print("GO FORWARD");
        	_out.print(fwdURL.close());
        	_out.print(sFwdCell.close());

        	// Close the row
        	_out.println(scrollRow.close());
        } catch (IOException ie) {
        	throw new JspException(ie);
        } finally {
        	release();
        }
        
        return EVAL_PAGE;
	}

	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		super.release();
		_cellPad = SystemData.getInt("html.table.spacing", 0);
		_cellSpace = SystemData.getInt("html.table.padding", 0);
		_border = 0;
		_showDaysOfWeek = true;
		_showScrollTags = true;
		_tableClass = null;
		_topBarClass = null;
		_dayBarClass = null;
		_contentClass = null;
		_currentDateAttr = null;
		_entries.clear();
	}
}