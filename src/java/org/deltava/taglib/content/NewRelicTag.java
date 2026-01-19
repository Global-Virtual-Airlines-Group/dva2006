// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import jakarta.servlet.jsp.JspException;

import com.newrelic.api.agent.NewRelic;

import org.deltava.beans.system.ContentSecurity;

import org.deltava.taglib.*;

import org.deltava.util.StringUtils;

/**
 * A JSP tag to inject NewRelic browser instrumentation with a CSP nonce.
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public class NewRelicTag extends CSPNonceTag {

	@Override
	public int doEndTag() throws JspException {

		// Get the data with the nonce
		String src = NewRelic.getBrowserTimingHeader(getNonce());
		if (StringUtils.isEmpty(src))
			return EVAL_BODY_INCLUDE;
		
		try {
			pageContext.getOut().println(src);
		} catch (Exception e) {
			throw new JspException(e);
		}
		
		// Update CSP policy
		ContentHelper.addCSP(pageContext, ContentSecurity.SCRIPT, "js-agent.newrelic.com");
		ContentHelper.addCSP(pageContext, ContentSecurity.CONNECT, "bam.nr-data.net");
		ContentHelper.addCSP(pageContext, ContentSecurity.CONNECT, "bam-cell.nr-data.net"); 
		return EVAL_BODY_INCLUDE;
	}
}