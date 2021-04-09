// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import org.deltava.beans.EnumDescription;

/**
 * An enumeration of Flight types for customs/gate purposes.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public enum FlightType implements EnumDescription {
	UNKNOWN, DOMESTIC, INTERNATIONAL, USPFI, SCHENGEN;
	
	@Override
	public String getDescription() {
		return (this == USPFI) ? name() : EnumDescription.super.getDescription();
	}
}