// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.functions;

import org.deltava.beans.EMailAddress;

import org.deltava.util.StringUtils;

/**
 * A JSP Function Library for Pilot-related functions.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PersonFunctions {

	/**
	 * Returns wether the supplied e-mail address is valid.
	 * @param addr the e-mail address
	 * @return TRUE if the address is valid, otherwise FALSE
	 */
	public static boolean isEMailValid(String addr) {
		return (!StringUtils.isEmpty(addr)) && (!EMailAddress.INVALID_ADDR.equals(addr));
	}
}