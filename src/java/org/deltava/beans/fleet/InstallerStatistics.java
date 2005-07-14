// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.fleet;

import java.io.Serializable;

/**
 * A bean to store Fleet Installer System Information statistics.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InstallerStatistics implements Serializable {
	
	private String _label;
	private int _count;

	/**
	 * Create a new Statistics bean.
	 * @param label the statistics label
	 * @param count the statistics count
	 */
	public InstallerStatistics(String label, int count) {
		super();
		_label = label;
		_count = count;
	}

	/**
	 * Return the count.
	 * @return the count
	 */
	public int getCount() {
		return _count;
	}
	
	/**
	 * Return the label.
	 * @return the label
	 */
	public String getLabel() {
		return _label;
	}
}