// Copyright 2008, 2010, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import org.deltava.beans.*;
import org.deltava.beans.schedule.GeoPosition;

/**
 * An abstract class to describe ACARS map entries.
 * @author Luke
 * @version 7.2
 * @since 2.2
 */

public abstract class ACARSMapEntry extends DatabaseBean implements MarkerMapEntry {
	
	/**
	 * Map entry types.
	 */
	public enum EntryType {
		AIRCRAFT, DISPATCH, ATC
	}
	
	protected GeoLocation _pos;
	protected Pilot _usr;
	protected int _clientBuild;
	protected int _betaBuild;
	protected boolean _busy;

	/**
	 * Creates the map entry.
	 * @param loc the entry location
	 */
	protected ACARSMapEntry(GeoLocation loc) {
		super();
		_pos = (loc == null) ? new GeoPosition(0, 0) : loc;
	}
	
	/**
	 * Returns the entry's position.
	 * @return the position
	 */
	public GeoLocation getLocation() {
		return _pos;
	}
	
	@Override
	public final double getLatitude() {
		return _pos.getLatitude();
	}

	@Override
	public final double getLongitude() {
		return _pos.getLongitude();
	}
	
	/**
	 * Returns the Pilot.
	 * @return the Pilot bean, or null if not populated
	 */
	public Pilot getPilot() {
		return _usr;
	}
	
	/**
	 * Returns whether the user is marked Busy.
	 * @return TRUE if Busy, otherwise FALSE
	 */
	public boolean isBusy() {
		return _busy;
	}
	
	/**
	 * Returns the map entry type.
	 * @return the EntryType
	 */
	public abstract EntryType getType();
	
	/**
	 * Sets the ACARS client version used.
	 * @param buildNumber the client build number
	 * @param beta the beta version number
	 */
	public void setClientBuild(int buildNumber, int beta) {
		_clientBuild = buildNumber;
		_betaBuild = Math.max(0, beta);
	}

	/**
	 * Marks the user as Busy.
	 * @param isBusy TRUE if Busy, otherwise FALSE
	 */
	public void setBusy(boolean isBusy) {
		_busy = isBusy;
	}
}