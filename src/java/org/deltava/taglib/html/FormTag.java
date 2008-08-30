// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.jsp.JspException;

import org.deltava.beans.DatabaseBean;

/**
 * A JSP tag for generating HTML forms.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class FormTag extends ElementTag {
    
    private int _tabIndex;
    private boolean _allowUpload;
    
    private String _id;
    private String _opName;
    private String _action;
    
    /**
     * Creates a new Form element Tag.
     */
    public FormTag() {
        super("form");
    }

    /**
     * Opens this FORM element by writing a &gt;FORM&lt; tag. This will also save the tag into the
     * page Context as thisForm (so that the JSP EL can return it as ${thisForm}) so that child elements
     * can get the tab index as ${thisForm.incTabIndex}.
     * @throws JspException if an I/O error occurs;  
     */
    public int doStartTag() throws JspException {
        
        // Sets the tab index and marks this as a parent tag
        _tabIndex = 0;
        pageContext.setAttribute("thisForm", this);
        
        // Set the ACTION URL
        StringBuilder url = new StringBuilder(_action);
        try {
            if (_id != null) {
                url.append("?id=");
                url.append(URLEncoder.encode(_id, "UTF-8"));
            }

            if (_opName != null) {
                url.append((_id == null) ? "?" : "&amp;");
                url.append("op=");
                url.append(URLEncoder.encode(_opName, "UTF-8"));
            }
        } catch (UnsupportedEncodingException uee) {
            throw new JspException("UTF-8 encoding not supported - Laws of Universe no longer apply");
        }
        
        // Update the ACTION
        _data.setAttribute("action", url.toString());
        
        // Update the encoding type if uploads are permitted
        if (_allowUpload)
            _data.setAttribute("enctype", "multipart/form-data");
        
        try {
            _out.println(_data.open(true));
        } catch(Exception e) {
            throw new JspException(e);
        }

        return EVAL_BODY_INCLUDE;
    }
    
    /**
     * Closes this FORM element by writing a &gt;/FORM&lt; tag.
     * @throws JspException if an I/O error occurs
     */
    public int doEndTag() throws JspException {
        try {
            _out.println(_data.close());
        } catch(Exception e) {
            throw new JspException(e);
        }
        
        release();
        return EVAL_PAGE;
    }
    
    /**
     * Increments the current tab index, then returns the new value.
     * @return the new tab Index
     */
    public int incTabIndex() {
        return ++_tabIndex;
    }

    /**
     * Returns the current tab index of this form.
     * @return the current tab Index
     */
    public int getTabIndex() {
        return _tabIndex;
    }
    
    /**
     * Returns wether RFC1867 file uploads are permitted within this form.
     * @return TRUE if file uploads are permitted, otherwise FALSE 
     */
    public boolean getAllowUpload() {
        return _allowUpload;
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
        } else if (!"".equals(id))
            _id = id;
    }
    
	/**
	 * Sets the database ID to link to.
	 * @param db a {@link DatabaseBean} with the proper database ID
	 */
	public void setLink(DatabaseBean db) {
		if (db != null)
			_id = db.getHexID();
	}
    
    /**
     * Sets the command operation for this button.
     * @param opName the operation name
     * @see org.deltava.commands.CommandContext#getCmdParameter(int, Object)
     */
    public void setOp(String opName) {
    	if (!"".equals(opName))
    		_opName = opName;
    }
    
    /**
     * Sets the action type of this form.
     * @param postType the action type, typically GET or POST 
     */
    public void setMethod(String postType) {
        _data.setAttribute("method", postType.toLowerCase());
    }
    
    /**
     * Sets this form's JavaScript validation routine.
     * @param jsFunc JavaScript code to be passed as the onSubmit attribute
     */
    public void setValidate(String jsFunc) {
        _data.setAttribute("onsubmit", jsFunc);
    }
    
    /**
     * Sets the form target.
     * @param target the target frame name
     */
    public void setTarget(String target) {
    	_data.setAttribute("target", target);
    }
    
    /**
     * Sets the action URL of this form.
     * @param url the URL where this form data will be sent to on submit
     */
    public void setAction(String url) {
        _action = url;
    }
    
    /**
     * Sets if RFC1867 file uploads are permitted within this form. 
     * @param allowUpload TRUE if file uploads are permitted, otherwise FALSE
     */
    public void setAllowUpload(boolean allowUpload) {
        _allowUpload = allowUpload;
    }
    
    /**
     * Releases the tag's state variables.
     */
    public void release() {
        super.release();
        _id = null;
        _opName = null;
    }
}