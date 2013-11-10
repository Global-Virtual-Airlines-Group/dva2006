// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.googlemap;

import javax.servlet.jsp.tagext.TagSupport;

/**
 * An abstract class to support JSON-loaded weather data.
 * @author Luke
 * @version 5.2
 * @since 5.2
 */

abstract class WeatherJSTag extends TagSupport {
	
	private final String _defaultFunction;
	
	/**
	 * Data parsing function name.
	 */
	protected String _jsFunc;
	
	/**
	 * Constructor. Sets the default function to execute on load. 
	 * @param defaultFunction the default function name
	 * @see WeatherJSTag#setFunction(String)
	 */
	protected WeatherJSTag(String defaultFunction) {
		super();
		_defaultFunction = defaultFunction;
		_jsFunc = defaultFunction;
	}
	
	/**
	 * Overrides the data parsing function name.
	 * @param jsFunc a JavaScript function name
	 */
	public void setFunction(String jsFunc) {
		_jsFunc = jsFunc;
	}
	
	/**
	 * Releases the tag's state data.
	 */
	@Override
	public void release() {
		super.release();
		_jsFunc = _defaultFunction;
	}
}