// Copyright 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import org.deltava.beans.Simulator;
import org.deltava.beans.schedule.*;

/**
 * A class for storing XACARS-submitted Flight Reports.
 * @author Luke
 * @version 7.0
 * @since 4.1
 */

public class XACARSFlightReport extends FDRFlightReport {
	
	private Airport _aL;
	
	private int _majorVersion;
	private int _minorVersion;
	
	/**
     * Creates a new XACARS Flight Report object.
     */
	public XACARSFlightReport(Airline a, int flightNumber, int leg) {
		super(a, flightNumber, leg);
		setSimulator(Simulator.XP10);
	}
	
	@Override
	public Recorder getFDR() {
		return Recorder.XACARS;
	}
	
    /**
     * Returns the XACARS major version used on this flight.
     * @return the major version
     */
    public int getMajorVersion() {
    	return _majorVersion;
    }
    
    /**
     * Returns the XACARS minor version used on this flight.
     * @return the minor version
     */
    public int getMinorVersion() {
    	return _minorVersion;
    }
    
    @Override
    public double getAverageFrameRate() {
    	return 0;
    }
    
    /**
     * Returns the alternate Airport.
     * @return the Airport, or null if none
     */
    public Airport getAirportL() {
    	return _aL;
    }
    
    /**
     * Updates the alternate Airport.
     * @param a the Airport, or null if none
     */
    public void setAirportL(Airport a) {
    	_aL = a;
    }
    
    /**
     * Sets the XACARS minor version used on this flight.
     * @param v the minor version
     */
    public void setMinorVersion(int v) {
    	_minorVersion = Math.max(0, v);
    }

    /**
     * Sets the XACARS major version used on this flight.
     * @param v the major version
     */
    public void setMajorVersion(int v) {
    	_majorVersion = Math.max(0, v);
    }
}