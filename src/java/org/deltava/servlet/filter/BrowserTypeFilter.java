// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.filter;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * A servlet filter to detect the browser type.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class BrowserTypeFilter implements Filter {

    private static final Logger log = Logger.getLogger(BrowserTypeFilter.class);

    private static final int UNKNOWN = -1;
    private static final int MOZILLA = 0;
    private static final int MSIE6 = 1;
    private static final int MSIE7 = 2;
    private static final int MSIE8 = 3;

    private String _defaultCode;
    private static final String[] MOZILLA_IDENT = { "Firefox", "Gecko" };
    private static final String[] MSIE8_IDENT = { "MSIE 8.0" };
    private static final String[] MSIE7_IDENT = { "MSIE 7.0" };
    private static final String[] MSIE_IDENT = { "MSIE" };
    
    /**
     * Called by the servlet container when the filter is started. Logs a message.
     * @param cfg the Filter Configuration
     */
    public void init(FilterConfig cfg) throws ServletException {
        _defaultCode = cfg.getInitParameter("default");
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
    @SuppressWarnings("fallthrough")
    public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain fc) throws IOException, ServletException {

        // Set request attributes based on the browser type
        HttpServletRequest hreq = (HttpServletRequest) req;
        switch (getBrowserType(hreq.getHeader("User-Agent"))) {
            case MOZILLA:
                req.setAttribute("browser$mozilla", Boolean.TRUE);
                break;
                
            case MSIE8:
            	req.setAttribute("browser$ie8", Boolean.TRUE);

            case MSIE7:
            	req.setAttribute("browser$ie7", Boolean.TRUE);
                
            case MSIE6:
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
       if ((userAgent == null) && (_defaultCode != null))
          userAgent = _defaultCode;

       // Check for Gecko/Firefox
       for (int x = 0; x < MOZILLA_IDENT.length; x++) {
           if (userAgent.indexOf(MOZILLA_IDENT[x]) != -1)
               return MOZILLA;
       }
       
       // Check for Internet Explorer 8
       for (int x = 0; x < MSIE8_IDENT.length; x++) {
           if (userAgent.indexOf(MSIE8_IDENT[x]) != -1)
               return MSIE8;
       }

       // Check for Internet Explorer 7
       for (int x = 0; x < MSIE7_IDENT.length; x++) {
           if (userAgent.indexOf(MSIE7_IDENT[x]) != -1)
               return MSIE7;
       }

       // Check for Internet Explorer 5/6
       for (int x = 0; x < MSIE_IDENT.length; x++) {
           if (userAgent.indexOf(MSIE_IDENT[x]) != -1)
               return MSIE6;
       }

       return UNKNOWN;
    }
}