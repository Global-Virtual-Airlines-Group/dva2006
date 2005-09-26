package org.deltava.taglib.content;

import javax.servlet.jsp.tagext.TagSupport;

/**
 * An abstract class for content insertion JSP tags.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class InsertContentTag extends TagSupport {
	
    protected boolean _forceInclude;
    
    /**
     * The name of the resource to display, <i>without extension</i>.
     */
    protected String _resourceName;
    
    /**
     * Update the name of the resource to insert.
     * @param name the name of the resource, without extension
     */
    public void setName(String name) {
        _resourceName = name;
    }
    
    /**
     * Toggles if this content shall been included, even if already included previously in the request.
     * @param doForce TRUE if the content should be included
     */
    public void setForce(boolean doForce) {
        _forceInclude = doForce;
    }
    
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

    /**
     * Releases the tag's state variables.
     */
    public void release() {
       super.release();
       _forceInclude = false;
    }
}