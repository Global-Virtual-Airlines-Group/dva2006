// Copyright 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

/**
 * An XACARS Flight Information object. 
 * @author Luke
 * @version 7.2
 * @since 4.1
 */

public class XAFlightInfo extends FDRFlightReport {
	
	/**
	 * Enumeration to track climb/cruise/descent.
	 */
	public enum ClimbPhase {
		UNKNOWN("N/A"), CLIMB("Climb"), CRUISE("Cruise"), DESCEND ("Descent");
		
		private String _name;
		
		ClimbPhase(String name) {
			_name = name;
		}
		
		public String getName() {
			return _name;
		}
	}
	
	private Airport _aL;
	private String _cruiseAlt;
	
	private FlightPhase _phase = FlightPhase.PREFLIGHT;
	private ClimbPhase _cPhase = ClimbPhase.UNKNOWN;
	private int _zfw;
	
	/**
	 * Creates the Flight Info object.
	 * @param a the Airline
	 * @param leg the leg number
	 */
	public XAFlightInfo(Airline a, int leg) {
		super(a, leg, 1);
	}
	
	@Override
	public Recorder getFDR() {
		return Recorder.XACARS;
	}
	
	/**
	 * Returns the flight phase.
	 * @return a FlightPhase
	 */
	public FlightPhase getPhase() {
		return _phase;
	}
	
	/**
	 * Returns the climb phase.
	 * @return a ClimbPhase
	 */
	public ClimbPhase getClimbPhase() {
		return _cPhase;
	}
	
	/**
	 * Returns the alternate Airport.
	 * @return the Airport, or null if not filed
	 */
	public Airport getAirportL() {
		return _aL;
	}
	
	/**
	 * Returns the cruise altitude.
	 * @return the cruising altitude or flight level
	 */
	public String getCruiseAltitude() {
		return _cruiseAlt;
	}
	
	/**
	 * Returns the aircraft weight without fuel.
	 * @return the weight in pounds
	 */
	public int getZeroFuelWeight() {
		return _zfw;
	}
	
	@Override
    public final int getLength() {
    	return (int)((getStartTime().toEpochMilli() - getEndTime().toEpochMilli()) / 3_600_000);
    }
    
    @Override
    public double getAverageFrameRate() {
    	return 0;
    }
	
	/**
	 * Updates the aircraft weight without fuel.
	 * @param zfw the weight in pounds
	 */
	public void setZeroFuelWeight(int zfw) {
		_zfw = Math.max(1, zfw);
	}
	
	/**
	 * Sets the cruising altitude.
	 * @param alt the altitude or flight level
	 */
	public void setCruseAltitude(String alt) {
		_cruiseAlt = alt.toUpperCase();
	}
	
	/**
	 * Sets the alternate Airport.
	 * @param a the Airport
	 */
	public void setAirportL(Airport a) {
		_aL = a;
	}
	
	/**
	 * Updates the flight phase.
	 * @param p a FlightPhase
	 */
	public void setPhase(FlightPhase p) {
		_phase = p;
	}

	/**
	 * Updates the climb phase.
	 * @param cp a ClimbPhase
	 */
	public void setClimbPhase(ClimbPhase cp) {
		_cPhase = cp;
	}
}