// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.flightplan;

import java.util.Collection;

import org.deltava.beans.navdata.NavigationDataBean;

import org.deltava.util.XMLUtils;

/**
 * A Flight Plan Generator for Microsoft Flight Simulator 2020.
 * @author Luke
 * @version 9.1
 * @since 9.1
 */

public class FS2020Generator extends MSFSXMLGenerator {
	
	/**
	 * Creates the generator.
	 */
	public FS2020Generator() {
		super(11, 282174);
	}

	@Override
	public String generate(Collection<NavigationDataBean> waypoints) {
		return XMLUtils.format(generateDocument(waypoints), "UTF-8");
	}
}