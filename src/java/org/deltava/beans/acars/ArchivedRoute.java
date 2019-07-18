// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.navdata.*;

/**
 * A bean to store archivted route data and AIRAC version.
 * @author Luke
 * @version 8.6
 * @since 8.6
 */

public class ArchivedRoute extends DatabaseBean implements Route {

	private final int _airacVersion;
	
	private final Collection<NavigationDataBean> _wps = new ArrayList<NavigationDataBean>();
	
	/**
	 * Creates the bean.
	 * @param id the ACARS flight ID
	 * @param airacVersion the AIRAC version in effect
	 */
	public ArchivedRoute(int id, int airacVersion) {
		super();
		setID(id);
		_airacVersion = airacVersion;
	}
	
	/**
	 * Returns the AIRAC version in effect.
	 * @return the version code
	 */
	public int getAIRACVersion() {
		return _airacVersion;
	}

	@Override
	public void addWaypoint(NavigationDataBean nd) {
		_wps.add(nd);
	}

	@Override
	public LinkedList<NavigationDataBean> getWaypoints() {
		return new LinkedList<NavigationDataBean>(_wps);
	}

	@Override
	public int getSize() {
		return _wps.size();
	}

	@Override
	public String getRoute() {
		StringBuilder buf = new StringBuilder();
		for (Iterator<NavigationDataBean> i = _wps.iterator(); i.hasNext(); ) {
			NavigationDataBean nd = i.next();
			buf.append(nd.getCode());
			if (i.hasNext())
				buf.append(' ');
		}
		
		return buf.toString();
	}
}