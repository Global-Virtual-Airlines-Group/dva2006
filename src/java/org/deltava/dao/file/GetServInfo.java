// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2015, 2016, 2017, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;
import java.time.Instant;

import org.apache.log4j.Logger;

import org.deltava.beans.OnlineNetwork;

import org.deltava.beans.servinfo.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Data Acces Object to fetch VATSIM/IVAO ServInfo data.
 * ServInfo data for pilots and controllers is in a colon (:) delimited format, with the following tokens:
 * 00 callsign 01 cid 02 realname 03 clienttype 04 frequency 05 latitude 06 longitude 07 altitude 08 groundspeed
 * 09 planned_aircraft 10 planned_tascruise 11 planned_depairport 12 planned_altitude 13 planned_destairport
 * 14 server 15 protrevision 16 rating 17 transponder 18 facilitytype 19 visualrange 20 planned_revision 
 * 21 planned_flighttype 22 planned_deptime 23 planned_actdeptime 24 planned_hrsenroute
 * 25 planned_minenroute 26 planned_hrsfuel 27 planned_minfuel 28 planned_altairport 29 planned_remarks
 * 30 planned_route 31 planned_depairport_lat 32 planned_depairport_lon 33 planned_destairport_lat
 * 34 planned_destairport_lon 35 atis_message 36 time_last_atis_received 37 time_logon 38 heading 39 QNH_iHg 40 QNH_Mb
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class GetServInfo extends DAO implements OnlineNetworkDAO {

	private static final Logger log = Logger.getLogger(GetServInfo.class);
	
	private final OnlineNetwork _net;

	/**
	 * Initializes the DAO with a particular stream.
	 * @param is the stream to use
	 * @param net the OnlineNetwork
	 */
	public GetServInfo(InputStream is, OnlineNetwork net) {
		super(is);
		_net = net;
	}
	
	/**
	 * Private helper class to parse ServInfo CLIENT data.
	 */
	class SITokens extends ArrayList<String> {

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
		public static final int HDG = 38;

		public SITokens(String data) {
			super(42);
			StringUtils.split(data, ":").forEach(s -> add(s.trim()));
		}
	}

	private static Airport getAirport(String airportCode) {
		Airport a = SystemData.getAirport(airportCode);
		return (a == null) ? new Airport(airportCode, airportCode, airportCode) : a;
	}
	
	@Override
	public NetworkInfo getInfo() throws DAOException {
		try (LineNumberReader br = getReader()) {
			NetworkInfo info = new NetworkInfo(_net);
			Map<String, MutableInteger> serverCons = new HashMap<String, MutableInteger>();
			String iData = br.readLine();
			while (iData != null) {
				if ((iData.length() > 7) && (iData.charAt(0) == '!')) {
					String sectionName = iData.substring(1, 8).toUpperCase();
					if (log.isDebugEnabled())
						log.debug("Loading Section " + sectionName);

					// Parse the general section
					if ("GENERAL".equals(sectionName)) {
						do {
							StringTokenizer tk = new StringTokenizer(iData, " = ");
							if (tk.countTokens() == 2) {
								String name = tk.nextToken();
								String val = tk.nextToken();
								if ("VERSION".equalsIgnoreCase(name))
									info.setVersion(StringUtils.parse(val, 7));
								else if ("UPDATE".equalsIgnoreCase(name)) {
									try {
										info.setValidDate(StringUtils.parseInstant(val, "yyyyMMddHHmmss"));
									} catch (Exception e) {
										log.info(e.getMessage());
										info.setValidDate(Instant.now());
									}

									if (log.isDebugEnabled())
										log.debug("Valid as of " + info.getValidDate());
								}
							}

							// Get next line
							iData = br.readLine();
						} while ((iData != null) && (iData.indexOf(" = ") != -1));
					// Parse the pilot/ATC list
					} else if ("CLIENTS".equals(sectionName)) {
						iData = br.readLine();
						while ((iData != null) && (iData.length() > 3) && (iData.charAt(0) != '!')) {
							SITokens si = new SITokens(iData);
							int id = StringUtils.parse(si.get(SITokens.ID), 0);
							NetworkUser.Type usrType = NetworkUser.Type.PILOT;
							try {
								usrType = NetworkUser.Type.valueOf(si.get(SITokens.TYPE).toUpperCase());
							} catch (Exception e) {
								// empty
							}

							// Load the type
							switch (usrType) {
								case ATC:
									if (si.size() < SITokens.FACILITY)
										break;
									
									try {
										Controller c = new Controller(id, _net);
										c.setCallsign(si.get(SITokens.CALLSIGN));
										c.setName(si.get(SITokens.NAME));
										c.setFrequency(si.get(SITokens.FREQ));
										c.setPosition(si.get(SITokens.LAT), si.get(SITokens.LON));
										c.setRating(Rating.values()[StringUtils.parse(si.get(SITokens.RATING), 1)]);
										c.setServer(si.get(SITokens.SERVER).toUpperCase());
										if (c.getFacility() != Facility.ATIS) {
											int idx = StringUtils.parse(si.get(SITokens.FACILITY), 0);
											c.setFacility(Facility.values()[idx]);
										}
										
										// Set server
										MutableInteger srvCnt = serverCons.get(c.getServer());
										if (srvCnt == null)
											serverCons.put(c.getServer(), new MutableInteger(1));
										else
											srvCnt.inc();
										
										info.add(c);
									} catch (Exception e) {
										log.info("Error parsing controller data for " + si.get(SITokens.CALLSIGN) + " - " + e.getMessage());
									}
									
									break;

								default:
								case PILOT:
									if (si.size() < SITokens.ROUTE)
										break;
									
									try {
										Pilot p = new Pilot(id, _net);
										p.setCallsign(si.get(SITokens.CALLSIGN));
										p.setName(si.get(SITokens.NAME));
										p.setAirportD(getAirport(si.get(SITokens.AIRPORT_D)));
										p.setAirportA(getAirport(si.get(SITokens.AIRPORT_A)));
										p.setPosition(si.get(SITokens.LAT), si.get(SITokens.LON));
										p.setEquipmentCode(si.get(SITokens.EQCODE));
										p.setAltitude(StringUtils.parse(si.get(SITokens.ALT), 0));
										p.setHeading(StringUtils.parse(si.get(SITokens.HDG), 0));
										p.setGroundSpeed(StringUtils.parse(si.get(SITokens.GSPEED), 0));
										p.setRoute(si.get(SITokens.ROUTE));
										p.setServer(si.get(SITokens.SERVER).toUpperCase());

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
										
										// Set server
										MutableInteger srvCnt = serverCons.get(p.getServer());
										if (srvCnt == null)
											serverCons.put(p.getServer(), new MutableInteger(1));
										else
											srvCnt.inc();

										// Add to results
										if (p.getID() != 0)
											info.add(p);
									} catch (Exception e) {
										log.info("Error parsing pilot data for " + si.get(SITokens.CALLSIGN) + " - " + e.getMessage());
									}
							}

							iData = br.readLine();
						}
					} else if ("SERVERS".equals(sectionName)) {
						iData = br.readLine();
						while ((iData != null) && (iData.length() > 3) && (iData.charAt(0) != '!')) {
							StringTokenizer tk = new StringTokenizer(iData, ":");
							String name = tk.nextToken();
							try {
								Server srv = new Server(name);
								srv.setAddress(tk.nextToken().trim());
								srv.setLocation(tk.nextToken());
								srv.setComment(tk.nextToken());
								MutableInteger srvCnt = serverCons.get(srv.getName());
								srv.setConnections((srvCnt ==  null) ? 0 : srvCnt.getValue().intValue());
								info.add(srv);
							} catch (Exception e) {
								log.info("Error parsing server data for " + name + " - " + e.getMessage());
							}

							iData = br.readLine();
						}
					} else
						iData = br.readLine();
				} else
					iData = br.readLine();
			}

			return info;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}