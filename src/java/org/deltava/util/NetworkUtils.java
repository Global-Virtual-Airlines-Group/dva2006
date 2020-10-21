// Copyright 2007, 2009, 2011, 2013, 2014, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.net.*;

/**
 * A utility class to handle TCP/IP network operations.
 * @author Luke
 * @version 5.4
 * @since 1.0
 */

public class NetworkUtils {
	
	/**
	 * IP Address type enumeration.
	 */
	public enum AddressType {
		UNKNOWN, IPv4, IPv6
	}
	
	// Singleton constructor
	private NetworkUtils() {
		super();
	}

	/**
	 * Returns an IP address type.
	 * @param addr an IP address
	 * @return the AddressType
	 */
	public static AddressType getType(String addr) {
		try {
			InetAddress ia = InetAddress.getByName(addr);
			return (ia instanceof Inet6Address) ? AddressType.IPv6 : AddressType.IPv4;
		} catch (Exception e) {
			return AddressType.UNKNOWN;
		}
	}
	
	/**
	 * Formats a packed IP address.
	 * @param addr the IP address
	 * @return the formatted IP address
	 */
	public static String format(byte[] addr) {
		char sep = (addr.length == 16) ? ':' : '.';
		StringBuilder buf = new StringBuilder();
		for (int x = 0; x < addr.length; x++) {
			int b = addr[x];
			if (b < 0)
				b += 256;
			
			buf.append(b);
			if (x < (addr.length - 1))
				buf.append(sep);
		}
		
		return buf.toString();
	}
	
	/**
	 * Returns a address in host:port format.
	 * @param sa the address
	 * @return the address in host:port format
	 */
	public static String getSourceAddress(SocketAddress sa) {
		InetSocketAddress addr = (InetSocketAddress) sa;
		boolean isIPv6 = (addr.getAddress() instanceof Inet6Address);
		StringBuilder buf = new StringBuilder();
		buf.append(addr.getAddress().getHostAddress());
		buf.append(isIPv6 ? '%' : ':');
		buf.append(addr.getPort());
		return buf.toString();
	}
}