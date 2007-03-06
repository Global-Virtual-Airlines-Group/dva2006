// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.net.*;

/**
 * A utility class to handle TCP/IP network operations.
 * @author Luke
 * @version 1.0
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
		return ((addr[0] << 24) + (addr[1] << 16) + (addr[2] << 8) + addr[3]);
	}
	
	/**
	 * Converts an address into a packed IP address. 
	 * @param addr the IP address or host name
	 * @return a packed 32-bit address
	 */
	public static int pack(String addr) {
		try {
			InetAddress ip = InetAddress.getByName(addr);
			return convertIP(ip.getAddress());
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
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