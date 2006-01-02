// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib.calendar;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to define HTML content to display in an empty Calendar row.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CalendarEntryEmptyTag extends TagSupport {

	public int doStartTag() throws JspException {
		
		// Get the parent tag
		CalendarTag parent = (CalendarTag) TagSupport.findAncestorWithClass(this, CalendarTag.class);
		if (parent == null)
			throw new JspException("Must be contained within a CalendarTag");
		
		// Display only if parent has no entries for today's date
		return parent.getCurrentEntries().isEmpty() ? EVAL_BODY_INCLUDE : SKIP_BODY;
	}
}