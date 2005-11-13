// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.taglib;

import javax.servlet.jsp.tagext.TagSupport;

/**
 * An abstract JSP tag class to support browser detection functions.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class BrowserDetectingTag extends TagSupport {

    /**
     * Detects if the browser is Microsoft Internet Explorer.
     * @return TRUE if the browser is Internet Explorer, otherwise FALSE
     * @see org.deltava.servlet.filter.BrowserTypeFilter
     */
    protected boolean isIE() {
        return (pageContext.getRequest().getAttribute("browser$ie") != null);
    }
    
    /**
     * Detects if the browser is Mozilla Firefox.
     * @return TRUE if the browser is Firefox, otherwise FALSE
     * @see org.deltava.servlet.filter.BrowserTypeFilter
     */
    protected boolean isFirefox() {
        return (pageContext.getRequest().getAttribute("browser$mozilla") != null);
    }
}