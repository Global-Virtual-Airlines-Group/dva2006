// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.time.Instant;

import org.deltava.util.CIDRBlock;

/**
 * A bean to store login/blacklist registration entries. 
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class BlacklistEntry {
	
	private final CIDRBlock _cidr;
	private Instant _created;
	private String _comments;

	/**
	 * Creates the bean.
	 * @param addr the base IP address
	 * @param prefixLength the network prefix length in bits
	 */
	public BlacklistEntry(String addr, int prefixLength) {
		super();
		_cidr = new CIDRBlock(addr, prefixLength);
	}

	/**
	 * Returns the CIDR block for the blacklist entry.
	 * @return the CIDRBlock
	 */
	public CIDRBlock getCIDR() {
		return _cidr;
	}
	
	/**
	 * Returns the creation date of this entry.
	 * @return the creation date/time
	 */
	public Instant getCreated() {
		return _created;
	}
	
	/**
	 * Returns any blacklist comments.
	 * @return the comments
	 */
	public String getComments() {
		return _comments;
	}
	
	/**
	 * Returns whether this blacklist entry contains a particular IP address.
	 * @param addr the IP address
	 * @return TRUE if contained by this blacklist, otherwise FALSE
	 */
	public boolean contains(String addr) {
		return _cidr.isInRange(addr);
	}
	
	/**
	 * Updates the creation date of this blacklist entry.
	 * @param dt the creation date/time
	 */
	public void setCreated(Instant dt) {
		_created = dt;
	}

	/**
	 * Updates the blacklist entry comments.
	 * @param comments the comments
	 */
	public void setComments(String comments) {
		_comments = comments;
	}
	
	@Override
	public int hashCode() {
		return _cidr.hashCode();
	}
	
	@Override
	public String toString() {
		return _cidr.toString();
	}
}