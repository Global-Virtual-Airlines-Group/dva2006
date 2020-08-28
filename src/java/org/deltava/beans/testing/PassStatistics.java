// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

/**
 * An interface for beans that store pass/fail/total counts.
 * @author Luke
 * @version 9.1
 * @since 9.1
 */

public interface PassStatistics {

	/**
	 * Returns the total number of invocations.
	 * @return the number of invocations
	 */
	public int getTotal();
	
	/**
	 * Returns the number of test passes or correct answers.
	 * @return the number of successes
	 */
	public int getPassCount();
}