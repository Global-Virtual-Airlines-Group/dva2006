// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;
import java.time.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;

/**
 * A class for storing ACARS-submitted Flight Reports.
 * @author Luke
 * @version 10.3
 * @since 1.0
 */

public class ACARSFlightReport extends FDRFlightReport implements FlightTimes {
	
	public static final String GENERIC_SDK = "Generic";
	
    private final Map<Long, Integer> _time = new HashMap<Long, Integer>();
    private Duration _boardTime = Duration.ZERO;
    private Duration _deboardTime = Duration.ZERO;
    private Duration _onlineTime = Duration.ZERO;
    
    private String _tailCode;
    
    private ILSCategory _ils;
    private double _landingG;
    
    private String _fde;
    private String _code;
    private String _sdk;
    private long _capabilities;
    private String _acAuthor;
	private String _acPath;
    
    private Instant _departureTime;
    private Instant _arrivalTime;
    private OnTime _onTime = OnTime.UNKNOWN;
    
    private int _paxWeight;
    private int _cargoWeight;
    
    private double _avgFrames;
    
    private int _restoreCount;
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
     * Returns the amount of time spent loading passengers/cargo. 
     * @return the amount of time in seconds
     * @see ACARSFlightReport#setBoardTime(int)
     * @see ACARSFlightReport#getDeboardTime()
     */
    public Duration getBoardTime() {
    	return _boardTime;
    }
    
    /**
     * Returns the amount of time spent unloading passengers/cargo.
     * @return the amount of time in seconds
     * @see ACARSFlightReport#setDeboardTime(int)
     * @see ACARSFlightReport#getBoardTime()
     */
    public Duration getDeboardTime() {
    	return _deboardTime;
    }
    
    /**
     * Returns the amount of time spent connected to an Online Network.
     * @return the amount of time in seconds
     * @see ACARSFlightReport#setOnlineTime(int)
     */
    public Duration getOnlineTime() {
    	return _onlineTime;
    }
    
    /**
     * Returns the date/time that the aircraft reached Top of Climb.
     * @return the date/time of top of climb
     * @see ACARSFlightReport#setTOCTime(Instant)
     */
    public Instant getTOCTime() {
    	return _stateChangeTimes.get(StateChange.TOC);
    }
    
    /**
     * Returns the date/time that the aircraft reached Top of Descent.
     * @return the date/time of top of descent
     * @see ACARSFlightReport#setTODTime(Instant)
     */
    public Instant getTODTime() {
    	return _stateChangeTimes.get(StateChange.TOD);
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
    
	/**
	 * Returns the aircraft author.
	 * @return the author name
	 * @see ACARSFlightReport#setAuthor(String)
	 */
	public String getAuthor() {
		return _acAuthor;
	}
	
	/**
	 * Returns the aircraft path in the simulator. 
	 * @return the path name
	 * @see ACARSFlightReport#setAircraftPath(String)
	 */
	public String getAircraftPath() {
		return _acPath;
	}
    
    @Override
	public double getAverageFrameRate() {
		return _avgFrames;
	}

    /**
     * Returns the number of ACARS in-flight resotres on this flight.
     * @return the number of flight restores
     * @see ACARSFlightReport#setRestoreCount(int)
     */
    public int getRestoreCount() {
    	return _restoreCount;
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
	 * Returns the local takeoff date/time. This is the simulator's local time at takeoff.
	 * @return the takeoff date/time
	 */
	public ZonedDateTime getLocalTakeoffTime() {
		if ((_departureTime == null) || !_stateChangeTimes.containsKey(StateChange.START) || !_stateChangeTimes.containsKey(StateChange.TAKEOFF)) return null;
		Duration d = Duration.between(getStartTime(), getTakeoffTime());
		return getTimeD().plus(d);
	}
	
	/**
	 * Returns the local landing date/time. This is the simulator's local time at touchdown.
	 * @return the touchdown date/time
	 */
	public ZonedDateTime getLocalLandingTime() {
		if ((_departureTime == null) || !_stateChangeTimes.containsKey(StateChange.END) || !_stateChangeTimes.containsKey(StateChange.LAND)) return null;
		Duration d = Duration.between(getEndTime(), getLandingTime());
		return getTimeA().plus(d);
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
	 * Returns the registration code of the aircraft.
	 * @return the tail code
	 */
	public String getTailCode() {
		return _tailCode;
	}
	
	/**
	 * Updates the registration code of the aircraft.
	 * @param tc the tail code
	 */
	public void setTailCode(String tc) {
		if ((tc != null) && (tc.length() > 14))
			_tailCode = tc.substring(0, 14).toUpperCase();
		else if (tc != null)
			_tailCode = tc.trim().toUpperCase();
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
     * Updates the amount of time spent loading passengers/cargo.
     * @param secs the time in seconds
     * @see ACARSFlightReport#getBoardTime()
     * @see ACARSFlightReport#setDeboardTime(int)
     */
    public void setBoardTime(int secs) {
    	_boardTime = Duration.ofSeconds(Math.max(0, secs));
    }
    
    /**
     * Updates the amount of time spent unloading passengers/cargo.
     * @param secs the time in seconds
     * @see ACARSFlightReport#getDeboardTime()
     * @see ACARSFlightReport#setBoardTime(int)
     */
    public void setDeboardTime(int secs) {
    	_deboardTime = Duration.ofSeconds(Math.max(0, secs));
    }
    
    /**
     * Updates the amount of time spent connected to an Online Network.
     * @param secs the time in seconds
     * @see ACARSFlightReport#getOnlineTime()
     */
    public void setOnlineTime(int secs) {
    	_onlineTime = Duration.ofSeconds(Math.max(0, secs));
    }
    
    /**
     * Updates the Top of Climb date/time.
     * @param dt the date/time at top of climb
     * @see ACARSFlightReport#getTOCTime()
     */
    public void setTOCTime(Instant dt) {
    	_stateChangeTimes.put(StateChange.TOC, dt);
    }
    
    /**
     * Updates the Top of Descent date/time.
     * @param dt the date/time at top of descent
     * @see ACARSFlightReport#getTODTime()
     */
    public void setTODTime(Instant dt) {
    	_stateChangeTimes.put(StateChange.TOD, dt);
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
     * Updates the number of ACARS in-flight restores during this flight.
     * @param cnt the number of flight restores
     * @see ACARSFlightReport#getRestoreCount()
     */
    public void setRestoreCount(int cnt) {
    	_restoreCount = cnt;
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
	 * Updates the aircraft author.
	 * @param author the author name
	 * @see ACARSFlightReport#getAuthor()
	 */
	public void setAuthor(String author) {
		_acAuthor = author;
	}

	/**
	 * Updates the aircraft path in the simulator. 
	 * @param path the path name
	 * @see ACARSFlightReport#getAircraftPath()
	 */
	public void setAircraftPath(String path) {
		_acPath = path;
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
	 * Updates the departure date/time. This is the simulator's local time at start time.
	 * @param dt the departure date/time
	 * @see FDRFlightReport#getStartTime()
	 */
	public void setDepartureTime(Instant dt) {
		_departureTime = dt;
	}
	
	/**
	 * Updates the arrival date/time. This is the simulators' local time at end time.
	 * @param dt the arrival date/time
	 * @see FDRFlightReport#getEndTime()
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