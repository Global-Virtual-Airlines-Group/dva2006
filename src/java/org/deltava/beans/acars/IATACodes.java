// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store mappings of IATA aircraft codes.
 * @author Luke
 * @version 5.1
 * @since 5.1
 */

public class IATACodes extends LinkedHashMap<String, Integer> implements Cacheable, Comparable<IATACodes> {

	private final String _eqType;
	
	/**
	 * Initializes the bean.
	 * @param eqType the equipment type name
	 */
	public IATACodes(String eqType) {
		super();
		_eqType = eqType;
	}

	/**
	 * Returns the equipment type.
	 * @return the equipment type name
	 */
	public String getEquipmentType() {
		return _eqType;
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder(_eqType);
		buf.append(' ');
		buf.append(super.toString());
		return buf.toString();
	}
	
	public Object cacheKey() {
		return _eqType;
	}
	
	public int hashCode() {
		return _eqType.hashCode();
	}
	
	public int compareTo(IATACodes c2) {
		return _eqType.compareTo(c2._eqType);
	}
}