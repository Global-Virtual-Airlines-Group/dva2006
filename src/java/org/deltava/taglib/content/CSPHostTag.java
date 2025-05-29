// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.system.ContentSecurity;

import org.deltava.taglib.ContentHelper;
import org.deltava.util.EnumUtils;

/**
 * A JSP tag to add a CSP host name. This is a very thin wrapper around the method in {@link ContentHelper}
 * @author Luke
 * @version 12.0
 * @since 12.0
 * @see ContentHelper#addCSP(PageContext, ContentSecurity, String[])
 */

public class CSPHostTag extends TagSupport {
	
	private ContentSecurity _type;
	private String _host;

	/**
	 * Sets the content security policy type.
	 * @param csType a ContentSecurity enum name
	 */
	public void setType(String csType) {
		ContentSecurity cs = EnumUtils.parse(ContentSecurity.class, csType, null);
		if (cs != null)
			_type = cs;
	}
	
	/**
	 * Sets the host name.
	 * @param host the host name
	 */
	public void setHost(String host) {
		_host = host;
	}
	
	@Override
	public int doEndTag() throws JspException {
		ContentHelper.addCSP(pageContext, _type, _host);
		return EVAL_PAGE;
	}
}