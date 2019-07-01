// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2014, 2016, 2017, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.flight.Recorder;
import org.deltava.beans.schedule.*;

import org.deltava.util.StringUtils;

/**
 * A bean to store a snapshot of an ACARS-logged flight.
 * @author Luke
 * @version 8.6
 * @since 1.0
 */

public class MapRouteEntry extends ACARSRouteEntry implements TabbedMapEntry {
	
	private String _eqType;
	private String _flightNumber;
	private Airport _airportD;
	private Airport _airportA;
	private OnlineNetwork _network;
	private Simulator _sim;
	private boolean _checkRide;
	private boolean _dispatchRoute;
	private String _phaseName;
	private Recorder _fdr = Recorder.ACARS;
	
	private String _externalID;
	
	private Country _c;
	private String _tailCode;
	
	private double _lf = -1;
	private int _pax;

	/**
	 * Creates the bean.
	 * @param dt the date/time
	 * @param gl the aircraft location
	 * @param usr the Pilot
	 * @param eqType the aircraft type
	 */
	public MapRouteEntry(Instant dt, GeoLocation gl, Pilot usr, String eqType) {
		super(dt, gl);
		_usr = usr;
		_eqType = eqType;
	}
	
	/**
	 * Returns the external flight ID, used by clients such as simFDR.
	 * @return the ID
	 */
	public String getExternalID() {
		return _externalID;
	}

	/**
	 * Updates whether the pilot is flying a check ride.
	 * @param isCR TRUE if a check ride, otherwise FALSE
	 */
	public void setCheckRide(boolean isCR) {
		_checkRide = isCR;
	}
	
	/**
	 * Updates whether the pilot is flying using a Dispatch-generated flight route.
	 * @param isDP TRUE if a dispatched plan, otherwise FALSE
	 */
	public void setDispatchPlan(boolean isDP) {
		_dispatchRoute = isDP;
	}

	/**
	 * Updates the flight code.
	 * @param flightNumber the flight code
	 */
	public void setFlightNumber(String flightNumber) {
		_flightNumber = flightNumber;
	}

	/**
	 * Updates the departure Airport.
	 * @param a the Airport
	 */
	public void setAirportD(Airport a) {
		_airportD = a;
	}
	
	/**
	 * Updates the arrival Airport.
	 * @param a the Airport
	 */
	public void setAirportA(Airport a) {
		_airportA = a;
	}
	
	/**
	 * Updates the online network used for this flight.
	 * @param network the OnlineNetwork
	 */
	public void setNetwork(OnlineNetwork network) {
		_network = network;
	}
	
	/**
	 * Updates the flight's flight phase. 
	 * @param phase the phase name
	 */
	public void setPhaseName(String phase) {
		_phaseName = phase;
	}
	
	/**
	 * Updates the Flight Data Recorder used to generate this entry.
	 * @param fdr a Recorder
	 */
	public void setRecorder(Recorder fdr) {
		_fdr = fdr;
	}
	
	/**
	 * Updates the load factor for this flight.
	 * @param lf the load factor
	 */
	public void setLoadFactor(double lf) {
		_lf = lf;
	}
	
	/**
	 * Updates the passenger count for this flight.
	 * @param pax the passenger count
	 */
	public void setPassengers(int pax) {
		_pax = Math.max(0, pax);
	}
	
	/**
	 * Updates the Simulator used on this flight.
	 * @param s the Simulator
	 */
	public void setSimulator(Simulator s) {
		_sim = s;
	}
	
	/**
	 * Updates what Country's airspace this entry is in.
	 * @param c a Country
	 */
	public void setCountry(Country c) {
		_c = c;
	}
	
	/**
	 * Updates the aircraft's tail code.
	 * @param tailCode the tail code
	 */
	public void setTailCode(String tailCode) {
		_tailCode = tailCode;
	}

	/**
	 * Sets the external flight ID, for use by third-party clients such as simFDR.
	 * @param id the ID
	 */
	public void setExternalID(String id) {
		_externalID = id;
	}

	@Override
	public final String getIconColor() {
		if (isFlagSet(ACARSFlags.PAUSED) || isWarning())
			return RED;
		else if (isFlagSet(ACARSFlags.ONGROUND))
			return WHITE;
		else if (getVerticalSpeed() > 100)
			return ORANGE;
		else if (getVerticalSpeed() < -100)
			return YELLOW;
		else 
			return BLUE;
	}
	
	@Override
	public final String getInfoBox() {
		StringBuilder buf = new StringBuilder(_usr.getRank().getName());
		buf.append(", ");
		buf.append(_usr.getEquipmentType());
		buf.append("<br />");
		if (!StringUtils.isEmpty(_flightNumber)) {
			buf.append("<br />Flight <span class=\"bld\">");
			buf.append(_flightNumber);
			buf.append("</span> - <span class=\"sec bld\">");
		}

		buf.append(_eqType);
		buf.append("</span> (Build ");
		buf.append(_clientBuild);
		if (_betaBuild > 0) {
			buf.append(" Beta ");
			buf.append(_betaBuild);
		}
		
		buf.append(")<br />From: ");
		buf.append(_airportD.getName());
		buf.append(" (");
		buf.append(_airportD.getICAO());
		buf.append(")<br />To: ");
		buf.append(_airportA.getName());
		buf.append(" (");
		buf.append(_airportA.getICAO());
		buf.append(")<br />");
		buf.append("Using ");
		buf.append(_sim.getName());
		if (_fdr != Recorder.ACARS) {
			buf.append(" and <span class=\"ter\">");
			buf.append(_fdr.toString());
			buf.append("</span>");
		}
		
		buf.append("<br />");
		if (_network != null) {
			buf.append("Flight operated online using <span class=\"sec bld\">");
			buf.append(_network.toString());
			buf.append("</span><br />");
		}
		
		buf.append("<span class=\"small\">Currently in ");
		buf.append((_c == null) ? "International" : _c.getName());
		buf.append(" airspace</span><br />");
		buf.append("<br />");
		buf.append(super.getInfoBox());
		return buf.toString();
	}

	@Override
	public final LinkedHashMap<String, String> getTabs() {
		LinkedHashMap<String, String> results = new LinkedHashMap<String, String>();

		// Build Pilot information
		StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox acarsFlight\"><span class=\"pri bld\">");
		buf.append(_usr.getName());
		buf.append("</span>");
		if (!StringUtils.isEmpty(_usr.getPilotCode())) {
			buf.append(" (");
			buf.append(_usr.getPilotCode());
			buf.append(')');
		}
		
		buf.append("<br />");
		buf.append(_usr.getRank());
		buf.append(", ");
		buf.append(_usr.getEquipmentType());
		buf.append("<br />");
		if (!StringUtils.isEmpty(_flightNumber)) {
			buf.append("<br />Flight <span class=\"bld\">");
			buf.append(_flightNumber);
			buf.append("</span> - <span class=\"sec bld\">");
		}

		buf.append(_eqType);
		buf.append("</span> (Build ");
		buf.append(String.valueOf(_clientBuild));
		if (_betaBuild > 0) {
			buf.append(" Beta ");
			buf.append(_betaBuild);
		}
		
		buf.append(")<br />From: ");
		buf.append(_airportD.getName());
		buf.append(" (");
		buf.append(_airportD.getICAO());
		buf.append(")<br />To: ");
		buf.append(_airportA.getName());
		buf.append(" (");
		buf.append(_airportA.getICAO());
		buf.append(")<br />Using <span class=\"pri\">");
		buf.append(_sim.getName());
		
		if (_fdr != Recorder.ACARS) {
			buf.append("</span> and <span class=\"ter\">");
			buf.append(_fdr.toString());
			buf.append("</span><br />");
		} else {
			buf.append("</span><br /><br />ACARS Flight <span class=\"bld\">");
			buf.append(StringUtils.format(getID(), "#,##0"));
			buf.append("</span>");
			if (!StringUtils.isEmpty(_tailCode)) {
				buf.append(" <span class=\"small\">(a/c ");
				buf.append(_tailCode);
				buf.append(")</span>");
			}
		}
		
		if (_pax > 0) {
			buf.append("<br /><span class=\"small\">Carrying <span class=\"ter bld\">");
			buf.append(_pax);
			buf.append("</span> passengers (");
			buf.append(StringUtils.format(_lf, "##0.0%"));
			buf.append(" full)</span>");
		}
		
		if (_network != null) {
			buf.append("<br />Flight operated online using <span class=\"sec bld\">");
			buf.append(_network.toString());
			buf.append("</span>");
		}
		if (isFlagSet(ACARSFlags.ONGROUND)) {
			buf.append("<br />Flight Phase: <span class=\"bld\">");
			buf.append(_phaseName);
			if (getGroundOperations() > 0) {
				buf.append(" (");
				for (GroundOperations op : GroundOperations.values()) {
					if (op.has(getGroundOperations()))
						buf.append(op.getDescription()).append(' ');
				}
				
				buf.setLength(buf.length() - 1);
				buf.append(')');
			}
			
			buf.append("</span>");
		} else {
			buf.append("<br /><span class=\"small\">Currently in ");
			buf.append((_c == null) ? "International" : _c.getName());
			buf.append(" airspace</span><br />");
		}
		
		boolean sterileCockpit = (!_busy && ((getRadarAltitude() < 5000) || (getAltitude() < 10000)));
		if (_checkRide || _dispatchRoute || _busy || sterileCockpit) {
			buf.append("<br />");
			if (_checkRide) buf.append("<span class=\"pri bld\">CHECK RIDE</span> ");
			if (_dispatchRoute) buf.append("<span class=\"sec bld\">USING DISPATCH</span> ");
			if (_busy) buf.append("<span class=\"error bld\">BUSY</span> ");
			if (sterileCockpit) buf.append("<span class=\"ter bld\">STERILE COCKPIT</span>");
		}
		
		buf.append("</div>");
		results.put("Pilot", buf.toString());
		
		// Add Flight information
		results.put("Flight Data", super.getInfoBox());
		return results;
	}
}