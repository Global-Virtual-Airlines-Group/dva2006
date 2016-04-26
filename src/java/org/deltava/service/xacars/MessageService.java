// Copyright 2011, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.xacars;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.apache.log4j.Logger;

import org.deltava.beans.acars.*;
import org.deltava.beans.acars.XAFlightInfo.ClimbPhase;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

import org.gvagroup.acars.ACARSFlags;

/**
 * The XACARS status message Web Service. 
 * @author Luke
 * @version 7.0
 * @since 4.1
 */

public class MessageService extends XAService {

	private static final Logger log = Logger.getLogger(MessageService.class);
	
	/**
	 * XACARS message types.
	 */
	private enum MessageType {
		PR("Position"), AR("Altitude"), WX("Weather"), QA("Start"), QB("Takeoff"), QC("Landing"),
		QD("End"), QM("Statistics"), CM("User Message");
		
		private String _name;
		
		MessageType(String name) {
			_name = name;
		}
		
		public String getName() {
			return _name;
		}
	}
	
	/**
	 * Helper class to calculate average N1/N2 values.
	 */
	private class EngineInfo {
		private int _cnt;
		private int _aggN1;
		
		EngineInfo() {
			super();
		}

		public void addN1(int eng, int n1) {
			_cnt = Math.max(eng, _cnt);
			_aggN1 += n1;
		}
		
		public double getN1() {
			return _aggN1 / ((double) _cnt);
		}
	}
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the data and sanity check
		int flightID = StringUtils.parse(ctx.getParameter("DATA3"), -1);
		String msgData = ctx.getParameter("DATA4");
		if ((msgData == null) || (msgData.length() < 32)) {
			ctx.print("0|Invalid XACARS Message");
			return SC_OK;
		} else if (flightID <= 1) {
			ctx.print("0|");
			ctx.print((flightID == 0) ? "Flight Not Started" : "Unparseable Flight ID");
			return SC_OK;
		}
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the flight
			GetXACARS dao = new GetXACARS(con);
			XAFlightInfo inf = dao.getFlight(flightID);
			if (inf == null)
				throw new InvalidDataException("Invalid Flight ID");
			
			// Parse the message
			//Date dt = StringUtils.parseDate(msgData.substring(1, 16), "MM/dd/yyyy HH:mm");
			int pos = msgData.indexOf("Msg Label: ");
			if (pos == -1)
				throw new InvalidDataException("No Message Label");
			
			// Get the message type
			MessageType mt = null;
			String msgType = msgData.substring(pos +  11, msgData.indexOf(' ', pos + 11));
			try {
				mt = MessageType.valueOf(msgType.toUpperCase());
				log.info("Received " + mt.getName() + " message");
				log.info(msgData.substring(pos));
			} catch (IllegalArgumentException iae) {
				throw new InvalidDataException("Invalid Message Type - " + msgType);
			}
			
			// Get message body
			pos = msgData.indexOf("Message:", pos + 14);
			if (pos == -1)
				throw new InvalidDataException("No Message Body");
			
			// Save other variables
			int temp = 0; int zfw = inf.getZeroFuelWeight();
			EngineInfo nX = new EngineInfo();

			// Parse the position data
			XARouteEntry re = new XARouteEntry(null, Instant.now());
			re.setFlightID(inf.getID());
			re.setFlag(ACARSFlags.FLAG_ONGROUND, (inf.getPhase() != FlightPhase.AIRBORNE));
			List<String > data = StringUtils.split(msgData.substring(pos + 9) , "/");
			for (String cmdEntry : data) {
				pos = cmdEntry.indexOf(' ');
				if (pos == -1) {
					log.warn("Invalid message command - " + cmdEntry);
					continue;
				}
				
				String cmd = cmdEntry.substring(0, pos).toUpperCase();
				String cmdData = cmdEntry.substring(pos + 1).trim();
				if ("HDG".equals(cmd))
					re.setHeading(StringUtils.parse(cmdData, 0));
				else if ("ZFW".equals(cmd)) {
					zfw = StringUtils.parse(cmdData, zfw);
					inf.setZeroFuelWeight(zfw);
				} else if ("FOB".equals(cmd))
					re.setFuelRemaining(StringUtils.parse(cmdData, 0));
				else if ("IAS".equals(cmd))
					re.setAirSpeed(StringUtils.parse(cmdData, re.getGroundSpeed()));
				else if ("POS".equals(cmd))
					re.setLocation(GeoUtils.parseXACARS(cmdData));
				else if ("OAT".equals(cmd))
					temp = StringUtils.parse(cmdData, -20);
				else if ("TAS".equals(cmd))
					re.setGroundSpeed(StringUtils.parse(cmdData, re.getAirSpeed()));
				else if ("ALT".equals(cmd)) {
					int pos2 = cmdData.indexOf(' ');
					if (pos2 > -1) {
						re.setAltitude(StringUtils.parse(cmdData.substring(0, pos2), 0));
						String dir = cmdData.substring(pos2 + 1).trim().toUpperCase();
						if ("CLIMB".equals(dir)) {
							inf.setClimbPhase(ClimbPhase.CLIMB);
							re.setVerticalSpeed(300);
						} else if ("DESC".equals(dir)) {
							inf.setClimbPhase(ClimbPhase.DESCEND);
							re.setVerticalSpeed(-300);
						}
					} else
						re.setAltitude(StringUtils.parse(cmdData, 0));
				} else if ("WND".equals(cmd) && (cmdData.length() > 4)) {
					re.setWindHeading(StringUtils.parse(cmdData.substring(0, 3), 0));
					re.setWindSpeed(StringUtils.parse(cmdData.substring(3, 5), 0));
				} else if (cmd.startsWith("E") && (cmd.length() == 4) && (cmd.charAt(2) == 'N')) {
					int eng = StringUtils.parse(cmd.substring(1, 2), 0);
					int NX = StringUtils.parse(cmd.substring(3, 4), 0);
					if ((eng < 1) || (eng > 4))
						log.warn("Unknown engine - " + eng);
					else if ((NX != 1) && (NX != 2))
						log.warn("Unknown engine " + eng + " Nx - " + NX);
					else if (NX == 1) {
						int nValue = StringUtils.parse(cmdData, 0);
						nX.addN1(eng, nValue);
					}
				}
			}
			
			// Calculate Mach and ground speed - ensure we have a TAS
			re.setGroundSpeed(Math.max(re.getGroundSpeed(), re.getAirSpeed()));
			re.setMach(re.getGroundSpeed() / (38.967854d * Math.sqrt(temp + 273.15d)));
			double gs = Math.sqrt((re.getWindSpeed() * re.getWindSpeed()) + (re.getGroundSpeed() * re.getGroundSpeed()) -
					(2 * re.getWindSpeed() * re.getGroundSpeed() * Math.cos(Math.toRadians(re.getHeading() - re.getWindHeading()))));
			re.setGroundSpeed((int) gs);
			re.setMessageType(mt.toString());
			
			// Start transaction
			ctx.startTX();
			
			// Do phase change
			boolean writePosition = false;
			SetXACARS xwdao = new SetXACARS(con);
			switch (mt) {
				case QA:
					writePosition = true;
					inf.setTaxiTime(re.getDate());
					inf.setTaxiFuel(re.getFuelRemaining());
					inf.setTaxiWeight(re.getFuelRemaining() + zfw);
					inf.setPhase(FlightPhase.TAXIOUT);
					xwdao.update(inf);
					break;
					
				case QB:
					writePosition = true;
					re.setFlag(ACARSFlags.FLAG_TOUCHDOWN, true);
					inf.setTakeoffTime(re.getDate());
					inf.setTakeoffFuel(re.getFuelRemaining());
					inf.setTakeoffWeight(re.getFuelRemaining() + zfw);
					inf.setTakeoffHeading(re.getHeading());
					inf.setTakeoffSpeed(re.getAirSpeed());
					inf.setTakeoffLocation(re);
					inf.setTakeoffN1(nX.getN1());
					inf.setTakeoffDistance(GeoUtils.distance(re, inf.getAirportD()));
					inf.setClimbPhase(ClimbPhase.CLIMB);
					inf.setPhase(FlightPhase.AIRBORNE);
					xwdao.update(inf);
					break;
					
				case QC:
					writePosition = true;
					re.setFlag(ACARSFlags.FLAG_TOUCHDOWN, true);
					inf.setLandingTime(re.getDate());
					inf.setLandingFuel(re.getFuelRemaining());
					inf.setLandingWeight(re.getFuelRemaining() + zfw);
					inf.setLandingHeading(re.getHeading());
					inf.setLandingSpeed(re.getAirSpeed());
					inf.setLandingLocation(re);
					inf.setLandingN1(nX.getN1());
					inf.setLandingDistance(GeoUtils.distance(re, inf.getAirportD()));
					inf.setPhase(FlightPhase.TAXIIN);
					inf.setClimbPhase(ClimbPhase.UNKNOWN);
					xwdao.update(inf);
					break;
				
				case QD:
				case QM:
					writePosition = true;
					inf.setGateFuel(re.getFuelRemaining());
					inf.setGateWeight(re.getFuelRemaining() + zfw);
					inf.setPhase(FlightPhase.ATGATE);
					inf.setClimbPhase(ClimbPhase.UNKNOWN);
					xwdao.update(inf);
					break;
				
				case PR:
					writePosition = true;
					break;
					
				case AR:
					xwdao.update(inf);
					break;
					
				case CM:
				case WX:
				default:
					break;
			}
			
			// Write the position entry
			writePosition &= GeoUtils.isValid(re);
			if (writePosition) {
				log.info("IAS = " +  re.getAirSpeed() + " TAS = " + re.getGroundSpeed() + " MACH = " + re.getMach() + " GS = " + gs);
				re.setPhase(inf.getPhase().ordinal());
				xwdao.write(re);
			}
			
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} catch (InvalidDataException ide) {
			ctx.rollbackTX();
			log.warn(ide.getMessage());
			ctx.print(ide.getResponse());
		} finally {
			ctx.release();
		}
		
		return SC_OK;
	}
}