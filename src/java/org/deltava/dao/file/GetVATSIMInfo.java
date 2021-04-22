// Copyright 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;
import java.time.Instant;

import org.json.*;
import org.apache.log4j.Logger;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.servinfo.*;

import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Data Access Object to read the VATSIM JSON data feed.
 * @author Luke
 * @version 9.2
 * @since 9.0
 */

public class GetVATSIMInfo extends DAO implements OnlineNetworkDAO {

	private static final Logger log = Logger.getLogger(GetVATSIMInfo.class);
	
	private static final String DATE_FMT = "yyyy-MM-dd'T'HH:mm:ss";
	
	/**
	 * Creates the Data Access Object.
	 * @param is the InputStream to read
	 */
	public GetVATSIMInfo(InputStream is) {
		super(is);
	}
	
	private static Instant parseDateTime(String dt) {
		int pos = dt.indexOf('.');
		String dt2 = (pos > -1) ? dt.substring(0, pos) : dt;
		return StringUtils.parseInstant(dt2, DATE_FMT);
	}

	private static Airport getAirport(String airportCode) {
		Airport a = SystemData.getAirport(airportCode);
		return (a == null) ? new Airport(airportCode, airportCode, airportCode) : a;
	}
	
	private static Controller parseController(JSONObject co) {
		int id = 0;
		try {
			Controller c = new Controller(co.getInt("cid"), OnlineNetwork.VATSIM);
			id = c.getID();
			c.setName(co.getString("name"));
			c.setServer(co.getString("server"));
			c.setCallsign(co.getString("callsign"));
			c.setRating(Rating.values()[co.optInt("rating", Rating.OBS.ordinal())]);
			c.setLoginTime(parseDateTime(co.getString("logon_time")));
			c.setRange(co.optInt("visual_range"));
			c.setFacility(StringUtils.isEmpty(co.optString("atis_code"))  ? Facility.values()[co.optInt("facility", Facility.ATIS.ordinal())] : Facility.ATIS);
			return c;
		} catch (Exception e) {
			log.error("Error parsing controller " + id + " - " + e.getMessage());
			return null;
		}
	}
	
	private static Pilot parsePilot(JSONObject po) {
		int id = 0;
		try {
			Pilot p = new Pilot(po.getInt("cid"), OnlineNetwork.VATSIM);
			p.setName(po.getString("name"));
			p.setServer(po.getString("server"));
			p.setCallsign(po.getString("callsign"));
			p.setAltitude(po.getInt("altitude"));
			p.setGroundSpeed(po.getInt("groundspeed"));
			p.setHeading(po.getInt("heading"));
			p.setPosition(po.getDouble("latitude"), po.getDouble("longitude"));
			p.setLoginTime(parseDateTime(po.getString("logon_time")));
			JSONObject fpo = po.optJSONObject("flight_plan");
			if (fpo != null) {
				p.setAirportD(getAirport(fpo.optString("departure")));
				p.setAirportA(getAirport(fpo.optString("arrival")));	
				p.setEquipmentCode(fpo.optString("aircraft"));
				p.setRoute(po.optString("route", ""));
				p.setComments(po.optString("remarks", ""));
			}
			
			return p;
		} catch (Exception e) {
			log.error("Error parsing pilot " + id + " - " + e.getMessage());
			return null;
		}
	}
	
	@Override
	public NetworkInfo getInfo() throws DAOException {

		JSONObject jo = null;
		try (InputStream is = getStream()) {
			jo = new JSONObject(new JSONTokener(is));
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
		
		// Check that it's the right version
		JSONObject go = jo.getJSONObject("general");
		int v = go.optInt("version");
		if (v != 3)
			throw new IllegalArgumentException("Invalid VATSIM data feed version - " + v);

		// Parse the servers
		NetworkInfo info = new NetworkInfo(OnlineNetwork.VATSIM);
		info.setVersion(v);
		info.setValidDate(parseDateTime(go.getString("update_timestamp")));
		Map<String, Server> results = new TreeMap<String, Server>();
		JSONArray sa = jo.getJSONArray("servers");
		for (int x = 0; x < sa.length(); x++) {
			JSONObject so = sa.getJSONObject(x);
			Server srv = new Server(so.getString("name"));
			srv.setAddress(so.getString("hostname_or_ip"));
			srv.setLocation(so.optString("location", "?"));
			srv.setConnectionsAllowed(so.optBoolean("clients_connection_allowed", true));
			results.put(srv.getName(), srv);
			info.add(srv);
		}
		
		// Parse the pilots
		JSONArray pa = jo.getJSONArray("pilots");
		for (int x = 0; x < pa.length(); x++) {
			JSONObject po = pa.getJSONObject(x);
			info.add(parsePilot(po));
		}

		// Parse the controllers
		JSONArray ca = jo.getJSONArray("controllers");
		for (int x = 0; x < ca.length(); x++) {
			JSONObject co = ca.getJSONObject(x);
			info.add(parseController(co));
		}
		
		// Parse the ATIS
		ca = jo.getJSONArray("atis");
		for (int x = 0; x < ca.length(); x++) {
			JSONObject co = ca.getJSONObject(x);
			info.add(parseController(co));
		}
		
		// Calculate connection counts
		info.getPilots().stream().map(p -> results.get(p.getServer())).filter(Objects::nonNull).forEach(s -> s.setConnections(s.getConnections() + 1));
		info.getControllers().stream().map(p -> results.get(p.getServer())).filter(Objects::nonNull).forEach(s -> s.setConnections(s.getConnections() + 1));
		return info;
	}
}