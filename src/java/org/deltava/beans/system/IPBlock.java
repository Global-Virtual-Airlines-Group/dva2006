// Copyright 2009, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import org.deltava.util.*;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store CIDR address blocks.
 * @author Luke
 * @version 5.0
 * @since 2.5
 */

public class IPBlock implements Cacheable, Comparable<IPBlock> {
	
	private final String _baseAddr;
	private final long _rawAddr;
	private final long _endAddr;
	private final int _bits;

	/**
	 * Initializes the bean.
	 * @param addr the CIDR block
	 */
	public IPBlock(String addr) {
		super();
		
		// Split the address
		int pos = addr.indexOf('/');
		if (pos == -1) {
			_baseAddr = addr;
			_bits = 32;
		} else {
			_baseAddr = addr.substring(0, pos);	
			_bits = StringUtils.parse(addr.substring(pos + 1), 32);
		}
		
		_rawAddr = NetworkUtils.pack(_baseAddr);
		_endAddr = _rawAddr + (1 << (32 - _bits));
	}

	/**
	 * Returns the base Address of the block
	 * @return the base IP address
	 */
	public String getAddress() {
		return _baseAddr;
	}
	
	/**
	 * Returns the size of the address block.
	 * @return the size of the block in bits
	 */
	public int getBits() {
		return _bits;
	}
	
	/**
	 * Returns the size of the block.
	 * @return the number of addresses in the block
	 */
	public int getSize() {
		return (int) (_endAddr - _rawAddr);
	}
	
	/**
	 * Checks whether this IP block contains a specific IP address.
	 * @param addr the IP address
	 * @return TRUE if the block contains the address, otherwise FALSE
	 */
	public boolean contains(String addr) {
		long intAddr = NetworkUtils.pack(addr);
		return (intAddr >= _rawAddr) && (intAddr <= _endAddr);
	}

	/**
	 * Compares two IP Ranges by comparing their base addresses and mask sizes.
	 */
	public int compareTo(IPBlock ib2) {
		int tmpResult = new Long(_rawAddr).compareTo(new Long(ib2._rawAddr));
		if (tmpResult == 0)
			tmpResult = Integer.valueOf(_bits).compareTo(Integer.valueOf(ib2._bits));
		
		return tmpResult;
	}
	
	public Object cacheKey() {
		return toString();
	}

	public String toString() {
		StringBuilder buf = new StringBuilder(_baseAddr);
		buf.append('/').append(_bits);
		return buf.toString();
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
}