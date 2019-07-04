// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2015, 2016, 2017, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;
import java.time.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;

/**
 * A class for storing ACARS-submitted Flight Reports.
 * @author Luke
 * @version 8.6
 * @since 1.0
 */

public class ACARSFlightReport extends FDRFlightReport implements FlightTimes {
	
    private final Map<Long, Integer> _time = new HashMap<Long, Integer>();
    
    private ILSCategory _ils;
    private double _landingG;
    
    private String _fde;
    private String _code;
    private String _sdk;
    private long _capabilities;
    
    private Instant _departureTime;
    private Instant _arrivalTime;
    private OnTime _onTime = OnTime.UNKNOWN;
    
    private int _paxWeight;
    private int _cargoWeight;
    
    private double _avgFrames;
    
    private boolean _hasReload;
    private int _clientBuild;
    private int _beta;
    
    /**
     * Creates a new ACARS Flight Report object with a given flight.
     * @param a the Airline
     * @param flightNumber the Flight Number
     * @param leg the Leg Number
     * @throws NullPointerException if the Airline Code is null
     * @throws IllegalArgumentException if the Flight Report is zero or negative
     * @throws IllegalArgumentException if the Leg is less than 1 or greater than 8
     * @see Flight#setAirline(Airline)
     * @see Flight#setFlightNumber(int)
     * @see Flight#setLeg(int)
     */
    public ACARSFlightReport(Airline a, int flightNumber, int leg) {
        super(a, flightNumber, leg);
    }
    
	@Override
	public Recorder getFDR() {
		return Recorder.ACARS;
	}

	@Override
	public Duration getBlockTime() {
		return Duration.ofSeconds(getTime(1) + getTime(2) + getTime(4));
	}
    
    /**
     * Returns the G-Forces at touchdown.
     * @return the G-Forces in G
     * @see ACARSFlightReport#setLandingG(double)
     */
    public double getLandingG() {
    	return _landingG;
    }
    
    /**
     * Returns the amount of time at a given time acceleration rate.
     * @param rate the acceleration rate
     * @return the amount of time in seconds at that acceleration rate
     * @see ACARSFlightReport#setTime(int, int)
     * @see ACARSFlightReport#getTimes()
     */
    public int getTime(int rate) {
    	Integer time = _time.get(Long.valueOf(rate));
    	return (time == null) ? 0 : time.intValue();
    }
    
    /**
     * Returns a Map of times and acceleration rates.
     * @return a sorted Map of times, keyed by acceleration rate
     * @see ACARSFlightReport#getTime(int)
     */
    public Map<Long, Integer> getTimes() {
    	return new TreeMap<Long, Integer>(_time);
    }
    
    /**
     * Returns the length of the fllight <i>in hours multiplied by ten</i>. This is done to avoid rounding errors when
     * using a floating point number. ACARS Flight Reports will use the time at 1x, 2x, and 4x acceleration to calculate
     * the flight length if the length field is not already populated. This assumes that the ACARS client has performed
     * the proper multiplication before submitting the data. 
     * @return the length of the flight <i>in hours multiplied by ten</i>
     * @see FlightReport#getLength()
     * @see ACARSFlightReport#getTime(int)
     */
    @Override
    public final int getLength() {
    	return (super.getLength() != 0) ? super.getLength() : (getTime(1) + getTime(2) + getTime(4)) / 360;
    }
    
    /**
     * Returns the FDE used for this flight.
     * @return the AIR file name, or null
     * @see ACARSFlightReport#setFDE(String)
     */
    public String getFDE() {
    	return _fde;
    }
    
    /**
     * Returns the aircraft SDK used for this flight.
     * @return the SDK code, or null
     * @see ACARSFlightReport#setSDK(String)
     */
    public String getSDK() {
    	return _sdk;
    }
    
    /**
     * Returns the aircraft code used for this flight.
     * @return the aircraft code
     * @see ACARSFlightReport#setAircraftCode(String)
     */
    public String getAircraftCode() {
    	return _code;
    }
    
    @Override
	public double getAverageFrameRate() {
		return _avgFrames;
	}

    /**
     * Returns if the flight was reloaded by ACARS mid-flight.
     * @return TRUE if reloaded, otherwise FALSE
     * @see ACARSFlightReport#setHasReload(boolean)
     */
    public boolean getHasReload() {
    	return _hasReload;
    }
    
    /**
     * Returns the ACARS client build number.
     * @return the client build number
     * @see org.deltava.beans.acars.ConnectionEntry#setClientBuild(int)
     */
    public int getClientBuild() {
       return _clientBuild;
    }
    
    /**
     * Returns the ACARS beta build number.
     * @return the beta number
     * @see org.deltava.beans.acars.ConnectionEntry#setBeta(int)
     */
    public int getBeta() {
 	   return _beta;
    }
    
	/**
	 * Returns the ILS category.
	 * @return the ILS Category
	 */
	public ILSCategory getLandingCategory() {
		return _ils;
	}
	
	/**
	 * Returns the total passenger weight.
	 * @return the weight in pounds
	 */
	public int getPaxWeight() {
		return _paxWeight;
	}
	
	/**
	 * Returns the total cargo weight.
	 * @return the weight in pounds
	 */
	public int getCargoWeight() {
		return _cargoWeight;
	}
	
	/**
	 * Returns the aircraft/simulator capabilities flags.
	 * @return the Capabilities flag bitmap
	 */
	public long getCapabilities() {
		return _capabilities;
	}
	
	/**
	 * Returns the flight start date/time in the simulator. 
	 * @return the departure date/time
	 */
	@Override
	public ZonedDateTime getTimeD() {
		return (_departureTime == null) ? null : ZonedDateTime.ofInstant(_departureTime, getAirportD().getTZ().getZone());
	}
	
	/**
	 * Returns the flight arrival date/time in the simulator.
	 * @return the arrival date/time
	 */
	@Override
	public ZonedDateTime getTimeA() {
		return (_arrivalTime == null) ? null : ZonedDateTime.ofInstant(_arrivalTime, getAirportA().getTZ().getZone());
	}
	
	/**
	 * Returns whether the flight matched the schedule times.
	 * @return an OnTime enumeration
	 */
	public OnTime getOnTime() {
		return _onTime;
	}
    
    /**
     * Updates the G-Forces at touchdown.
     * @param g the G-forces in G
     * @see ACARSFlightReport#getLandingG()
     */
    public void setLandingG(double g) {
    	if (!Double.isNaN(g))
    		_landingG = g;
    }
    
    /**
     * Updates the amount of time at a particular time acceleration rate.
     * @param rate the acceleration rate
     * @param secs the amount of time in seconds
     * @throws IllegalArgumentException if rate is not 0, 1, 2, 4
     * @see ACARSFlightReport#getTime(int)
     */
    public void setTime(int rate, int secs) {
    	if ((rate < 0) || (rate == 3) || (rate > 4))
    		throw new IllegalArgumentException("Rate must be 0, 1 2 or 4 - " + rate);
    	
    	_time.put(Long.valueOf(rate), Integer.valueOf(Math.max(0, secs)));
    }
    
    /**
     * Updates the FDE used for this flight.
     * @param airFile the AIR file name, or null if unknown
     * @see ACARSFlightReport#getFDE()
     */
    public void setFDE(String airFile) {
    	_fde = airFile;
    }
    
    /**
     * Updates the aircraft code used for this flight.
     * @param code the aircraft code
     * @see ACARSFlightReport#getAircraftCode()
     */
    public void setAircraftCode(String code) {
    	_code = code;
    }
    
	/**
	 * Updates the average simulator frame rate for this flight.
	 * @param avgRate the average rate in frames per second
	 * @see FDRFlightReport#getAverageFrameRate()
	 */
	public void setAverageFrameRate(double avgRate) {
		_avgFrames = Math.max(0, avgRate);
	}
    
    /**
     * Sets if the flight was restored by ACARS mid-flight.
     * @param hasReload TRUE if a reload occured, otherwise FALSE
     * @see ACARSFlightReport#getHasReload()
     */
    public void setHasReload(boolean hasReload) {
    	_hasReload = hasReload;
    }
    
    /**
     * Updates the ACARS client build number.
     * @param ver the build number
     * @see org.deltava.beans.acars.ConnectionEntry#getClientBuild()
     */
    public void setClientBuild(int ver) {
       _clientBuild = Math.max(1, ver);
    }
    
    /**
     * Updates the ACARS beta build number.
     * @param beta the beta number
     * @see org.deltava.beans.acars.ConnectionEntry#getBeta()
     */
    public void setBeta(int beta) {
 	   _beta = Math.max(0, beta);
    }
    
	/**
	 * Sets the ILS category.
	 * @param ilscat the ILS category
	 */
	public void setLandingCategory(ILSCategory ilscat) {
		_ils = ilscat;
	}
	
	/**
	 * Sets the aircraft SDK used.
	 * @param sdk the SDK code
	 */
	public void setSDK(String sdk) {
		_sdk = sdk;
	}
	
	/**
	 * Sets aircraft/sim capabilities flags.
	 * @param cap the Capabilities flags
	 */
	public void setCapabilities(long cap) {
		_capabilities = cap;
	}
	
	/**
	 * Updates the passenger weight.
	 * @param wt the total passenger weight, in pounds
	 */
	public void setPaxWeight(int wt) {
		_paxWeight = Math.max(0, wt);
	}

	/**
	 * Updates the cargo weight.
	 * @param wt the total cargo weight, in pounds
	 */
	public void setCargoWeight(int wt) {
		_cargoWeight = Math.max(0, wt);
	}
	
	/**
	 * Updates the departure date/time.
	 * @param dt the departure date/time
	 */
	public void setDepartureTime(Instant dt) {
		_departureTime = dt;
	}

	/**
	 * Updates the arrival date/time.
	 * @param dt the arrival date/time
	 */
	public void setArrivalTime(Instant dt) {
		_arrivalTime = dt;
	}

	/**
	 * Updates whether the flight was on time.
	 * @param o an OnTime value
	 */
	public void setOnTime(OnTime o) {
		_onTime = o;
	}
}