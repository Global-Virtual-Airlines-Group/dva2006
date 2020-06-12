// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 9.0
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

	@Override
	public NetworkInfo getInfo() throws DAOException {

		JSONObject jo = null;
		try (InputStream is = getStream()) {
			jo = new JSONObject(new JSONTokener(is));
		} catch (IOException ie) {
			throw new DAOException(ie);
		}

		// Parse the servers
		NetworkInfo info = new NetworkInfo(OnlineNetwork.VATSIM);
		info.setValidDate(parseDateTime(jo.getJSONObject("general").getString("update_timestamp")));
		Map<String, Server> results = new TreeMap<String, Server>();
		JSONArray sa = jo.getJSONArray("servers");
		for (int x = 0; x < sa.length(); x++) {
			JSONObject so = sa.getJSONObject(x);
			Server srv = new Server(so.getString("name"));
			srv.setAddress(so.getString("hostname_or_ip"));
			srv.setLocation(so.optString("location", "?"));
			results.put(srv.getName(), srv);
			info.add(srv);
		}

		// Parse the connections
		JSONArray ca = jo.getJSONArray("clients");
		for (int x = 0; x < ca.length(); x++) {
			JSONObject co = ca.getJSONObject(x);
			int id = StringUtils.parse(co.getString("cid"), 0);
			NetworkUser.Type t = NetworkUser.Type.valueOf(co.getString("clienttype"));
			if (t == NetworkUser.Type.RATING) continue;

			// Create the object
			ConnectedUser nt = null;
			try {
				switch (t) {
				case PILOT:
					Pilot p = new Pilot(id, OnlineNetwork.VATSIM);
					p.setCallsign(co.getString("callsign"));
					p.setAltitude(co.getInt("altitude"));
					p.setGroundSpeed(co.getInt("groundspeed"));
					p.setHeading(co.getInt("heading"));
					p.setRoute(co.optString("planned_route", ""));
					p.setComments(co.optString("planned_remarks", ""));
					p.setAirportD(getAirport(co.optString("planned_depairport")));
					p.setAirportA(getAirport(co.optString("planned_destairport")));
					info.add(p);
					nt = p;
					break;

				case ATC:
					Controller c = new Controller(id, OnlineNetwork.VATSIM);
					c.setCallsign(co.getString("callsign"));
					c.setRating(Rating.values()[co.getInt("rating")]);
					c.setFacility(Facility.values()[co.getInt("facilitytype")]);
					c.setFrequency(co.optString("frequency", Controller.OBS_FREQ));
					info.add(c);
					nt = c;
					break;

				default:
					continue;
				}

				nt.setLoginTime(parseDateTime(co.getString("time_logon")));
				nt.setPosition(co.getDouble("latitude"), co.getDouble("longitude"));
				nt.setName(co.getString("realname"));
				nt.setServer(co.getString("server"));
				Server srv = results.get(nt.getServer());
				if (srv != null)
					srv.setConnections(srv.getConnections() + 1);
			} catch (Exception e) {
				if (nt != null)
					log.error("Error loading " + nt.getCallsign() + " - " + e.getMessage(), e);
				else
					log.error(e.getMessage(), e);
				
				log.error(co.toString());
			}
		}

		return info;
	}
}