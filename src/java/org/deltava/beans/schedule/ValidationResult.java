// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * A bean to store raw schedule entry validation results.
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class ValidationResult {
	
	private boolean _isPolicy;
	private boolean _isDuplicate;
	private boolean _isTime;
	private boolean _isAircraft;
	
	private int _rangeMin;
	private int _rangeMax;

	public boolean getIsDuplicate() {
		return _isDuplicate;
	}
	
	public boolean getIsTime() {
		return _isTime;
	}
	
	public boolean getIsPolicy() {
		return _isPolicy;
	}
	
	public boolean getIsAircraft() {
		return _isAircraft;
	}
	
	public boolean getIsOK() {
		return !(_isDuplicate || _isTime || _isAircraft || _isPolicy);
	}
	
	public int getRangeMin() {
		return _rangeMin;
	}
	
	public int getRangeMax() {
		return _rangeMax;
	}

	public void setRange(int min, int max) {
		_rangeMin = min;
		_rangeMax = max;
	}
	
	public void setDuplicate(boolean isWarn) {
		_isDuplicate = isWarn;
	}
	
	public void setTime(boolean isWarn) {
		_isTime = isWarn;
	}
	
	public void setAircraft(boolean isWarn) {
		_isAircraft = isWarn;
	}
	
	public void setPolicy(boolean isWarn) {
		_isPolicy = isWarn;
	}
	
	public String getMessage() {
		StringBuilder buf = new StringBuilder();
		if (_isDuplicate) buf.append("DUPLICATE ");
		if (_isPolicy) buf.append("POLICY ");
		if (_isTime) buf.append("TIME ");
		if (_isAircraft) buf.append("AIRCRAFT");
		return buf.toString().trim();
	}
}