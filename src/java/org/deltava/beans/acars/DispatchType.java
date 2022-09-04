// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import org.deltava.beans.EnumDescription;

/**
 * An enmeration of Dispatcher types. 
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public enum DispatchType implements EnumDescription {
	NONE, DISPATCH, SIMBRIEF;

	@Override
	public String getDescription() {
		return (this == SIMBRIEF)  ? "SimBrief" : EnumDescription.super.getDescription(); 
	}
}