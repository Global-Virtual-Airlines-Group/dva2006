// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.calendar;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to generate spacers between Calendar entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CalendarEntrySpacerTag extends TagSupport {
	
	/**
	 * Renders the spacer entry if the parent tag has more entries for the current day.
	 * @return TagSupport#EVAL_BODY_INCLUDE if more entries, otherwise TagSupport#SKIP_BODY
	 * @throws JspException if not included in a {@link CalendarEntryTag}
	 * @see CalendarEntryTag#hasMoreEntries()
	 */
	public int doStartTag() throws JspException {

		// Get the parent tag
		CalendarEntryTag parent = (CalendarEntryTag) TagSupport.findAncestorWithClass(this, CalendarEntryTag.class);
		if (parent == null)
			throw new JspException("Must be contained within a CalendarEntryTag");

		// If we have more entries, render the code
		return parent.hasMoreEntries() ? EVAL_BODY_INCLUDE : SKIP_BODY;
	}
}