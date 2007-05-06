// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
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

	/**
	 * Displays the body if no entries for the current date.
	 * @return TagSupport#EVAL_BODY_INCLUDE if no entries, otherwise TagSupport#SKIP_BODY
	 * @throws JspException if not included in a {@link CalendarEntryTag}
	 * @see CalendarTag#getCurrentEntries()
	 */
	public int doStartTag() throws JspException {
		
		// Get the parent tag
		CalendarTag parent = (CalendarTag) TagSupport.findAncestorWithClass(this, CalendarTag.class);
		if (parent == null)
			throw new JspException("Must be contained within a CalendarTag");
		
		// Display only if parent has no entries for today's date
		return parent.getCurrentEntries().isEmpty() ? EVAL_BODY_INCLUDE : SKIP_BODY;
	}
}