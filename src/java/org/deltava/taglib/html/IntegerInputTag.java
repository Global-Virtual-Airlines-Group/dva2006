// Copyright 2012, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

/**
 * An HTML 5 JSP tag for integer input elements. 
 * @author Luke
 * @version 8.6
 * @since 5.0
 */

public class IntegerInputTag extends NumberInputTag {

	/**
	 * Sets the maximum value.
	 * @param n the value
	 */
	@Override
	public void setMax(int n) {
		_data.setAttribute("max", String.valueOf(n));
	}
	
	/**
	 * Sets the minimum value.
	 * @param n the value
	 */
	public void setMin(int n) {
		_data.setAttribute("min", String.valueOf(n));
	}
	
	/**
	 * Sets the value stepping interval.
	 * @param s the interval
	 */
	public void setStep(int s) {
		_data.setAttribute("step", String.valueOf(s));
	}
}