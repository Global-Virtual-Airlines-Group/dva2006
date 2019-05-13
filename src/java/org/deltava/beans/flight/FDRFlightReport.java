// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2014, 2016, 2017, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;
import java.time.*;

import org.deltava.beans.GeospaceLocation;
import org.deltava.beans.schedule.*;

/**
 * A bean to store FDR (ACARS/XACARS/simFDR) submitted flight reports. 
 * @author Luke
 * @version 8.3
 * @since 1.0
 */

public abstract class FDRFlightReport extends FlightReport {
	
	private final Map<StateChange, Instant> _stateChangeTimes = new HashMap<StateChange, Instant>();
	
    private int _taxiWeight;
    private int _taxiFuel;
    
    private int _takeoffDistance;
    private int _takeoffSpeed;
    private int _takeoffWeight;
    private int _takeoffFuel;
    private double _takeoffN1;
    private int _takeoffHdg = -1;
    private GeospaceLocation _takeoffPos = new GeoPosition(0, 0);

    private int _landingDistance;
    private int _landingSpeed;
    private int _landingVspeed;
    private int _landingWeight;
    private int _landingFuel;
    private double _landingN1;
    private int _landingHdg = -1;
    private GeospaceLocation _landingPos = new GeoPosition(0, 0);

    private int _gateWeight;
    private int _gateFuel;

    private int _totalFuel;
    
    /**
     * Creates a new FDR Flight Report object.
     * @param a the Airline
     * @param flightNumber the flight number 
     * @param leg the leg number 
     */
	public FDRFlightReport(Airline a, int flightNumber, int leg) {
		super(a, flightNumber, leg);
	}
	
	/**
	 * Returns the software used to record this Flight.
	 * @return a Recorder
	 */
	public abstract Recorder getFDR();
	
	@Override
	public final Duration getDuration() {
		return getBlockTime();
	}

    /**
     * Returns the start time of this flight.
     * @return the start date/time
     * @see FDRFlightReport#setStartTime(Instant)
     */
    public Instant getStartTime() {
        return _stateChangeTimes.get(StateChange.START);
    }
    
    /**
     * Returns the date/time of pushback.
     * @return the date/time the aircraft was pushed back
     * @see FDRFlightReport#setTaxiTime(Instant)
     */
    public Instant getTaxiTime() {
        return _stateChangeTimes.get(StateChange.TAXI_OUT);
    }
    
    /**
     * Returns the aircraft weight at pushback.
     * @return the weight in pounds
     * @see FDRFlightReport#setTaxiWeight(int)
     */
    public int getTaxiWeight() {
        return _taxiWeight;
    }
    
    /**
     * Returns the amount of fuel at pushback.
     * @return the amount of fuel in pounds
     * @see FDRFlightReport#setTaxiFuel(int)
     */
    public int getTaxiFuel() {
        return _taxiFuel;
    }
    
    /**
     * Returns the date/time of takeoff.
     * @return the date/time the aircraft left the ground
     * @see FDRFlightReport#setTakeoffTime(Instant)
     */
    public Instant getTakeoffTime() {
        return _stateChangeTimes.get(StateChange.TAKEOFF);
    }
    
    /**
     * Returns the distance from the origin airport at takeoff. This is used to validate the provided airports.
     * @return the distance from the origin airport, in miles
     * @see FDRFlightReport#setTakeoffDistance(int)
     * @see FDRFlightReport#getLandingDistance()
     * @see org.deltava.beans.schedule.GeoPosition#distanceTo(org.deltava.beans.GeoLocation)
     */
    public int getTakeoffDistance() {
        return _takeoffDistance;
    }
    
    /**
     * Returns the aircraft airspeed at takeoff.
     * @return the airspeed in knots
     * @see FDRFlightReport#setTakeoffSpeed(int)
     */
    public int getTakeoffSpeed() {
        return _takeoffSpeed;
    }

    /**
     * Returns the weight of the aircraft at takeoff.
     * @return the weight in pounds
     * @see FDRFlightReport#setTakeoffWeight(int)
     */
    public int getTakeoffWeight() {
        return _takeoffWeight;
    }
    
    /**
     * Returns the amount of fuel at takeoff.
     * @return the amount of fuel in pounds
     * @see FDRFlightReport#setTakeoffFuel(int)
     */
    public int getTakeoffFuel() {
        return _takeoffFuel;
    }
    
    /**
     * Returns the heading at takeoff.
     * @return the takeoff heading in degrees
     * @see FDRFlightReport#setTakeoffHeading(int)
     */
    public int getTakeoffHeading() {
    	return _takeoffHdg;
    }
    
    /**
     * Returns the average N1 speed of the engines at takeoff.
     * @return the average N1 percentage, mulitiplied by 100
     * @see ACARSFlightReport#setTakeoffN1(double)
     * @see ACARSFlightReport#getLandingN1()
     */
    public double getTakeoffN1() {
        return _takeoffN1;
    }
    
    /**
     * Returns the position at takeoff.
     * @return the takeoff position
     * @see FDRFlightReport#setTakeoffLocation(GeospaceLocation)
     */
    public GeospaceLocation getTakeoffLocation() {
    	return _takeoffPos;
    }
    
    /**
     * Returns the date/time that the aircraft touched down.
     * @return the date/time of touchdown
     * @see FDRFlightReport#setLandingTime(Instant)
     */
    public Instant getLandingTime() {
        return _stateChangeTimes.get(StateChange.LAND);
    }
    
    /**
     * Returns the distance from the destination airport at landing. This is used to validate the provided airports.
     * @return the distance from the destination airport, in miles
     * @see FDRFlightReport#setLandingDistance(int)
     * @see FDRFlightReport#getTakeoffDistance()
     * @see org.deltava.beans.schedule.GeoPosition#distanceTo(org.deltava.beans.GeoLocation)
     */
    public int getLandingDistance() {
        return _landingDistance;
    }
    
    /**
     * Returns the aircraft airspeed at touchdown.
     * @return the airspeed in knots
     * @see FDRFlightReport#setLandingSpeed(int)
     */
    public int getLandingSpeed() {
        return _landingSpeed;
    }

    /**
     * Returns the vertical speed of the aircraft at touchdown.
     * @return the vertical speed in feet per minute
     * @see FDRFlightReport#setLandingVSpeed(int)
     */
    public int getLandingVSpeed() {
        return _landingVspeed;
    }

    /**
     * Returns the weight of the aircraft at touchdown.
     * @return the weight in pounds
     * @see FDRFlightReport#setLandingWeight(int)
     */
    public int getLandingWeight() {
        return _landingWeight;
    }
    
    /**
     * Returns the amount of fuel at touchdown.
     * @return the amount of fuel in pounds
     * @see FDRFlightReport#setLandingFuel(int)
     */
    public int getLandingFuel() {
        return _landingFuel;
    }
    
    /**
     * Returns the heading at touchdown.
     * @return the touchdown heading in degrees
     * @see FDRFlightReport#setLandingHeading(int)
     */
    public int getLandingHeading() {
    	return _landingHdg;
    }
    
    /**
     * Returns the average N1 speed of the engines at touchdown.
     * @return the average N1 percentage, multiplied by 100
     * @see ACARSFlightReport#setLandingN1(double)
     * @see ACARSFlightReport#getTakeoffN1()
     */
    public double getLandingN1() {
        return _landingN1;
    }
    
    /**
     * Returns the position at touchdown.
     * @return the touchdown heading
     * @see FDRFlightReport#setLandingLocation(GeospaceLocation)
     */
    public GeospaceLocation getLandingLocation() {
    	return _landingPos;
    }

    /**
     * Returns the end date/time of the flight.
     * @return the date/time the flight ended at the gate
     */
    public Instant getEndTime() {
        return _stateChangeTimes.get(StateChange.END);
    }

    /**
     * Returns the weight of the aircraft at the end of the flight.
     * @return the weight in pounds
     * @see FDRFlightReport#setGateWeight(int)
     */
    public int getGateWeight() {
        return _gateWeight;
    }
    
    /**
     * Returns the amount of fuel at the end of the flight.
     * @return the amount of fuel in pounds
     * @see FDRFlightReport#setGateFuel(int)
     */
    public int getGateFuel() {
        return _gateFuel;
    }

    /**
     * Returns the total amount of fuel burned.
     * @return the total amount of fuel in pounds
     * @see FDRFlightReport#setTotalFuel(int)
     */
    public int getTotalFuel() {
    	return _totalFuel;
    }

    /**
     * Returns the time that the aircraft was airborne for this flight.
     * @return the Duration the aircraft was airborne
     * @throws NullPointerException if either the landing or takeoff time are not set
     * @see FDRFlightReport#setTakeoffTime(Instant)
     * @see FDRFlightReport#setLandingTime(Instant)
     */
    public Duration getAirborneTime() {
       	return Duration.between(getTakeoffTime(), getLandingTime());
    }
    
    /**
     * Returns the total time of the flight.
     * @return the total Duration
     * @throws NullPointerException if either the start or end time are not set
     * @see FDRFlightReport#setStartTime(Instant)
     * @see FDRFlightReport#setEndTime(Instant)
     */
    public Duration getBlockTime() {
       	return Duration.between(getStartTime(), getEndTime());
    }
    
    /**
     * Returns the outbound taxi time.
     * @return the taxi Duration
     * @throws NullPointerException if either taxi time or takeoff time are not set
     */
    public Duration getTaxiOutTime() {
    	Duration d = Duration.between(getTaxiTime(), getTakeoffTime());
    	return (d.abs().toMinutes() < 120) ? d : null; 
    }
    
    /**
     * Returns the inbound taxi time.
     * @return the taxi Duration
     * @throws NullPointerException if either landing time or end time are not set
     */
    public Duration getTaxiInTime() {
    	Duration d = Duration.between(getLandingTime(), getEndTime());
    	return (d.abs().toMinutes() < 120) ? d : null;
    }

    /**
     * Returns the average frame rate for this flight.
     * @return the average frame rate in frames per second
     */
    public abstract double getAverageFrameRate();

    /**
     * Updates the start time of the flight.
     * @param dt the date/time the flight started
     * @see FDRFlightReport#getEndTime()
     */
    public void setStartTime(Instant dt) {
        _stateChangeTimes.put(StateChange.START, dt);
    }
    
    /**
     * Updates the time the aircraft was pushed back.
     * @param dt the date/time of pushback
     * @see FDRFlightReport#getTaxiTime()
     */
    public void setTaxiTime(Instant dt) {
        _stateChangeTimes.put(StateChange.TAXI_OUT, dt);
    }

    /**
     * Updates the weight of the aircraft at pushback.
     * @param w the weight in pounds
     * @see FDRFlightReport#getTaxiWeight()
     */
    public void setTaxiWeight(int w) {
        _taxiWeight = Math.max(0, w);
    }
    
    /**
     * Updates the fuel amount at pushback.
     * @param f the amount of fuel in pounds
     * @see FDRFlightReport#getTaxiFuel()
     */
    public void setTaxiFuel(int f) {
        _taxiFuel = Math.max(0, f);
    }
    
    /**
     * Updates the takeoff date/time.
     * @param dt the date/time at takeoff
     * @see FDRFlightReport#getTakeoffTime()
     */
    public void setTakeoffTime(Instant dt) {
        _stateChangeTimes.put(StateChange.TAKEOFF, dt);
    }

    /**
     * Updates the distance from the origin airport where takeoff occured.
     * @param d the distance in miles
     * @see FDRFlightReport#getTakeoffDistance()
     */
    public void setTakeoffDistance(int d) {
        _takeoffDistance = Math.max(0, d);
    }
    
    /**
     * Updates the airspeed at takeoff.
     * @param s the airspeed in knots
     * @see FDRFlightReport#getTakeoffSpeed()
     */
    public void setTakeoffSpeed(int s) {
        _takeoffSpeed = Math.max(0, s);
    }

    /**
     * Updates the aircraft weight at takeoff.
     * @param w the weight in pounds
     * @see FDRFlightReport#getTakeoffWeight()
     */
    public void setTakeoffWeight(int w) {
        _takeoffWeight = Math.max(0, w);
    }
    
    /**
     * Updates the amount of fuel at takeoff. 
     * @param f the amount of fuel in pounds
     * @see FDRFlightReport#getTakeoffFuel()
     */
    public void setTakeoffFuel(int f) {
        _takeoffFuel = Math.max(0, f);
    }

    /**
     * Updates the heading at takeoff.
     * @param hdg the takeoff heading in degrees
     * @see FDRFlightReport#getTakeoffHeading()
     */
    public void setTakeoffHeading(int hdg) {
    	_takeoffHdg = hdg;
    }
    
    /**
     * Updates the average N1 of the engines at takeoff.
     * @param n1 the average N1, multiplied by 100
     * @see ACARSFlightReport#getTakeoffN1()
     */
    public void setTakeoffN1(double n1) {
    	if (!Double.isNaN(n1))
    		_takeoffN1 = Math.max(0, n1);
    }

    /**
     * Updates the position at takeoff.
     * @param loc the takeoff location
     * @see FDRFlightReport#getTakeoffLocation()
     */
    public void setTakeoffLocation(GeospaceLocation loc) {
    	if (loc != null)
    		_takeoffPos = loc;
    }
    
    /**
     * Updates the landing date/time.
     * @param dt the date/time the aircraft touched down
     * @see FDRFlightReport#getLandingTime()
     */
    public void setLandingTime(Instant dt) {
        _stateChangeTimes.put(StateChange.LAND, dt);
    }
    
    /**
     * Updates the distance from the destination airport where touchdown occured.
     * @param d the distance in miles
     * @see FDRFlightReport#getLandingDistance()
     */
    public void setLandingDistance(int d) {
        _landingDistance = Math.max(0, d);
    }
    
    /**
     * Updates the airspeed at touchdown.
     * @param s the airspeed in knots
     * @see FDRFlightReport#getLandingSpeed()
     */
    public void setLandingSpeed(int s) {
        _landingSpeed = Math.max(0, s);
    }

    /**
     * Updates the vertical speed at touchdown.
     * @param s the vertical speed in feet per minute
     * @see FDRFlightReport#getLandingVSpeed()
     */
    public void setLandingVSpeed(int s) {
        _landingVspeed = (s == 0) ? -1 : ((s > 0) ? -s : s);
    }
    
    /**
     * Updates the weight of the aircraft at touchdown.
     * @param w the weight in pounds
     * @see FDRFlightReport#getLandingWeight()
     */
    public void setLandingWeight(int w) {
        _landingWeight = Math.max(0, w);
    }
    
    /**
     * Updates the amount of fuel at touchdown.
     * @param f the amount of fuel in pounds
     * @see FDRFlightReport#getLandingFuel()
     */
    public void setLandingFuel(int f) {
        _landingFuel = Math.max(0, f);
    }
    
    /**
     * Updates the heading at touchdown.
     * @param hdg the touchdown heading in degrees
     * @see FDRFlightReport#getLandingHeading() 
     */
    public void setLandingHeading(int hdg) {
    	_landingHdg = hdg;
    }
    
    /**
     * Updates the average N1 speed of the engines at touchdown.
     * @param n1 the average N1 speed, multiplied by 100
     * @see ACARSFlightReport#getLandingN1()
     */
    public void setLandingN1(double n1) {
    	if (!Double.isNaN(n1))
    		_landingN1 = Math.max(0, n1);
    }
    
    /**
     * Updates the position at touchdown.
     * @param loc the position
     * @see FDRFlightReport#getLandingLocation()
     */
    public void setLandingLocation(GeospaceLocation loc) {
    	if (loc != null)
    		_landingPos = loc;
    }

    /**
     * Updates the end time of the flight.
     * @param dt the date/time the flight ended
     * @see FDRFlightReport#getEndTime()
     */
    public void setEndTime(Instant dt) {
        _stateChangeTimes.put(StateChange.END, dt);
    }
    
    /**
     * Updates the weight of the aircraft at the end of the flight. 
     * @param w the weight in pounds
     * @see FDRFlightReport#getGateWeight()
     */
    public void setGateWeight(int w) {
        _gateWeight = Math.max(0, w);
    }
    
    /**
     * Updates the amount of fuel at the end of the flight.
     * @param f the amount of fuel in pounds
     * @see FDRFlightReport#getGateFuel()
     */
    public void setGateFuel(int f) {
        _gateFuel = Math.max(0, f);
    }
    
    /**
     * Updates the total amount of fuel burned during the flight.
     * @param f the amount of fuel in punds
     * @see FDRFlightReport#getTotalFuel()
     */
    public void setTotalFuel(int f) {
    	_totalFuel = Math.max(0, f);
    }
}