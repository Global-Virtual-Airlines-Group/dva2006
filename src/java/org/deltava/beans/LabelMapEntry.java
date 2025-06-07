// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface for Map markers that have a label below the marker.
 * @author Luke
 * @version 12.0
 * @since 12.0
 */

public interface LabelMapEntry extends MapEntry {

	/**
	 * Returns the label text.
	 * @return the label text
	 */
	public String getLabel();
}