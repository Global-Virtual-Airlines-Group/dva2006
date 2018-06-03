// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.Pilot;

/**
 * A JSP tag to display Pilot Names, hiding GDPR-forgotten user names. 
 * @author Luke
 * @version 8.3
 * @since 8.3
 */

public class PilotNameTag extends TagSupport {

	private Pilot _p;

	/**
	 * Sets the Pilot.
	 * @param p the Pilot
	 */
	public void setPilot(Pilot p) {
		_p = p;
	}
	
	/**
	 * Determines whether the enclosed content should be rendered to the JSP output stream.
	 * @return TagSupport.EVAL_BODY_INCLUDE or TagSupport.SKIP_BODY
	 */
	@Override
	public int doStartTag() {
		return ((_p != null) && _p.getIsForgotten()) ? SKIP_BODY : EVAL_BODY_INCLUDE;
	}
}