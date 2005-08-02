package org.deltava.servlet.filter;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * A servlet filter to detect the browser type.
 * @author Luke
 * @version 1.0
 * @since 1.0 Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
 */

public class BrowserTypeFilter implements Filter {

    private static final Logger log = Logger.getLogger(BrowserTypeFilter.class);

    private static final int UNKNOWN = -1;
    private static final int MOZILLA = 0;
    private static final int MSIE = 1;

    private static final String[] MOZILLA_IDENT = { "Firefox", "Gecko" };
    private static final String[] MSIE_IDENT = { "MSIE" };

    /**
     * Called by the servlet container when the filter is started. Logs a message.
     * @param cfg the Filter Configuration
     */
    public void init(FilterConfig cfg) throws ServletException {
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

        // Get the browser's name
        HttpServletRequest hreq = (HttpServletRequest) req;
        String userAgent = hreq.getHeader("User-Agent");

        // Set request attributes based on the browser type
        switch (getBrowserType(userAgent)) {
            case MOZILLA:
                req.setAttribute("browser$mozilla", Boolean.TRUE);
                break;

            case MSIE:
            default:
                req.setAttribute("browser$ie", Boolean.TRUE);
        }

        // Execute the next filter in the chain
        fc.doFilter(req, rsp);
    }

    /**
     * Called by the servlet container when the filter is stopped. Logs a message.
     */
    public void destroy() {
        log.info("Stopped");
    }

    /**
     * Helper method to search the ident strings and return the browser type.
     */
    private int getBrowserType(String userAgent) {
       if (userAgent == null)
          return UNKNOWN;

        // Check for Gecko/Firefox
        for (int x = 0; x < MOZILLA_IDENT.length; x++) {
            if (userAgent.indexOf(MOZILLA_IDENT[x]) != -1)
                return MOZILLA;
        }

        // Check for Internet Explorer
        for (int x = 0; x < MSIE_IDENT.length; x++) {
            if (userAgent.indexOf(MSIE_IDENT[x]) != -1)
                return MSIE;
        }

        // If we got this far, no clue on the browser
        return UNKNOWN;
    }
}