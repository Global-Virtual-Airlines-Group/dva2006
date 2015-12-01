// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface for objects whose usage is counted.
 * @author Luke
 * @version 6.3
 * @since 6.3
 */

public interface UseCount {

	/**
	 * Returns the number of times this object has been used.
	 * @return the number of uses
	 */
	public int getUseCount();
}