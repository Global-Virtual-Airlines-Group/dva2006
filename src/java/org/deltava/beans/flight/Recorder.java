// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

/**
 * An enumeration of Flight Data Recorder clients. 
 * @author Luke
 * @version 7.0
 * @since 7.0
 */

public enum Recorder {
	ACARS, XACARS, SIMFDR;

	/**
	 * Returns the recorder name.
	 */
	@Override
	public String toString() {
		return (this == SIMFDR) ? "simFDR" : name();
	}
}