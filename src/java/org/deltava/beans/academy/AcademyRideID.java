// Copyright 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.academy;

/**
 * A bean to store Flight Academy Check Ride Script IDs. 
 * @author Luke
 * @version 7.0
 * @since 5.3
 */

public class AcademyRideID {
	
	private final String _name;
	private final int _idx;

	/**
	 * Creates the ID.
	 * @param name the Certification code
	 * @param idx the ride index
	 */
	public AcademyRideID(String name, int idx) {
		this(name + "-" + idx);
	}
	
	/**
	 * Creates the ID.
	 * @param id the Name-ID pair
	 */
	public AcademyRideID(String id) {
		super();
		int ofs = id.lastIndexOf('-');
		_name = (ofs < 1) ? id : id.substring(0, ofs);
		_idx = (ofs < 1) ? 1 : Integer.parseInt(id.substring(ofs+1));
	}
	
	/**
	 * Returns the certification name.
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the Check Ride index.
	 * @return the index
	 */
	public int getIndex() {
		return _idx;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_name);
		buf.append('-');
		buf.append(_idx);
		return buf.toString();
	}
}