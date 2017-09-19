// Copyright 2009, 2012, 2013, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import org.deltava.util.*;

/**
 * A bean to store IP address block information.
 * @author Luke
 * @version 8.0
 * @since 5.2
 */

public class IP4Block extends IPBlock {
	
	private final long _rawAddr;
	private final long _rawEndAddr;
	
	/**
	 * Initializes the bean.
	 * @param id the block ID
	 * @param start the start IP address
	 * @param end the ending IP address
	 * @param size the CIDR size in bits
	 */
	public IP4Block(int id, String start, String end, int size) {
		super(id, start, end, size);
		_rawAddr = NetworkUtils.pack(start).longValue();
		_rawEndAddr = NetworkUtils.pack(end).longValue();
	}

	@Override
	public IPAddress getType() {
		return IPAddress.IPV4;
	}

	@Override
	public boolean contains(String addr) {
		long intAddr = NetworkUtils.pack(addr).longValue();
		return (intAddr >= _rawAddr) && (intAddr <= _rawEndAddr);
	}

	@Override
	public int compareTo(IPBlock ib2) {
		int tmpResult = IPAddress.IPV4.compareTo(ib2.getType());
		if (tmpResult == 0) {
			IP4Block ib4 = (IP4Block) ib2;
			tmpResult = Long.compare(_rawAddr, ib4._rawAddr);
			if (tmpResult == 0)
				tmpResult = Integer.compare(getBits(), ib4.getBits());
		}
		
		return tmpResult;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(getAddress());
		buf.append('/').append(getBits());
		return buf.toString();
	}
}