package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

/**
 * A JSP tag to generate an HTML link.
 * 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class LinkTag extends ElementTag {

    /**
     * Generates a new link tag.
     */
    public LinkTag() {
        super("a");
    }

    /**
     * Opens this link element by writing an &gt;A&lt; tag.
     * @throws JspException if an error occurs; 
     */
    public int doStartTag() throws JspException {
        try {
            validateLink();
            _out.print(openHTML(true));
        } catch(Exception e) {
            throw wrap(e);
        }

        return EVAL_BODY_INCLUDE;
    }
    
    /**
     * Closes this link element by writing an &gt;/A&lt; tag.
     * @throws JspException if an I/O error occurs
     */
    public int doEndTag() throws JspException {
        try {
            _out.print(closeHTML());
        } catch(Exception e) {
            throw wrap(e);
        }
        
        return EVAL_PAGE;
    }
    
    /**
     * Sets the target URL for this link.
     * @param url the target URL
     */
    public void setUrl(String url) {
        _attrs.put("href", url);
    }
    
    /**
     * Sets the JavaScript onClick event for this link.
     * @param js the JavaScript code to execute when this link is clicked
     */
    public void setOnClick(String js) {
        _attrs.put("onclick", js);
    }
    
    /**
     * Sets the label for this link. This causes window.status to be set in the <b>onMouseOver</b> event,
     * and then cleared on the <b>onMouseOut</b> event.
     * @param label
     */
    public void setLabel(String label) {
        _attrs.put("onmouseover", "window.status=\'" + label + "\';");
        _attrs.put("onmouseout", "window.status=\'\';");
    }
    
    /**
     * Sets the target frame for this link.
     * @param targetFrame the target frame name
     */
    public void setTarget(String targetFrame) {
        _attrs.put("target", targetFrame);
    }
    
    /**
     * Validates the tag to ensure a URL/onClick event has been set.
     * @throws IllegalStateException if neither a URL nor onClick are present
     */
    protected void validateLink() throws IllegalStateException {
        boolean isOK = (_attrs.containsKey("href") || _attrs.containsKey("onclick"));
        if (!isOK)
            throw new IllegalStateException("HREF or onClick must be set");
    }
}