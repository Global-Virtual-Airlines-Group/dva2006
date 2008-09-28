// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;
import java.text.*;

import org.apache.log4j.Logger;

import org.deltava.beans.OnlineNetwork;
import org.deltava.beans.DateTime;
import org.deltava.beans.TZInfo;

import org.deltava.beans.servinfo.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Acces Object to fetch VATSIM/IVAO ServInfo data. ServInfo data for pilots and controllers is in a colon (:)
 * delimited format, with the following tokens: 00 callsign 01 cid 02 realname 03 clienttype 04 frequency 05latitude 06
 * longitude 07 altitude 08 groundspeed 09 planned_aircraft 10 planned_tascruise 11 planned_depairport 12
 * planned_altitude 13 planned_destairport 14 server 15 protrevision 16 rating 17 transponder 18 facilitytype 19
 * visualrange 20 planned_revision 21 planned_flighttype 22 planned_deptime 23 planned_actdeptime 24 planned_hrsenroute
 * 25 planned_minenroute 26 planned_hrsfuel 27 planned_minfuel 28 planned_altairport 29 planned_remarks 30 planned_route
 * 31 planned_depairport_lat 32 planned_depairport_lon 33 planned_destairport_lat 34 planned_destairport_lon 35
 * atis_message 36 time_last_atis_received 37 time_logon 38 heading 39 QNH_iHg 40 QNH_Mb
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class GetServInfo extends DAO {

	private static final Logger log = Logger.getLogger(GetServInfo.class);

	private static final Cache<NetworkStatus> _netCache = new ExpiringCache<NetworkStatus>(3, 43200); // 12 hours
	private static final ExpiringCache<NetworkInfo> _infoCache = new ExpiringCache<NetworkInfo>(3, 240); // 4 minutes
	
	private boolean _useCache = true;

	/**
	 * Initializes the DAO with a particular stream.
	 * @param is the stream to use
	 */
	public GetServInfo(InputStream is) {
		super(is);
	}

	/**
	 * Private helper class to parse ServInfo CLIENT data.
	 */
	class SITokens {

		public static final int CALLSIGN = 0;
		public static final int ID = 1;
		public static final int NAME = 2;
		public static final int TYPE = 3;
		public static final int FREQ = 4;
		public static final int LAT = 5;
		public static final int LON = 6;
		public static final int ALT = 7;
		public static final int GSPEED = 8;
		public static final int EQCODE = 9;
		public static final int PSPEED = 10;
		public static final int AIRPORT_D = 11;
		public static final int AIRPORT_A = 13;
		public static final int SERVER = 14;
		public static final int RATING = 16;
		public static final int SQCODE = 17;
		public static final int FACILITY = 18;
		public static final int IFRVFR = 21;
		public static final int REMARKS = 29;
		public static final int ROUTE = 30;
		public static final int DEP_LAT = 31;
		public static final int DEP_LON = 32;
		public static final int ARR_LAT = 33;
		public static final int ARR_LON = 34;

		private List<String> _tkns;

		public SITokens(String data) {
			super();
			_tkns = StringUtils.split(data, ":");
		}

		public String get(int idx) {
			return _tkns.get(idx);
		}

		public int size() {
			return _tkns.size();
		}
	}

	private Airport getAirport(String airportCode) {
		Airport a = SystemData.getAirport(airportCode);
		return (a == null) ? new Airport(airportCode, airportCode, airportCode) : a;
	}
	
	/**
	 * Optionally disables the use of the DAO cache.
	 * @param useCache TRUE if the cache should be used, otherwise FALSE
	 */
	public void setUseCache(boolean useCache) {
		_useCache = useCache;
	}
	
	/**
	 * Returns cached network status data, if present.
	 * @param net the network
	 * @return a NetworkStatus bean, or null if not cached
	 */
	public static synchronized NetworkStatus getCachedStatus(OnlineNetwork net) {
		NetworkStatus status =  _netCache.get(net);
		if (status != null)
			status.setCached();
		
		return status;
	}
	
	/**
	 * Returns cached network information data, if present.
	 * @param net the network
	 * @return a NetworkInfo bean, or null if not cached
	 */
	public static synchronized NetworkInfo getCachedInfo(OnlineNetwork net) {
		NetworkInfo info = _infoCache.get(net, true);
		if (info != null) {
			info.setCached();
			if (_infoCache.isExpired(net) && !info.getExpired())
				info.setExpired();
		}
		
		return info;
	}

	/**
	 * Loads network data URLs.
	 * @param net the network
	 * @return a Network Status bean
	 * @throws DAOException if an HTTP error occurs
	 */
	public NetworkStatus getStatus(OnlineNetwork net) throws DAOException {

		// Get from the cache if possible
		NetworkStatus status = _netCache.get(net);
		if ((status != null) && (_useCache)) {
			status.setCached();
			return status;
		}

		try {
			BufferedReader br = getReader();
			status = new NetworkStatus(net, SystemData.get("online." + net.toString().toLowerCase() + ".local.info"));
			String sData = br.readLine();
			while ((sData != null) && (!Thread.currentThread().isInterrupted())) {
				StringTokenizer tk = new StringTokenizer(sData, "=");
				if (tk.countTokens() > 1) {
					String param = tk.nextToken();
					if (("url0".equals(param)) && (tk.countTokens() > 0))
						status.addURL(tk.nextToken());
					else if (("msg0".equals(param)) && (tk.countTokens() > 0))
						status.setMessage(tk.nextToken());
				}

				// Read next line
				sData = br.readLine();
			}

			_netCache.add(status);
			return status;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}

	/**
	 * Loads network data.
	 * @param network the network
	 * @return a NetworkInfo bean
	 * @throws DAOException if an HTTP error occurs
	 */
	public NetworkInfo getInfo(OnlineNetwork network) throws DAOException {

		// Get from the cache if possible
		NetworkInfo info = _infoCache.get(network);
		if ((info != null) && (_useCache)) {
			info.setCached();
			return info;
		}
		
		try {
			LineNumberReader br = getReader();
			info = new NetworkInfo(network);

			// Initialize date formatter
			final SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

			String iData = br.readLine();
			while ((iData != null) && (!Thread.currentThread().isInterrupted())) {
				if ((iData.length() > 0) && (iData.charAt(0) == '!')) {
					String sectionName = iData.substring(1, 8).toUpperCase();
					log.debug("Loading Section " + sectionName);

					// Parse the general section
					if ("GENERAL".equalsIgnoreCase(sectionName)) {
						do {
							StringTokenizer tk = new StringTokenizer(iData, " = ");
							if (tk.countTokens() == 2) {
								String name = tk.nextToken();
								String val = tk.nextToken();
								if ("VERSION".equalsIgnoreCase(name)) {
									info.setVersion(val);
								} else if ("UPDATE".equalsIgnoreCase(name)) {
									try {
										info.setValidDate(df.parse(val));
									} catch (ParseException pe) {
										info.setValidDate(new Date());
									}

									// We get the update time in UTC, so convert to local
									DateTime dt = new DateTime(info.getValidDate(), TZInfo.UTC);
									dt.convertTo(TZInfo.local());
									info.setValidDate(dt.getDate());
									if (log.isDebugEnabled())
										log.debug("Valid as of " + dt.toString());
								}
							}

							// Get next line
							iData = br.readLine();
						} while ((iData != null) && (iData.indexOf(" = ") != -1));
						// Parse the pilot/ATC list
					} else if ("CLIENTS".equalsIgnoreCase(sectionName)) {
						iData = br.readLine();
						while ((iData != null) && (iData.length() > 3) && (iData.charAt(0) != '!')) {
							SITokens si = new SITokens(iData);
							int id = StringUtils.parse(si.get(SITokens.ID), 0);

							// Load the type
							switch (NetworkUser.getType(si.get(SITokens.TYPE))) {
								case NetworkUser.ATC:
									if (si.size() < SITokens.FACILITY)
										break;
									
									try {
										Controller c = new Controller(id);
										c.setCallsign(si.get(SITokens.CALLSIGN));
										c.setName(si.get(SITokens.NAME));
										c.setFrequency(si.get(SITokens.FREQ));
										c.setPosition(si.get(SITokens.LAT), si.get(SITokens.LON));
										c.setRating(Integer.parseInt(si.get(SITokens.RATING)));
										c.setFacilityType(Integer.parseInt(si.get(SITokens.FACILITY)));
										info.add(c);
									} catch (Exception e) {
										log.info("Error parsing data for " + si.get(SITokens.CALLSIGN) + " - " + e.getMessage());
										log.info(iData);
									}
									
									break;

								default:
								case NetworkUser.PILOT:
									if (si.size() < SITokens.ROUTE)
										break;
									
									try {
										Pilot p = new Pilot(id);
										p.setCallsign(si.get(SITokens.CALLSIGN));
										p.setName(si.get(SITokens.NAME));
										p.setAirportD(getAirport(si.get(SITokens.AIRPORT_D)));
										p.setAirportA(getAirport(si.get(SITokens.AIRPORT_A)));
										p.setPosition(si.get(SITokens.LAT), si.get(SITokens.LON));
										p.setEquipmentCode(si.get(SITokens.EQCODE));
										p.setAltitude(si.get(SITokens.ALT));
										p.setGroundSpeed(si.get(SITokens.GSPEED));
										p.setWayPoints(si.get(SITokens.ROUTE));

										// Set departure airport position if unknown
										if (p.getAirportD().getPosition() == null) {
											try {
												double lat = Double.parseDouble(si.get(SITokens.DEP_LAT));
												double lon = Double.parseDouble(si.get(SITokens.DEP_LON));
												p.getAirportD().setLocation(lat, lon);
											} catch (NumberFormatException nfe) {
												log.warn("Error pasing departure airport GeoPosition");
											}
										}

										// Set arrival airport position if unknown
										if (p.getAirportA().getPosition() == null) {
											try {
												double lat = Double.parseDouble(si.get(SITokens.DEP_LAT));
												double lon = Double.parseDouble(si.get(SITokens.DEP_LON));
												p.getAirportA().setLocation(lat, lon);
											} catch (NumberFormatException nfe) {
												log.warn("Error pasing departure airport GeoPosition");
											}
										}

										// Save raw data if we are calling this from the ServInfo web service
										p.setRawData(iData);

										// Add to results
										info.add(p);
									} catch (Exception e) {
										log.info("Error parsing data for " + si.get(SITokens.CALLSIGN) + " - " + e.getMessage());
									}
							}

							// Read next line
							iData = br.readLine();
						}
					} else
						iData = br.readLine();
				} else
					iData = br.readLine();
			}

			// Return result
			_infoCache.add(info);
			return info;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}