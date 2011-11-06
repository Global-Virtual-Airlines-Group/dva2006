// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.lang.annotation.*;

/**
 * An annotation to mark a helper class.
 * @author Luke
 * @version 4.1
 * @since 4.1
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Helper {

	/**
	 * Returns the class this typically operates on.
	 * @return a Class object
	 */
	public Class<?> value();
}