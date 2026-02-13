// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.filter;

import java.io.IOException;
import java.time.Instant;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.apache.logging.log4j.*;

import org.deltava.util.system.SystemData;

/**
 * A servlet filter to track request latency between the front-end web server and the servlet container.
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public class RequestLatencyFilter extends HttpFilter {
	
	private static final Logger log = LogManager.getLogger(RequestLatencyFilter.class);
	
	@Override
	public void init(FilterConfig cfg) throws ServletException {
		log.info("Started");
	}
	
	/**
	 * Called by the servlet container on each request. Tracks request queue latency from Apache HTTPD to Tomcat.
	 * @param req the request
	 * @param rsp the response
	 * @param fc the Filter Chain
	 * @throws ServletException if a general error occurs
	 */
	@Override
	public void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain fc) throws IOException, ServletException {
		
		// Calculate request queue time
		String reqStartHdr = req.getHeader("X-Request-Start");
		if (reqStartHdr != null) {
			long now = System.currentTimeMillis();
			try {
				long st = Long.parseLong(reqStartHdr, 2, reqStartHdr.length(), 10);
				long us = (now * 1000) - st;
				if (us > 100_000) {
					Instant reqTime = Instant.ofEpochMilli(st / 1000);
					double ms = us / 1000d;
					log.warn("{} long Request queue time - {}ms ({}) (@ {})", SystemData.get("airline.code"), String.format("%.2f", Double.valueOf(ms)), req.getRemoteAddr(), reqTime);
					rsp.addIntHeader("X-Queue-Time", (int)ms);
				}
			} catch (NumberFormatException nfe) {
				log.warn("Error parsing requst start time header {}", reqStartHdr);
			}
		}
		
		fc.doFilter(req, rsp);
	}
}