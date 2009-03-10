// Copyright 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;

/**
 * A utility class to handle TCP/IP network operations.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public class NetworkUtils {

	// Singleton constructor
	private NetworkUtils() {
	}

	/**
	 * Converts a 32-bit packed IP address into 4 bytes.
	 * @param addr the 32-bit address
	 * @return an array of bytes
	 * @see java.net.InetAddress#getByAddress(byte[])
	 */
	public static byte[] convertIP(int addr) {
		byte[] results = new byte[4];
		results[3] = (byte) (addr & 0xFF);
		results[2] = (byte) ((addr & 0xFF00) >> 8);
		results[1] = (byte) ((addr & 0xFF0000) >> 16);
		results[0] = (byte) ((addr & 0xFF000000) >> 24);
		return results;
	}

	/**
	 * Converts a four byte IP address into a 32-bit packed address. The most significant bits of the address are in the first
	 * element.
	 * @param addr an array of bytes
	 * @return a 32-bit packed address
	 */
	public static int convertIP(byte[] addr) {
		int address  = addr[3] & 0xFF;
		address |= ((addr[2] << 8) & 0xFF00);
		address |= ((addr[1] << 16) & 0xFF0000);
		address |= ((addr[0] << 24) & 0xFF000000);
		return address;	
	}
	
	/**
	 * Converts an address into a packed IP address. 
	 * @param addr the IP address or host name
	 * @return a packed 32-bit address
	 */
	public static int pack(String addr) {
		byte[] results = new byte[4];
		StringTokenizer tkns = new StringTokenizer(addr, ".");
		for (int x = 0; (x < 4) && (tkns.hasMoreTokens()); x++) {
			String bt = tkns.nextToken();
			results[x] = (byte) (StringUtils.parse(bt, 0) & 0xFF);
		}

		return convertIP(results);
	}
	
	/**
	 * Formats a four-byte IP address.
	 * @param addr the IP address
	 * @return the formatted IP address
	 */
	public static String format(byte[] addr) {
		StringBuilder buf = new StringBuilder();
		buf.append(addr[0]);
		buf.append('.');
		buf.append(addr[1]);
		buf.append('.');
		buf.append(addr[2]);
		buf.append('.');
		buf.append(addr[3]);
		return buf.toString();
	}
}