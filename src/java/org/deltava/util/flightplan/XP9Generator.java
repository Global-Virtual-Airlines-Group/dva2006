// Copyright 2010, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.flightplan;

import java.io.*;
import java.util.Collection;

import org.deltava.beans.navdata.*;

import org.deltava.util.StringUtils;

/**
 * A Flight Plan Generator for X-Plane 9.
 * @author Luke
 * @version 7.0
 * @since 2.8
 */

public class XP9Generator extends XPGenerator {

	/**
	 * Generates an X-Plane 9 flight plan between two airports.
	 * @param waypoints a Collection of waypoints
	 * @return the generated flight plan file
	 */
	@Override
	public String generate(Collection<NavigationDataBean> waypoints) {
		StringWriter out = new StringWriter();
		try (PrintWriter ctx = new CustomNewlineWriter(out)) {
			// Write header
			ctx.println('I');
			ctx.println("3 version");
			ctx.println('4');
			ctx.println('1');

			// Write navaids
			for (NavigationDataBean nd : waypoints) {
				ctx.print(getNavaidType(nd.getType()));
				ctx.print(' ');
				ctx.print(nd.getCode());
				ctx.print(" 0.00000 ");
				ctx.print(StringUtils.format(nd.getLatitude(), "#0.00000"));
				ctx.print(' ');
				ctx.println(StringUtils.format(nd.getLongitude(), "##0.00000"));
			}
		}

		return out.toString();
	}

	@Override
	public String getExtension() {
		return "fms";
	}
}