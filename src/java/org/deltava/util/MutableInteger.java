 // Copyright 2010, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

/**
 * A class to store mutable Integer values.
 * @author Luke
 * @version 8.7
 * @since 8.7
 */

public class MutableInteger implements Comparable<MutableInteger> {
	private int _value;
	
	/**
	 * Creates the integer.
	 * @param value the initial value
	 */
	public MutableInteger(int value) {
		super();
		_value = value;
	}
	
	/**
	 * Increments the value.
	 */
	public void inc() {
		_value++;
	}
	
	/**
	 * Returns the value.
	 * @return the value
	 */
	public Integer getValue() {
		return Integer.valueOf(_value);
	}
	
	@Override
	public String toString() {
		return String.valueOf(_value);
	}
	
	@Override
	public int hashCode() {
		return _value;
	}

	@Override
	public int compareTo(MutableInteger i2) {
		return Integer.compare(_value, i2._value);
	}
}