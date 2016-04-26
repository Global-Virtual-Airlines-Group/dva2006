// Copyright 2007, 2009, 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.time.Instant;

import org.deltava.beans.navdata.*;

/**
 * A bean to store NOTAMs containing Oceanic route data.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class OceanicNOTAM implements OceanicTrackInfo, Comparable<OceanicNOTAM> {

	private Instant _date;
    private Type _routeType;
    private String _sourceHost;
    private String _routeInfo;

	/**
	 * Initializes the NOTAM bean.
	 * @param type the route type
	 * @param dt the effective date
	 */
	public OceanicNOTAM(Type type, Instant dt) {
		super();
		setType(type);
		_date = dt;
	}
	
    /**
     * Returns the date of the Oceanic Route NOTAM.
     * @return the route date
     * @see OceanicNOTAM#setDate(Instant)
     */
	@Override
    public Instant getDate() {
        return _date;
    }
    
    /**
     * Returns the route type code.
     * @return the route type
     */
    @Override
    public Type getType() {
        return _routeType;
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
     * Updates the NOTAM effective date.
     * @param d the NOTAM date
     * @see OceanicTrackInfo#getDate()
     */
    public void setDate(Instant d) {
        _date = d;
    }
    
    /**
     * Updates the route type.
     * @param type the route type code
     * @see OceanicTrackInfo#getType()
     */
    public void setType(Type type) {
        _routeType = type;
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
    @Override
    public int compareTo(OceanicNOTAM or2) {
        int tmpResult = _date.compareTo(or2._date);
        return (tmpResult == 0) ? _routeType.compareTo(or2._routeType) : tmpResult;
    }
  
    @Override
    public String toString() {
		StringBuilder buf = new StringBuilder(_routeType.name());
		buf.append('-');
		buf.append(getDate().toString());
		return buf.toString();
    }
}