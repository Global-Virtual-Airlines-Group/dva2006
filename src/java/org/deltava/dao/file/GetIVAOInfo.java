// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;

import org.json.*;
import org.apache.log4j.Logger;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.servinfo.*;

import org.deltava.dao.DAOException;

import org.deltava.util.*;

/**
 * A Data Access Object to parse an IVAO v2 JSON servinfo feed. 
 * @author Luke
 * @version 10.1
 * @since 10.1
 */

public class GetIVAOInfo extends OnlineNetworkDAO {
	
	private static final Logger log = Logger.getLogger(GetIVAOInfo.class);
	
	/**
	 * Creates the Data Access Object.
	 * @param is the InputStream to read
	 */
	public GetIVAOInfo(InputStream is) {
		super(is);
	}
	
	private static Controller parseController(JSONObject co) {
		int id = 0;
		try {
			Controller c = new Controller(co.getInt("userId"), OnlineNetwork.IVAO);
			id = c.getID();
			c.setName(co.optString("name", "?"));
			c.setServer(co.getString("serverId"));
			c.setCallsign(co.getString("callsign"));
			c.setLoginTime(parseDateTime(co.getString("createdAt")));
			JSONObject lto = co.optJSONObject("lastTrack");
			JSONObject ato = co.optJSONObject("atcSession");
			if ((lto != null) && (ato != null)) {
				RadioPosition rp = new RadioPosition(c.getCallsign(), 1, StringUtils.format(ato.getDouble("frequency"), "##0.00"));
				rp.setPosition(lto.optDouble("latitude"), lto.optDouble("longitude"), 0);
				c.addPosition(rp);
				c.setRange(lto.optInt("distance", 50));
				c.setFacility(EnumUtils.parse(Facility.class, ato.optString("position", "TWR"), Facility.TWR));
			}
			
			return c;
		} catch (Exception e) {
			log.error("Error parsing controller " + id + " - " + e.getMessage());
			return null;
		}		
	}
	
	private static Pilot parsePilot(JSONObject po) {
		int id = 0;
		try {
			Pilot p = new Pilot(po.getInt("userId"), OnlineNetwork.IVAO);
			id = p.getID();
			p.setName(po.optString("name", "?"));
			p.setServer(po.getString("serverId"));
			p.setCallsign(po.getString("callsign"));
			p.setLoginTime(parseDateTime(po.getString("createdAt")));
			JSONObject lto = po.optJSONObject("lastTrack");
			if (lto != null) {
				p.setAltitude(lto.optInt("altitude"));
				p.setGroundSpeed(lto.optInt("groundSpeed"));
				p.setHeading(lto.optInt("heading"));
				p.setPosition(lto.optDouble("latitude"), lto.optDouble("longitude"));
			}
			
			JSONObject fpo = po.optJSONObject("flightPlan");
			if (fpo != null) {
				p.setAirportD(getAirport(fpo.optString("departureId")));
				p.setAirportA(getAirport(fpo.optString("arrivalId")));
				p.setRoute(fpo.optString("route"));
				p.setComments(fpo.optString("remarks"));
				p.setEquipmentCode(fpo.optString("aircraftId"));
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
		
		// Parse the servers
		NetworkInfo info = new NetworkInfo(OnlineNetwork.IVAO);
		info.setVersion(2);
		info.setValidDate(parseDateTime(jo.getString("updatedAt")));
		JSONArray sa = jo.getJSONArray("servers");
		for (int x = 0; x < sa.length(); x++) {
			JSONObject so = sa.getJSONObject(x);
			Server srv = new Server(so.getString("id"));
			srv.setAddress(so.getString("hostname"));
			srv.setComment(so.optString("description"));
			srv.setConnections(so.optInt("currentConnections"));
			srv.setConnectionsAllowed(so.optInt("maximumConnections", 0) > srv.getConnections());
			info.add(srv);
		}

		// Parse the pilots
		JSONObject clo = jo.getJSONObject("clients");
		JSONArray pa = clo.getJSONArray("pilots");
		for (int x = 0; x < pa.length(); x++) {
			JSONObject po = pa.getJSONObject(x);
			info.add(parsePilot(po));
		}

		// Parse the controllers
		JSONArray ca = clo.getJSONArray("atcs");
		for (int x = 0; x < ca.length(); x++) {
			JSONObject co = ca.getJSONObject(x);
			info.add(parseController(co));
		}

		return info;
	}
}