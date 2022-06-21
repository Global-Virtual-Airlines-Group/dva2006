// Copyright 2012, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to insert optionally minified content. Content will be minified if the SystemData environment name starts with &quot;prod&quot;.
 * @author Luke
 * @version 10.2
 * @since 5.0
 */

abstract class InsertMinifiedContentTag extends InsertContentTag {

	private String _actualResourceName;
	private boolean _isMin;
	
	/**
	 * Returns the actual file name of the requested resource.
	 * @return the file name
	 */
	protected String getFileName() {
		return _actualResourceName;
	}
	
	/**
	 * Returns whether this resource is minified.
	 * @return TRUE if minified, otherwise FALSE
	 */
	protected boolean isMinified() {
		return _isMin;
	}
	
	@Override
	public int doStartTag() {
		String env = SystemData.get(SystemData.CFG_NAME);
		_isMin = ((env != null) && env.startsWith("prod"));
		
		// Detect minification override
		boolean minOV = Boolean.parseBoolean(pageContext.getRequest().getParameter("minOV"));
		if (minOV) _isMin = !_isMin;
		
		// If running in prod, minify - otherwise 
		StringBuilder buf = new StringBuilder(_resourceName);
		if (_isMin)
			buf.append("-min");
		
		_actualResourceName = buf.toString();
		return SKIP_BODY;
	}
}