// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import java.util.*;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to stuff data from the SystemData singleton into the request.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class GetSystemDataTag extends TagSupport {

	private String _varName;
	private String _pName;
	private Object _defaultValue;
	
	private boolean _doMapToList;
	private boolean _doSort;
	
	/**
	 * Sets the request attribute name.
	 * @param vName the name of the request attribute to store the data in
	 */
	public void setVar(String vName) {
		_varName = vName;
	}
	
	/**
	 * Sets the SystemData property name.
	 * @param objName the name of the SystemData property to retrieve.
	 */
	public void setName(String objName) {
		_pName = objName;
	}
	
	/**
	 * If the SystemData property is a Map, save only its values to the request.
	 * @param mapToList TRUE if only values are to be set, otherwise FALSE
	 */
	public void setMapValues(boolean mapToList) {
	    _doMapToList = mapToList;
	}
	
	/**
	 * Sort the values.
	 * @param doSort TRUE if the collection values should be sorted, otherwise FALSE
	 */
	public void setSort(boolean doSort) {
	    _doSort = doSort;
	}
	
	/**
	 * Sets the default value of the attribute, if the SystemData property is not found.
	 * @param value the default value
	 */
	public void setDefault(Object value) {
		_defaultValue = value;
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
	    super.release();
	    _defaultValue = null;
	    _doMapToList = false;
	    _doSort = false;
	}

	/**
	 * Executes the tag handler and stuffs the SystemData object into the request.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public int doEndTag() throws JspException {
		
		// Get the system data object and stuff it into the request
	    Object obj = SystemData.getObject(_pName);
	    if (obj == null)
	    	obj = _defaultValue;
	    
	    // If the object is a map and MapValues is selected, get the values object
	    if ((obj instanceof Map) && _doMapToList)
	        obj = new LinkedHashSet(((Map) obj).values());
	    
	    // If we're a collection and Sort is selected, sort it
	    if ((obj instanceof Collection) && _doSort) {
	        List tmpResults = new ArrayList((Collection) obj);
	        Collections.sort(tmpResults);
	        obj = tmpResults;
	    }
	        
	    // Save in the request
	    pageContext.setAttribute(_varName, obj, PageContext.PAGE_SCOPE);	        
	    release();
		return EVAL_PAGE;
	}
}