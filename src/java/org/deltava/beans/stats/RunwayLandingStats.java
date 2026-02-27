// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.schedule.Airport;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to track landing statistics for a particular runway. 
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public class RunwayLandingStats implements Cacheable, Comparable<RunwayLandingStats> {
	
	private final Airport _a;
	private final String _runway;
	private final int _year;
	private int _cnt;
	
	private double _avgScore;
	private double _scoreSD;
	
	private int _avgDistance;
	private int _distSD;
	
	private int _avgVS;
	private int _vsSD;
	
	/**
	 * Creates the bean.
	 * @param a the Airport
	 * @param rw the Runway name
	 * @param year the year, or zero if aggregated
	 */
	public RunwayLandingStats(Airport a, String rw, int year) {
		super();
		_a = a;
		_runway = rw;
		_year = year;
	}

	/**
	 * Returns the Airport.
	 * @return the Airport
	 */
	public Airport getAirport() {
		return _a;
	}
	
	/**
	 * Returns the Runway name.
	 * @return the name
	 */
	public String getRunway() {
		return _runway;
	}
	
	/**
	 * Returns the year, or zero if aggregated.
	 * @return the year
	 */
	public int getYear() {
		return _year;
	}
	
	/**
	 * Returns the average landing score.
	 * @return the score
	 */
	public double getAverageScore() {
		return _avgScore;
	}
	
	/**
	 * Returns the landing score standard deviation size.
	 * @return the standard deviation
	 */
	public double getScoreSD() {
		return _scoreSD;
	}
	
	/**
	 * Returns the average touchdown distance from the runway threshold.
	 * @return the average ditsance in feet
	 */
	public int getAverageDistance() {
		return _avgDistance;
	}
	
	/**
	 * Returns the touchdown distance standard deviation size.
	 * @return the standard deviation
	 */
	public int getDistanceSD() {
		return _distSD;
	}
	
	/**
	 * Returns the average touchdown vertical speed.
	 * @return the vertical speed in feet per minute
	 */
	public int getAverageVerticalSpeed() {
		return _avgVS;
	}
	
	/**
	 * Returns the touchdown vertical speed standard deviation size.
	 * @return the standard deviation
	 */
	public int getVerticalSpeedSD() {
		return _vsSD;
	}

	/**
	 * Returns the number of scored landings.
	 * @return the number of landings
	 */
	public int getCount() {
		return _cnt;
	}

	/**
	 * Updates the average landing score and standard deviation.
	 * @param avg the average score
	 * @param sd the score standard deviation
	 */
	public void setScore(double avg, double sd) {
		_avgScore = avg;
		_scoreSD = sd;
	}
	
	/**
	 * Updates the average touchdown distance and standard deviation.
	 * @param avg the average distance from the threshold in feet
	 * @param sd the standard deviation
	 */
	public void setDistance(int avg, int sd) {
		_avgDistance = avg;
		_distSD = sd;
	}
	
	/**
	 * Updates the average vertical speed at touchdown and standard deviation.
	 * @param avg the average vertical speed in feet per minute
	 * @param sd the standard deviation
	 */
	public void setVerticalSpeed(int avg, int sd) {
		_avgVS = avg;
		_vsSD = sd;
	}
	
	/**
	 * Updates the number of scored landings.
	 * @param cnt the number of landings
	 */
	public void setCount(int cnt) {
		_cnt = cnt;
	}
	
	/**
	 * Merges two beans, claculcating combined means and standard deviations.
	 * @param rsw a RunwayLandingStats bean
	 * @return a combined RunwayLandingStats bean
	 */
	public RunwayLandingStats merge(RunwayLandingStats rsw) {
		if ((rsw == null) || (rsw.getCount() == 0)) return this;
		
		// Calculate aggregate means
		final int d = (_cnt + rsw._cnt); final double dd = d;
		double agmS = ((_cnt * _avgScore) + (rsw._cnt * rsw._avgScore)) / dd;
		double agmD = ((_cnt * _avgDistance) + (rsw._cnt * rsw._avgDistance)) / dd;
		double agmVS = ((_cnt * _avgVS) * (rsw._cnt * rsw._avgVS)) / dd;
		
		// Calculate within-group variances
		double sv1 = (_cnt * _scoreSD * _scoreSD) + (rsw._cnt * rsw._scoreSD * rsw._scoreSD);
		double dv1 = (_cnt * _distSD * _distSD) + (rsw._cnt * rsw._distSD * rsw._distSD);
		double vv1 = (_cnt * _vsSD * _vsSD) + (rsw._cnt * rsw._vsSD * rsw._vsSD);
		
		// Calculate between-group variances
		final double d2 = (_cnt * rsw._cnt) / dd;
		double sv2 = _avgScore - rsw._avgScore;
		double dv2 = _avgDistance - rsw._avgDistance;
		double vv2 = _avgVS - rsw._avgVS;
		
		// Calculate combined variance
		double scv = (sv1 + (sv2 * sv2 * d2)) / dd;
		double dcv = (dv1 + (dv2 * dv2 * d2)) / dd;
		double vcv = (vv1 + (vv2 * vv2 * d2)) / dd;
		
		// Return the new bean
		RunwayLandingStats rsw2 = new RunwayLandingStats(rsw.getAirport(), rsw.getRunway(), 0);
		rsw2.setCount(d);
		rsw2.setScore(agmS, Math.sqrt(scv));
		rsw2.setDistance((int) agmD, (int) Math.round(Math.sqrt(dcv)));
		rsw2.setVerticalSpeed((int) agmVS, (int) Math.round(Math.sqrt(vcv)));
		return rsw2;
	}
	
	@Override
	public int compareTo(RunwayLandingStats rsw) {
		int tmpResult = _a.compareTo(rsw._a);
		tmpResult = (tmpResult == 0) ? _runway.compareTo(rsw._runway) : tmpResult;
		return (tmpResult == 0) ? Integer.compare(_year, rsw._year) : tmpResult;
	}
	
	@Override
	public String toString() {
		return String.format("%s-%s", _a.getICAO(), _runway);
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public Object cacheKey() {
		return toString();
	}
}