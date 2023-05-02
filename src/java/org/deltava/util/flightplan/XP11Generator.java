// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.flightplan;

import java.io.*;
import java.util.Collection;

import org.deltava.beans.navdata.NavigationDataBean;

import org.deltava.util.StringUtils;

/**
 * A Flight Plan Generator for X-Plane 11/12.
 * @author Luke
 * @version 10.6
 * @since 10.6
 */

public class XP11Generator extends XPGenerator {
	
	@Override
	public String generate(Collection<NavigationDataBean> waypoints) {
		StringWriter out = new StringWriter();
		try (PrintWriter ctx = new CustomNewlineWriter(out)) {
			// Write header
			ctx.println('I');
			ctx.println("1100 Version");
			if (!StringUtils.isEmpty(_navCycle)) {
				ctx.print("CYCLE ");
				ctx.println(_navCycle);
			}
				
			ctx.print("ADEP ");
			ctx.println(_aD.getICAO());
			ctx.print("ADES ");
			ctx.println(_aA.getICAO());
			ctx.print("NUMENR ");
			ctx.println(waypoints.size());
			
			// Write navaids
			int idx = 0;
			for (NavigationDataBean nd : waypoints) {
				ctx.print(getNavaidType(nd.getType()));
				ctx.print(' ');
				ctx.print(nd.getCode());
				ctx.print(' ');
				if (idx == 0)
					ctx.print("ADEP");
				else if (idx == (waypoints.size() - 1))
					ctx.print("ADES");
				if (nd.isInTerminalRoute()) {
					String aw = nd.getAirway();
					ctx.print(aw.substring(0, aw.indexOf('.')));
				} else if (!StringUtils.isEmpty(nd.getAirway()))
					ctx.print(nd.getAirway());
				else
					ctx.print("DRCT");
				
				ctx.print(" 0.00000 ");
				ctx.print(StringUtils.format(nd.getLatitude(), "#0.00000"));
				ctx.print(' ');
				ctx.println(StringUtils.format(nd.getLongitude(), "##0.00000"));
				idx++;
			}
		}
		
		return out.toString();
	}

	@Override
	public String getExtension() {
		return "fms";
	}
}