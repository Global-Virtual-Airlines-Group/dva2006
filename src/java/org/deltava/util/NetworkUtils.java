// Copyright 2007, 2009, 2011, 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.net.*;
import java.math.BigInteger;

/**
 * A utility class to handle TCP/IP network operations.
 * @author Luke
 * @version 5.2
 * @since 1.0
 */

public class NetworkUtils {
	
	// Singleton constructor
	private NetworkUtils() {
		super();
	}

	/**
	 * Converts a four byte IP address into a 32-bit packed address. The most significant bits of the address are in the first
	 * element.
	 * @param addr an array of bytes
	 * @return a 32-bit packed address
	 */
	@Deprecated
	public static long convertIP(byte[] addr) {
		long address  = addr[3] & 0xFF;
		address |= ((addr[2] << 8) & 0xFF00);
		address |= ((addr[1] << 16) & 0xFF0000);
		address |= ((addr[0] << 24) & 0xFF000000);
		return address;	
	}
	
	public static BigInteger pack(String addr) {
		try {
			InetAddress ia = InetAddress.getByName(addr);
			return new BigInteger(ia.getAddress());
		} catch (UnknownHostException uhe) {
			throw new IllegalArgumentException(addr);
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
		StringBuilder buf = new StringBuilder(addr.getAddress().getHostAddress());
		buf.append(':');
		buf.append(addr.getPort());
		return buf.toString();
	}
}