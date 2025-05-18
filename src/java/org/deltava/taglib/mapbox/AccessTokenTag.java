// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.mapbox;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.taglib.ContentHelper;
import org.deltava.util.system.SystemData;

/**
 * A JSP tag to insert a Mapbox API access token. 
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

public class AccessTokenTag extends TagSupport {

	@Override
	public int doStartTag() throws JspException {
		super.doStartTag();
		
		// Check that the source variable has been defined
		if (!ContentHelper.containsContent(pageContext, "JS", InsertAPITag.API_JS_NAME))
			throw new IllegalStateException("MapBox API not defined in JavaScript");
		
		return SKIP_BODY;
	}
	
	@Override
	public int doEndTag() throws JspException {
		
		JspWriter out = pageContext.getOut();
		try {
			out.print("mapboxgl.accessToken = '");
			out.print(SystemData.get("security.key.mapbox"));
			out.print("';");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}