// Copyright 2005, 2007, 2010, 2012, 2014, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

import org.deltava.util.StringUtils;

/**
 * A JSP tag to generate an HTML link.
 * @author Luke
 * @version 7.4
 * @since 1.0
 */

public class LinkTag extends ElementTag {
	
	private String _anchor;
	
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
    @Override
    public int doStartTag() throws JspException {
    	super.doStartTag();
        try {
            validateLink();
            if (!StringUtils.isEmpty(_anchor))
            	_data.setAttribute("href", _data.get("href") + "#" + _anchor);
            
            _out.print(_data.open(true));
        } catch(Exception e) {
            throw new JspException(e);
        }

        return EVAL_BODY_INCLUDE;
    }
    
    /**
     * Closes this link element by writing an &gt;/A&lt; tag.
     * @throws JspException if an I/O error occurs
     */
    @Override
    public int doEndTag() throws JspException {
        try {
            _out.print(_data.close());
        } catch(Exception e) {
            throw new JspException(e);
        } finally {
        	release();	
        }
        
        return EVAL_PAGE;
    }
    
    /**
     * Sets the target URL for this link.
     * @param url the target URL
     */
    public void setUrl(String url) {
        _data.setAttribute("href", url);
    }
    
    /**
     * Sets the JavaScript onClick event for this link.
     * @param js the JavaScript code to execute when this link is clicked
     */
    public void setOnClick(String js) {
        _data.setAttribute("onclick", js);
    }
    
    /**
     * Sets the anchor for this link.
     * @param a the anchor
     */
    public void setAnchor(String a) {
    	_anchor = a;
    }
    
    /**
     * Sets the label for this link.
     * @param label the link label
     */
    public void setLabel(String label) {
    	_data.setAttribute("title", label);
    }
    
    /**
     * Sets the target frame for this link.
     * @param targetFrame the target frame name
     */
    public void setTarget(String targetFrame) {
        _data.setAttribute("target", targetFrame);
    }
    
    /**
     * Marks this link as an external link.
     * @param isExternal TRUE if the link is external, otherwise FALSE
     */
    public void setExternal(boolean isExternal) {
    	if (isExternal)
    		_data.setAttribute("rel", "external");
    }
    
    /**
     * Validates the tag to ensure a URL/onClick event has been set.
     * @throws IllegalStateException if neither a URL nor onClick are present
     */
    protected void validateLink() throws IllegalStateException {
        boolean isOK = (_data.has("href") || _data.has("onclick"));
        if (!isOK)
            throw new IllegalStateException("href or onClick must be set");
    }
    
    /**
     * Releases the tag's state variables.
     */
    @Override
    public void release() {
    	super.release();
    	_anchor = null;
    }
}