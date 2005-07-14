package org.deltava.taglib.content;

import java.util.Set;
import java.util.HashSet;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * An abstract class for content insertion JSP tags.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class InsertContentTag extends TagSupport {
	
    private boolean _forceInclude;
    
	/**
	 * The request attribute to store content resource names in. This variable is exposed since it
	 * is also referenced by containsContent() in {@link org.deltava.taglib.html.ElementTag }.
	 */
	public static final String CONTENT_MAP_ATTR = "$TagContentNames$";

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
     * Updates a request-located map of all inserted content files.
     * @param contentType the type of content (css/js)
     * @param contentName the name of the inserted content
     * @see InsertContentTag#containsContent(String, String)
     */
    protected void addContent(String contentType, String contentName) {
    	
    	// Find the content name map
    	Set content = (Set) pageContext.findAttribute(CONTENT_MAP_ATTR);
    	if (content == null) {
    		content = new HashSet();
    		pageContext.getRequest().setAttribute(CONTENT_MAP_ATTR, content);
    	}
    	
    	// Add the resource to the content name map
    	content.add(contentType + "$" + contentName);
    }
    
    /**
     * Determines if a particular content file has been inserted during this request invocation.
     * @param contentType the type of content (css/js)
     * @param contentName the name of the content
     * @return TRUE if the content has been added, otherwise FALSE
     * @see InsertContentTag#addContent(String, String)
     */
    protected boolean containsContent(String contentType, String contentName) {

    	// Find the content name map
    	Set content = (Set) pageContext.findAttribute(CONTENT_MAP_ATTR);
    	if ((content == null) || _forceInclude)
    		return false;
    	
    	return content.contains(contentType + "$" + contentName);
    }
    
    /**
     * Wraps an exception within a Tag into a JspException. This method sets the cause and copies 
     * the stack trace.
     * @param t the exception to wrap
     * @return a JspException containing the original exception
     */
    protected JspException wrap(Throwable t) {
        JspException je = new JspException("Error writing " + getClass().getName());
        je.initCause(t);
        je.setStackTrace(t.getStackTrace());
        return je;
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
}