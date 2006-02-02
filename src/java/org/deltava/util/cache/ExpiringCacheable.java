// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.util.Date;

/**
 * An interface to mark a cacheable object that sets its own expiration date.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface ExpiringCacheable extends Cacheable {

	/**
	 * Returns the date this object should be considered expired.
	 * @return the expiry date/time
	 */
	public Date getExpiryDate();
}
