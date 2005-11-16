// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.servlet.filter;

import java.util.*;
import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

/**
 * A servlet filter to implement GZIP compression.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CompressionFilter implements Filter {
	
	private static final String GZIP_REQ_ATTR = "$isGZIP";
	private static final Logger log = Logger.getLogger(CompressionFilter.class);
	
	private int _bufferSize;

    /**
     * Called by the servlet container when the filter is started.
     * @param cfg the Filter Configuration
     */
	public void init(FilterConfig cfg) throws ServletException {

		// Get initialization parameter
		try {
			_bufferSize = Integer.parseInt(cfg.getInitParameter("bufferSize")); 
		} catch (Exception e) {
			_bufferSize = 0;
		}
		
		log.info("Started");
	}

    /**
     * Called by the servlet container on each request. Saves the browser type in the request.
     * @param req the Servlet Request
     * @param rsp the Servlet Response
     * @param fc the Filter Chain
     * @throws IOException if an I/O error occurs
     * @throws ServletException if a general error occurs
     */
	public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain fc) throws IOException, ServletException {

		// Check if we compress
		boolean doCompress = false;
		if (_bufferSize != 0) {
			HttpServletRequest hreq = (HttpServletRequest) req;
			List hdrs = Collections.list(hreq.getHeaders("Accept-Encoding"));
			for (Iterator i = hdrs.iterator(); i.hasNext() && (!doCompress); ) {
				String hdr = (String) i.next();
				doCompress = hdr.contains("gzip");
			}
			
			log.debug("GZIP Compression = " + doCompress);
		} else {
			log.debug("Compresson Disabled");
		}

		// Act on compression setting
		if (doCompress && (!(rsp instanceof GZIPResponseWrapper))) {
			HttpServletResponseWrapper rspWrap = new GZIPResponseWrapper((HttpServletResponse) rsp);
			req.setAttribute(GZIP_REQ_ATTR, Boolean.TRUE);
			try {
				fc.doFilter(req, rspWrap);
			} finally {
				rspWrap.flushBuffer();
			}
		} else {
			fc.doFilter(req, rsp);
		}
	}

	/**
     * Called by the servlet container when the filter is stopped. Logs a message.
     */
	public void destroy() {
		log.info("Stopped");
	}
}