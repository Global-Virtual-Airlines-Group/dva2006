package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

/**
 * A JSP tag to render buttons that execute web site commands.
 * @author Luke
 * @version 1.0
 * @since 1.0
 * Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
 */

public class CommandButtonTag extends ButtonTag {

    private String _cmdName;
    private String _id;
    private String _opName;
    
    private boolean _doPost;
    
    /**
     * Create a new CommandButton element tag.
     */
    public CommandButtonTag() {
        super();
    }
    
    /**
     * Releases state and readies the tag for another invocation.
     */
    public void release() {
        super.release();
        _doPost = false;
    }
    
    /**
     * Sets the ID parameter for the command invocation. If it starts with &quot;0x&quot; then turn the rest
     * of the string into a hexadecimal number string.
     * @param id the parameter
     * @see org.deltava.commands.CommandContext#getCmdParameter(int, Object)
     */
    public void setLinkID(String id) {

        if (id.startsWith("0x")) {
            try {
                _id = "0x" + Integer.toString(Integer.parseInt(id.substring(2)), 16).toUpperCase();
            } catch (NumberFormatException nfe) {
                _id = id;
            }
        } else {
            _id = id;
        }
    }
    
    /**
     * Sets the command name for the button, <i>without extension</i>.
     * @param url the command name
     */
    public void setUrl(String url) {
    	_cmdName = url.toLowerCase() + ".do";
    }
    
    /**
     * Sets the command operation for this button.
     * @param opName the operation name
     * @see org.deltava.commands.CommandContext#getCmdParameter(int, Object)
     */
    public void setOp(String opName) {
        _opName = opName;
    }
    
    /**
     * Sets the button to POST or GET to the form.
     * @param doPost TRUE if POSTing, FALSE if GETting
     */
    public void setPost(boolean doPost) {
        _doPost = doPost;
    }
    
    /**
     * Overrides the type property from the superclass. <i>NOT IMPLEMENTED</i>.
     * @throws UnsupportedOperationException
     */
    public final void setType(String btnType) {
    	throw new UnsupportedOperationException();
    }
    
    /**
     * Overrides the onClick property from the superclass. <i>NOT IMPLEMENTED</i>.
     * @throws UnsupportedOperationException
     */
    public final void setOnClick(String js) {
    	throw new UnsupportedOperationException();
    }
    
    /**
     * Overrides the className property from the superclass. <i>NOT IMPLEMENTED</i>.
     * @throws UnsupportedOperationException
     */
    public final void setClassName(String className) {
    	throw new UnsupportedOperationException();
    }
    
    /**
     * Renders the tag. Sets the onClick property and calls the superclass renderer.
     * @return TagSupport.EVAL_PAGE
     * @throws JspException if an error occurs
     * @throws IllegalStateException if common.js has not been added to the request
     */
    public int doEndTag() throws JspException {
        
    	// Ensure that the common JS file has been included
    	if (!containsContent("JS", "common"))
    		throw new IllegalStateException("common.js not included in request");
    	
    	// Render the entire command string
        StringBuffer url = new StringBuffer(_cmdName);
        if (_id != null) {
            url.append("?id=");
            url.append(_id);
        }

        if (_opName != null) {
            url.append((_id == null) ? "?" : "&amp;");
            url.append("op=");
            url.append(_opName);
        }
        
        // Sets the JavaScript get/post function
        String jsFuncName = (_doPost) ? "cmdPost" : "cmdGet";
        super.setOnClick("void " + jsFuncName + "(\'" + url.toString() + "\')");
        super.setClassName("cmdButton");
        
        // Calls the superclass renderer
    	return super.doEndTag();
    }
}