// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.EnumDescription;

/**
 * An enumeration of Flight Tour statuses.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public enum TourStatus implements EnumDescription {
	PLANNING, QC, PENDING, COMPLETE;

	@Override
	public String getDescription() {
		return (this == QC) ? "Quality Control" : EnumDescription.super.getDescription(); 
	}
}