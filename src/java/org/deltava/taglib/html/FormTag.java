// Copyright 2005, 2006, 2007, 2008, 2010, 2012, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.*;

import org.deltava.beans.*;
import org.deltava.taglib.ContentHelper;
import org.deltava.util.system.SystemData;

/**
 * A JSP tag for generating HTML forms.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public class FormTag extends ElementTag {
	
	/*
	 * Default CSS scheme name.
	 */
	private static final String DEFAULT_SCHEME = "legacy";
    
    private int _tabIndex;
    private boolean _allowUpload;
    
    private String _scheme;
    private boolean _spinner = true;
    private String _spinnerURL;
    
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
	 * Loads the UI scheme name from the user object, if present.
	 * @param ctxt the JSP page context
	 */
    @Override
    public void setPageContext(PageContext ctxt) {
    	super.setPageContext(ctxt);
		HttpServletRequest req = (HttpServletRequest) ctxt.getRequest();
		Principal user = req.getUserPrincipal();
		if (user instanceof Person) {
			Person p = (Person) user;
			_scheme = (p.getUIScheme() == null) ? null : p.getUIScheme().toLowerCase().replace(' ', '_');
		}
    }
    
    /**
     * Opens this FORM element by writing a &gt;FORM&lt; tag.
     * @throws JspException if an I/O error occurs;  
     */
    @Override
    public int doStartTag() throws JspException {
    		super.doStartTag();
    		if (_spinner) {
    			StringBuilder buf = new StringBuilder("/").append(SystemData.get("path.img"));
        		buf.append("/spinner_");
        		buf.append((_scheme == null) ? DEFAULT_SCHEME : _scheme);
        		buf.append(".gif");
        		_spinnerURL = buf.toString();
        		ContentHelper.pushContent(pageContext, buf.substring(1), "image");
    		}
        
        // Set the ACTION URL
        try {
        	StringBuilder url = new StringBuilder(_action);
            if (_id != null) {
                url.append("?id=");
                url.append(URLEncoder.encode(_id, "UTF-8"));
            }

            if (_opName != null) {
                url.append((_id == null) ? "?" : "&amp;");
                url.append("op=");
                url.append(URLEncoder.encode(_opName, "UTF-8"));
            }
            
            _data.setAttribute("action", url.toString());
        } catch (UnsupportedEncodingException uee) {
            throw new JspException("UTF-8 encoding not supported - Laws of Universe no longer apply");
        }
        
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
    @Override
    public int doEndTag() throws JspException {
        try {
        	if (_spinner) {
        		_out.print("<div id=\"spinner\" style=\"display:none;\"><img class=\"spinImg\" src=\"");
        		_out.print(_spinnerURL);
        		_out.println("\" /></div>");
        	}
        	
            _out.println(_data.close());
        } catch(Exception e) {
            throw new JspException(e);
        } finally {
        	release();	
        }
        
        return EVAL_PAGE;
    }
    
    /**
     * Increments the current tab index, then returns the new value.
     * @return the new tab Index
     */
    int incTabIndex() {
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
     * Returns whether RFC1867 file uploads are permitted within this form.
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
     * Sets whether to render the hidden spinner DIV.
     * @param doSpinner TRUE if the spinner DIV should be rendered, otherwise FALSE
     */
    public void setSpinner(boolean doSpinner) {
    	_spinner = doSpinner;
    }
    
    /**
     * Releases the tag's state variables.
     */
    @Override
    public void release() {
        super.release();
        _id = null;
        _opName = null;
        _tabIndex = 0;
        _spinner = true;
    }
}