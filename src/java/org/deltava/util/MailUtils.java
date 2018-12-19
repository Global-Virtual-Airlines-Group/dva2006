// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import org.deltava.beans.EMailAddress;

/**
 * A utility class for e-mail.
 * @author Luke
 * @version 8.5
 * @since 8.5
 */

public class MailUtils {
	
	private static class EMailSender implements EMailAddress {

		private final String _name;
		private final String _addr;

		EMailSender(String addr, String name) {
			super();
			_name = name;
			_addr = addr;
		}

		@Override
		public String getEmail() {
			return _addr;
		}

		@Override
		public String getName() {
			return _name;
		}

		@Override
		public boolean isInvalid() {
			return false;
		}
		
		@Override
		public String toString() {
			return _name + " (" + _addr + ")";
		}
		
		@Override
		public int hashCode() {
			return toString().hashCode();
		}
	}
	
	// static class
	private MailUtils() {
		super();
	}

	/**
	 * Utility method to create an e-mail address object.
	 * @param addr the recipient address
	 * @param name the recipient name
	 * @return an EMailAddress object
	 */
	public static EMailAddress makeAddress(String addr, String name) {
		return new EMailSender(addr, name);
	}

	/**
	 * Utility method to create an e-mail address object.
	 * @param addr the recipient address
	 * @return an EMailAddress object, with the recipient address and name the same
	 */
	public static EMailAddress makeAddress(String addr) {
		return makeAddress(addr, addr);
	}
}