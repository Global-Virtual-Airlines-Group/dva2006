// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.io.IOException;

import jakarta.servlet.http.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.system.VersionInfo;

import org.deltava.util.*;

/**
 * A link shortener servlet that hands out redirects to other URLs. This currently only supports flight reports but can be extended for other uses. 
 * @author Luke
 * @version 12.1
 * @since 12.1
 */

public class LinkServlet extends GenericServlet {
	
	private static final Logger log = LogManager.getLogger(LinkServlet.class);

	@Override
	public String getServletInfo() {
		return "Link Shortener Servlet " + VersionInfo.TXT_COPYRIGHT;
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
		
		// Get the link ID
		URLParser url = new URLParser(req.getRequestURI()); int id = 0;
		try {
			id = StringUtils.parseHex("0x" + url.getName());
		} catch (Exception e) {
			log.warn("Error parsing ID {} - {}", url.getName(), e.getClass().getName());
			rsp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		// Redirect to the PIREP
		if (!"fr".equals(url.getLastPath())) {
			log.warn("Unknown short link - {}", req.getRequestURI());
			rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		String link = String.format("/pirep.do?id=0x%s", Integer.toHexString(id));
		log.info("Redirecting {} to {}", req.getRequestURI(), link);
		rsp.sendRedirect(link);
	}
}