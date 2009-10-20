// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

/**
 * An enumeration to determine whether a waypoint code is just a code, or encoded
 * coordinates as either a quadrant (with a single letter at the end) or a full lat/long set
 * with two letters.
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public enum CodeType {

	CODE, QUADRANT, FULL;
}