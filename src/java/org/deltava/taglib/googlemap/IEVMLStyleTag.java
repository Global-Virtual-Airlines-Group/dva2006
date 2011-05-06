// Copyright 2005, 2007, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import javax.servlet.jsp.*;

import org.deltava.beans.system.*;
import org.deltava.taglib.BrowserInfoTag;

/**
 * A JSP tag to render the Internet Explorer VML behavior style for Google Maps.
 * @author Luke
 * @version 3.7
 * @since 1.0
 */

public class IEVMLStyleTag extends BrowserInfoTag {

	public int doEndTag() throws JspException {

		// Do nothing if not IE
		HTTPContextData bctxt = getBrowserContext();
		if ((bctxt == null) || (bctxt.getBrowserType() != BrowserType.IE))
			return EVAL_PAGE;

		try {
			JspWriter out = pageContext.getOut();
			out.println("<style type=\"text/css\">");
			out.println("v\\:* {");
			out.println("\tbehavior:url(#default#VML);");
			out.println('}');
			out.println("</style>");
		} catch (Exception e) {
			throw new JspException(e);
		}

		return EVAL_PAGE;
	}
}