// Copyright 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;

/**
 * A utility class to handle TCP/IP network operations.
 * @author Luke
 * @version 2.5
 * @since 1.0
 */

public class NetworkUtils {
	
	/**
	 * Network classes.
	 */
	public enum NetworkType {
		A(16777216, 0xFF000000),
		B(65536, 0xFFFF0000),
		C(256, 0xFFFFFF00), 
		D(268435456, -1),
		E(268435456, -1);
		
		private final int _size;
		private final long _mask;
		
		NetworkType(int size, long mask) {
			_size = size;
			_mask = mask;
		}
		
		public long getMask() {
			return _mask;
		}
		
		public int size() {
			return _size;
		}
	}

	// Singleton constructor
	private NetworkUtils() {
		super();
	}

	/**
	 * Converts a 32-bit packed IP address into 4 bytes.
	 * @param addr the 32-bit address
	 * @return an array of bytes
	 * @see java.net.InetAddress#getByAddress(byte[])
	 */
	public static byte[] convertIP(long addr) {
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
	public static long convertIP(byte[] addr) {
		long address  = addr[3] & 0xFF;
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
	public static long pack(String addr) {
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
		for (int x = 0; x < 4; x++) {
			int b = addr[x];
			if (b < 0)
				b += 256;
			
			buf.append(b);
			if (x < 3)
				buf.append('.');
		}
		
		return buf.toString();
	}
	
	/**
	 * Returns the network type. This has been supplemented by CIDR. 
	 * @param addr the IP address
	 * @return the NetworkType
	 */
	public static NetworkType getNetworkType(byte[] addr) {
		int b = addr[0];
		if (b < 0)
			b += 256;
		
		if (b < 128)
			return NetworkType.A;
		else if (b < 192)
			return NetworkType.B;
		else if (b < 224)
			return NetworkType.C;
		else if (b < 240)
			return NetworkType.D;
		
		return NetworkType.E;
	}
}