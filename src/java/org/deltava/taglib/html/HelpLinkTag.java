// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import org.deltava.taglib.ContentHelper;

/**
 * A JSP tag to render a Help popup URL.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class HelpLinkTag extends LinkTag {

	/**
	 * Sets the help entry name.
	 * @param entryName the entry name
	 */
	public void setEntry(String entryName) {
		setUrl("javascript:openHelp(\'" + entryName + "\')");
	}

	/**
	 * Renders the start of the JSP tag, and checks that common.js has been included.
	 * @return EVAL_BODY_INCLUDE always
	 */
	public int doStartTag() {

		// Ensure that the common JS file has been included
		if (!ContentHelper.containsContent(pageContext, "JS", "common"))
			throw new IllegalStateException("common.js not included in request");

		return EVAL_BODY_INCLUDE;
	}
}