// Copyright 2021, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;

import org.json.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.servinfo.*;

import org.deltava.dao.DAOException;
import org.deltava.util.EnumUtils;

/**
 * A Data Access Object to parse a POSCON JSON servinfo feed. 
 * @author Luke
 * @version 11.0
 * @since 10.1
 */

public class GetPOSCONInfo extends OnlineNetworkDAO {
	
	private static final Logger log = LogManager.getLogger(GetPOSCONInfo.class);

	/**
	 * Creates the Data Access Object.
	 * @param is the InputStream to read
	 */
	public GetPOSCONInfo(InputStream is) {
		super(is);
	}
	
	private static Controller parseController(JSONObject co) {
		int id = 0;
		try {
			Controller c = new Controller(Integer.parseInt(co.getString("userId")), OnlineNetwork.POSCON);
			id = c.getID();
			c.setName(co.optString("userName", "?"));
			c.setCallsign(co.getString("position"));
			c.setLoginTime(parseDateTime(co.getString("login")));
			c.setFacility(EnumUtils.parse(Facility.class, co.getString("type"), Facility.CTR));
			JSONArray pa = co.optJSONArray("centerPoint");
			if ((pa != null) && (pa.length() > 1)) {
				RadioPosition rp = new RadioPosition(c.getCallsign(), 0, co.getString("vhfFreq"));
				rp.setPosition(pa.getDouble(0), pa.getDouble(1), 0);
				c.addPosition(rp);
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
			Pilot p = new Pilot(Integer.parseInt(po.getString("userId")), OnlineNetwork.POSCON);
			id = p.getID();
			p.setCallsign(po.getString("callsign"));
			p.setName(po.optString("userName", "?"));
			p.setLoginTime(parseDateTime(po.getString("login")));
			JSONObject lto = po.optJSONObject("position");
			if (lto != null) {
				p.setAltitude(lto.optInt("alt_amsl"));
				p.setGroundSpeed(lto.optInt("gs_kt"));
				p.setHeading(Math.round(lto.optFloat("true_hdg")));
				p.setPosition(lto.optDouble("lat"), lto.optDouble("long"));
			}
			
			JSONObject fpo = po.optJSONObject("flightPlan");
			if (fpo != null) {
				p.setAirportD(getAirport(fpo.optString("dep")));
				p.setAirportA(getAirport(fpo.optString("dest")));
				p.setRoute(fpo.optString("route"));
				p.setComments(fpo.optString("remarks"));
				p.setEquipmentCode(fpo.optString("ac_type"));
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
		NetworkInfo info = new NetworkInfo(OnlineNetwork.POSCON);
		info.setVersion(1);
		info.setValidDate(parseDateTime(jo.getString("lastUpdated")));
		
		// Parse the pilots
		JSONArray pa = jo.getJSONArray("flights");
		for (int x = 0; x < pa.length(); x++) {
			JSONObject po = pa.getJSONObject(x);
			info.add(parsePilot(po));
		}

		// Parse the controllers
		JSONArray ca = jo.getJSONArray("atc");
		for (int x = 0; x < ca.length(); x++) {
			JSONObject co = ca.getJSONObject(x);
			info.add(parseController(co));
		}
		
		return info;
	}
}