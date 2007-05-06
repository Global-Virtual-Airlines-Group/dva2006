// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.calendar;

import java.util.*;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.deltava.beans.CalendarEntry;

/**
 * A JSP tag to save Calendar entries to the request context for rendering to a JSP.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CalendarEntryTag extends TagSupport implements IterationTag {

	private String _attrName;
	private Iterator<CalendarEntry> _entries;
	
	/**
	 * Sets the name of the request attribute to save the Calendar Entry into.
	 * @param attrName the request attribute name
	 */
	public void setName(String attrName) {
		_attrName = attrName;
	}
	
	/**
	 * Returns whether there are further Calendar entries to display.
	 * @return TRUE if there are more entries, otherwise FALSE
	 */
	boolean hasMoreEntries() {
		return _entries.hasNext();
	}
	
	/**
	 * Renders the entries for the current date and saves the current entry into the request.
	 * @return TagSupport#SKIP_BODY if no entries for today, otherwise TagSupport#EVAL_BODY_INCLUDE
	 * @throws JspException if not contained within a {@link CalendarTag}
	 */
	public int doStartTag() throws JspException {
		
		// Get the parent tag
		CalendarTag parent = (CalendarTag) TagSupport.findAncestorWithClass(this, CalendarTag.class);
		if (parent == null)
			throw new JspException("Must be contained within a CalendarTag");
		
		// Get the entries for today and save in the request
		_entries = parent.getCurrentEntries().iterator();
		if (!_entries.hasNext())
			return SKIP_BODY;
		
		// Get the current entry and include the body
		CalendarEntry entry = _entries.next();
		pageContext.setAttribute(_attrName, entry, PageContext.REQUEST_SCOPE);
		return EVAL_BODY_INCLUDE;
	}
	
	/**
	 * Checks if there are further entries for the current date, and saves the next entry into the request.
	 * @return TagSupport#SKIP_BODY if no more entries for today, otherwise TagSupport#EVAL_BODY_INCLUDE
	 * @throws JspException never
	 */
	public int doAfterBody() throws JspException {
		if (!_entries.hasNext())
			return SKIP_BODY;
		
		// Save the entry and re-iterate
		CalendarEntry entry = _entries.next();
		pageContext.setAttribute(_attrName, entry, PageContext.REQUEST_SCOPE);
		return EVAL_BODY_AGAIN;
	}
}