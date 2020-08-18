// Copyright 2012, 2015, 2017, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.flightplan;

import java.util.Collection;

import org.deltava.beans.navdata.*;

import org.deltava.util.*;

/**
 * A Flight Plan Generator for Lockheed-Martin Prepar3D.
 * @author Luke
 * @version 9.1
 * @since 5.0
 */

public class P3DGenerator extends MSFSXMLGenerator {

	/**
	 * Creates the Generator.
	 */
	public P3DGenerator() {
		super(10, 12946);
	}
	
	@Override
	public String generate(Collection<NavigationDataBean> waypoints) {
		return XMLUtils.format(generateDocument(waypoints), "UTF-8");
	}
}