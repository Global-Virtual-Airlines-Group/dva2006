// Copyright 2012, 2022, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to insert optionally minified content. Content will be minified if the SystemData environment name starts with &quot;prod&quot;.
 * @author Luke
 * @version 12.0
 * @since 5.0
 */

abstract class InsertMinifiedContentTag extends InsertContentTag {

	private String _actualResourceName;
	private boolean _isMin;
	private Boolean _override;
	
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
	
	/**
	 * Manually overrides the minification state of this resource. 
	 * @param override TRUE to force minification, FALSE to prevent
	 */
	public void setMinify(boolean override) {
		_override = Boolean.valueOf(override);
	}
	
	@Override
	public void release() {
		super.release();
		_override = null;
	}
	
	@Override
	public int doStartTag() {
		String env = SystemData.get(SystemData.CFG_NAME);
		_isMin = ((env != null) && env.startsWith("prod"));
		
		// Detect minification overrides
		boolean minOV = Boolean.parseBoolean(pageContext.getRequest().getParameter("minOV"));
		if (minOV) _isMin = !_isMin;
		if (_override != null)
			_isMin = _override.booleanValue();
		
		// If running in prod, minify - otherwise 
		StringBuilder buf = new StringBuilder(_resourceName);
		if (_isMin)
			buf.append("-min");
		
		_actualResourceName = buf.toString();
		return SKIP_BODY;
	}
}