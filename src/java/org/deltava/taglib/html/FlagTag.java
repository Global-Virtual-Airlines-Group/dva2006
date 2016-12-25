// Copyright 2009, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import org.deltava.util.StringUtils;

/**
 * A JSP tag to display a national flag image.
 * @author Luke
 * @version 7.2
 * @since 2.5
 */

public class FlagTag extends ImageTag {

	/**
	 * Creates a new Flag Image element tag.
	 */
	public FlagTag() {
		super();
		setX(16);
		setY(11);
	}
	
	/**
	 * Resets the tag's state variables.
	 */
	@Override
	public void release() {
		super.release();
		setX(16);
		setY(11);
	}

	/**
	 * Set the country code for the flag to display.
	 * @param code the ISO 3166-1 country code
	 */
	public void setCountryCode(String code) {
		if (StringUtils.isEmpty(code)) return;
		StringBuilder buf = new StringBuilder("flags/");
		buf.append(code.toLowerCase());
		buf.append(".png");
		setSrc(buf.toString());
	}
}