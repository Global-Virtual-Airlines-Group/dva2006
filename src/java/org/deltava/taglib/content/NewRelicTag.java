// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import jakarta.servlet.jsp.JspException;

import com.newrelic.api.agent.NewRelic;

import org.deltava.beans.system.ContentSecurity;

import org.deltava.taglib.*;

import org.deltava.util.StringUtils;

/**
 * A JSP tag to inject NewRelic browser instrumentation with a CSP nonce. Because this injected JavaScript can easily overflow the JSP output
 * buffer (which prevents the CSP header from being emitted) this tag has a start and end. The start sets the relevant CSP headers, while the
 * end tag actually emits the script (with the expectation that the CSP header has already been written to the response).
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public class NewRelicTag extends CSPNonceTag {
	
	private String _src;
	private boolean _hasJS;
	
	@Override
	public int doStartTag() throws JspException {
		_src = NewRelic.getBrowserTimingHeader(getNonce());
		_hasJS = !StringUtils.isEmpty(_src);
		
		// Update CSP policy
		if (_hasJS) {
			ContentHelper.addCSP(pageContext, ContentSecurity.SCRIPT, "js-agent.newrelic.com");
			ContentHelper.addCSP(pageContext, ContentSecurity.CONNECT, "bam.nr-data.net");
			ContentHelper.addCSP(pageContext, ContentSecurity.CONNECT, "bam-cell.nr-data.net");
		}
		
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() throws JspException {
		try {
			if (_hasJS) pageContext.getOut().println(_src);
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		return EVAL_PAGE;
	}
}