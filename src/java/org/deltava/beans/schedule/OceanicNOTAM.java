// Copyright 2007, 2009, 2013, 2016, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.time.Instant;

import org.deltava.beans.navdata.*;

/**
 * A bean to store NOTAMs containing Oceanic route data.
 * @author Luke
 * @version 11.6
 * @since 1.0
 */

public class OceanicNOTAM implements OceanicTrackInfo, Comparable<OceanicNOTAM> {

	private Instant _date;
	private Instant _fetchDate;
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
	
	@Override
    public Instant getDate() {
        return _date;
    }
    
    @Override
    public Type getType() {
        return _routeType;
    }
    
    /**
     * Returns the retrieval date of the track data.
     * @return the retrieval date/time
     */
	public Instant getFetchDate() {
    	return _fetchDate;
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
     * Updates the retrieval date of this NOTAM.
     * @param dt the retrieval date/time
     * @see OceanicNOTAM#getFetchDate()
     */
    public void setFetchDate(Instant dt) {
    	_fetchDate = dt;
    }
    
    @Override
    public int compareTo(OceanicNOTAM or2) {
        int tmpResult = _date.compareTo(or2._date);
        return (tmpResult == 0) ? _routeType.compareTo(or2._routeType) : tmpResult;
    }
    
    @Override
	public int hashCode() {
    	return toString().hashCode();
    }
  
    @Override
    public String toString() {
		StringBuilder buf = new StringBuilder(_routeType.name());
		buf.append('-');
		buf.append(getDate().toString());
		return buf.toString();
    }
}