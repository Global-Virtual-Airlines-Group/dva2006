// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.jmx;

/**
 * An interface to mark JMX statistics refresh tasks.
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

interface JMXRefresh {

	/**
	 * Updates the JMX statistics.
	 */
	public void update();
}