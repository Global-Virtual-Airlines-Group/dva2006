// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import java.time.Duration;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to create a duration object from a time interval in seconds.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */
public class DurationTag extends TagSupport {

	private String _varName;
	private long _durationSeconds;

	/**
	 * Sets the request attribute name.
	 * @param vName the name of the request attribute to store the data in
	 */
	public void setVar(String vName) {
		_varName = vName;
	}

	/**
	 * Sets the length of the Duration to create.
	 * @param sec the length in seconds
	 */
	public void setLength(long sec) {
		_durationSeconds = sec;
	}
	
	@Override
	public int doEndTag() {
		pageContext.setAttribute(_varName, Duration.ofSeconds(_durationSeconds), PageContext.PAGE_SCOPE);
		return EVAL_PAGE;
	}
}