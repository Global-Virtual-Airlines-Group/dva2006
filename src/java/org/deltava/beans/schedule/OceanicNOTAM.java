// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.Date;

/**
 * A bean to store NOTAMs containing Oceanic route data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class OceanicNOTAM extends OceanicRoute implements Comparable<OceanicNOTAM> {
	
    private String _sourceHost;
    private String _routeInfo;

	/**
	 * Initializes the NOTAM bean.
	 * @param type the route type
	 * @param dt the effective date
	 */
	public OceanicNOTAM(int type, Date dt) {
		super(type);
		setDate(dt);
	}
	
    /**
     * Returns the source hostname where this NOTAM was downloaded from.
     * @return the hostname
     * @see OceanicNOTAM#setSource(String)
     */
    public String getSource() {
        return _sourceHost;
    }
    
    /**
     * Returns the route NOTAM text.
     * @return the NOTAM text
     * @see OceanicNOTAM#setRoute(String)
     */
    public String getRoute() {
        return _routeInfo;
    }
    
    /**
     * Updates the NOTAM source hostname.
     * @param srcHost the host this NOTAM was downloaded from
     * @throws NullPointerException if srcHost is null
     * @see OceanicNOTAM#getSource()
     */
    public void setSource(String srcHost) {
        _sourceHost = srcHost.trim().toLowerCase();
    }
    
    /**
     * Updates the NOTAM contents.
     * @param routeData the NOTAM contents
     * @see OceanicNOTAM#getRoute()
     */
    public void setRoute(String routeData) {
        _routeInfo = routeData;
    }
    
    /**
     * Implements Comparable interface by comparing the dates, then the route types.
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(OceanicNOTAM or2) {
        int tmpResult = getDate().compareTo(or2.getDate());
        return (tmpResult == 0) ? new Integer(getType()).compareTo(new Integer(or2.getType())) : tmpResult;
    }
    
    public String toString() {
		StringBuilder buf = new StringBuilder(getTypeName());
		buf.append('-');
		buf.append(getDate().toString());
		return buf.toString();
    }
}