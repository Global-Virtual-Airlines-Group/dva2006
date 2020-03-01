// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import org.deltava.beans.*;

/**
 * An enumeration of Flight Report status entry types.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

@Helper(FlightHistoryEntry.class)
public enum HistoryType implements EnumDescription {
	LIFECYCLE, SYSTEM, UPDATE
}