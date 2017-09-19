// Copyright 2012, 2013, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.*;

import org.deltava.beans.GeoLocation;

import org.deltava.util.GeoUtils;

/**
 * A bean to store runway selection results. 
 * @author Luke
 * @version 8.0
 * @since 4.2
 */

public class LandingRunways {
	
	private final GeoLocation _loc;
	private final int _hdg;
	private double _magVar;
	
	private final List<PossibleRunway> _rwys = new ArrayList<PossibleRunway>();
	
	public class PossibleRunway implements Comparable<PossibleRunway> {

		private final Runway _r;
		private final double _hdgDiff;
		private final double _brgDiff;
		private final double _xBrgDiff;
		private final double _brg;
		
		PossibleRunway(Runway r, double hdgDiff, double brgDiff, double brg) {
			super();
			_r = r;
			_hdgDiff = hdgDiff;
			_brgDiff = brgDiff;
			_xBrgDiff = Math.abs(Math.sin(Math.toRadians(brgDiff)));
			_brg = brg;
		}
		
		public Runway getRunway() {
			return _r;
		}
		
		public double getBearing() {
			return _brg;
		}
		
		public double getHeadingDelta() {
			return _hdgDiff;
		}
		
		public double getBearingDelta() {
			return _brgDiff;
		}
		
		public double getCrossBearingDelta() {
			return _xBrgDiff;
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(_r.getName());
			buf.append(",hD=");
			buf.append(_hdgDiff);
			buf.append(",bD=");
			buf.append(_brgDiff);
			buf.append(",xbD=");
			buf.append(_xBrgDiff);
			return buf.toString();
		}
		
		@Override
		public int hashCode() {
			return _r.hashCode();
		}
		
		@Override
		public int compareTo(PossibleRunway pr) {
			int tmpResult = Integer.compare((int) _hdgDiff / 45, (int) pr._hdgDiff / 45);
			if (tmpResult == 0)
				tmpResult = Double.valueOf(_xBrgDiff).compareTo(Double.valueOf(pr._xBrgDiff));
			return (tmpResult == 0) ? Double.valueOf(_brgDiff).compareTo(Double.valueOf(pr._brgDiff)) : tmpResult;
		}
	}

	/**
	 * Initializes the bean.
	 * @param loc the touchdown location
	 * @param hdg the true heading at touchdown
	 */
	public LandingRunways(GeoLocation loc, int hdg) {
		super();
		_loc = loc;
		_hdg = hdg;
	}

	/**
	 * Adds runway options.
	 * @param rwys a Collection of Runway beans
	 */
	public void addAll(Collection<Runway> rwys) {
		for (Runway r : rwys) {
			if ((Math.abs(r.getMagVar()) > _magVar) && (r.getMagVar() != 0.0)) _magVar = r.getMagVar();
			double bearing = GeoUtils.course(r, _loc);
			double hdgDiff = GeoUtils.delta(r.getHeading() + r.getMagVar(), _hdg);
			double brgDiff = GeoUtils.delta(r.getHeading(), bearing);
			_rwys.add(new PossibleRunway(r, hdgDiff, brgDiff, bearing));
		}
		
		Collections.sort(_rwys);
	}
	
	/**
	 * Returns the most likely runway used to land.
	 * @return a Runway, or null if no choices
	 */
	public Runway getBestRunway() {
		return _rwys.isEmpty() ? null : _rwys.get(0).getRunway();
	}
	
	/**
	 * Returns the greatest magnetic variation of all the runways.
	 * @return the variation in degrees
	 */
	public double getMagVar() {
		return _magVar;
	}
	
	/**
	 * Returns all possible Runways.
	 * @return a Collection of PossibleRunway beans
	 * @see PossibleRunway
	 */
	public Collection<PossibleRunway> getRunways() {
		return new ArrayList<PossibleRunway>(_rwys);
	}

	@Override
	public String toString() {
		return _rwys.toString();
	}
}