// Copyright 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import org.deltava.beans.EMailAddress;

/**
 * A utility class for e-mail.
 * @author Luke
 * @version 9.0
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
	 * @param domain the domain name
	 * @param name the recipient name
	 * @return an EMailAddress object
	 */
	public static EMailAddress makeAddress(String addr, String domain, String name) {
		return makeAddress(addr + "@" + domain, name);
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

	/**
	 * Retrieves the domain within an e-mail address.
	 * @param addr the address
	 * @return the domain, or null if none
	 */
	public static String getDomain(String addr) {
		int pos = addr.lastIndexOf('@');
		if (pos == -1) return null;
		
		return addr.substring(pos + 1).toLowerCase();
	}
}