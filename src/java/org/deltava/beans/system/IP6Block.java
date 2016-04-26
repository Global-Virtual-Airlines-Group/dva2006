// Copyright 2009, 2012, 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.math.BigInteger;

import org.deltava.util.*;

/**
 * A bean to store IPv6 address block information.
 * @author Luke
 * @version 7.0
 * @since 5.2
 */

public class IP6Block extends IPBlock {
	
	private final BigInteger _rawEndAddr;
	private final BigInteger _endAddr;
	
	/**
	 * Initializes the bean.
	 * @param id the block ID
	 * @param start the start IP address
	 * @param end the ending IP address
	 * @param size the CIDR size in bits
	 */
	public IP6Block(int id, String start, String end, int size) {
		super(id, start, end, size);
		_endAddr = NetworkUtils.pack(end);
		_rawEndAddr = NetworkUtils.pack(start);
	}

	@Override
	public IPAddress getType() {
		return IPAddress.IPV6;
	}
	
	/**
	 * Checks whether this IP block contains a specific IP address.
	 * @param addr the IP address
	 * @return TRUE if the block contains the address, otherwise FALSE
	 */
	@Override
	public boolean contains(String addr) {
		BigInteger ia = NetworkUtils.pack(addr);
		return (ia.compareTo(_rawEndAddr) > -1) && (ia.compareTo(_endAddr) < 1);
	}

	/**
	 * Compares two IP Ranges by comparing their base addresses and mask sizes.
	 */
	@Override
	public int compareTo(IPBlock ib2) {
		int tmpResult = IPAddress.IPV6.compareTo(ib2.getType());
		if (tmpResult == 0) {
			IP6Block ib6 = (IP6Block) ib2;
			tmpResult = _rawEndAddr.compareTo(ib6._rawEndAddr);
			if (tmpResult == 0)
				tmpResult = Integer.valueOf(getBits()).compareTo(Integer.valueOf(ib6.getBits()));
		}
		
		return tmpResult;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(getAddress());
		buf.append('/').append(128 - getBits());
		return buf.toString();
	}

	@Override
	public Object cacheKey() {
		return Long.valueOf(getID() << 32);
	}
}