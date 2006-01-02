// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
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
	
	public int doStartTag() throws JspException {

		// Get the parent tag
		CalendarEntryTag parent = (CalendarEntryTag) TagSupport.findAncestorWithClass(this, CalendarEntryTag.class);
		if (parent == null)
			throw new JspException("Must be contained within a CalendarEntryTag");

		// If we have more entries, render the code
		return parent.hasMoreEntries() ? EVAL_BODY_INCLUDE : SKIP_BODY;
	}
}