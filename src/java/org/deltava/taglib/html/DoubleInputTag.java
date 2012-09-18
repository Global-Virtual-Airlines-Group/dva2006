// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

/**
 * An HTML 5 JSP tag for floating point input elements. 
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class DoubleInputTag extends NumberInputTag {

	/**
	 * Sets the maximum value.
	 * @param n the value
	 */
	public void setMax(Double n) {
		_data.setAttribute("max", String.valueOf(n));
	}
	
	/**
	 * Sets the minimum value.
	 * @param n the value
	 */
	public void setMin(Double n) {
		_data.setAttribute("min", String.valueOf(n));
	}
	
	/**
	 * Sets the value stepping interval.
	 * @param s the interval
	 */
	public void setStep(Double s) {
		_data.setAttribute("step", String.valueOf(s));
	}
}