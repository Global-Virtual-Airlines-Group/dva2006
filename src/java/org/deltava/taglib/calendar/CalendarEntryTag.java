// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
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
	
	public void setEntry(String attrName) {
		_attrName = attrName;
	}
	
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
	
	public int doAfterBody() throws JspException {
		if (!_entries.hasNext())
			return SKIP_BODY;
		
		// Save the entry and re-iterate
		CalendarEntry entry = _entries.next();
		pageContext.setAttribute(_attrName, entry, PageContext.REQUEST_SCOPE);
		return EVAL_BODY_AGAIN;
	}
}