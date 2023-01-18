// Copyright 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.time.*;
import java.util.Collection;
import java.sql.Connection;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.ScheduleEntry;
import org.deltava.beans.stats.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.security.command.TourAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Serivce to update a Flight Tour. 
 * @author Luke
 * @version 10.4
 * @since 10.3
 */

public class TourUpdateService extends TourService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		int id = StringUtils.parse(ctx.getParameter("id"), 0); JSONObject ro = null;
		try {
			Connection con = ctx.getConnection();
			GetTour tdao = new GetTour(con);
			Tour t = tdao.get(id, ctx.getDB()); Tour ot = t;
			if ((t == null) && (id != 0))
				return SC_NOT_FOUND;
			
			// Check our access
			TourAccessControl ac = new TourAccessControl(ctx, t);
			ac.validate();
			boolean canExec = (t == null) ? ac.getCanCreate() : ac.getCanEdit();
			if (!canExec)
				throw error(SC_FORBIDDEN, String.format("Cannot %s Tour", (t == null) ? "create" : "update"), false);
			
			// Parse the object and update the bean
			try {
				JSONObject jo = new JSONObject(new JSONTokener(ctx.getBody()));
				if (t == null) 
					t = new Tour(jo.getString("name"));
				else
					t.setName(jo.getString("name"));
			
				t.setOwner(SystemData.getApp(jo.optString("owner", SystemData.get("airline.code"))));
				t.setStatus(EnumUtils.parse(TourStatus.class, jo.optString("status"), t.getStatus()));
				t.setActive(jo.optBoolean("active"));
				t.setACARSOnly(jo.optBoolean("acarsOnly"));
				t.setAllowOffline(jo.optBoolean("allowOffline"));
				t.setMatchEquipment(jo.optBoolean("matchEQ"));
				t.setMatchLeg(jo.optBoolean("matchLeg"));
				t.setStartDate(Instant.ofEpochSecond(jo.getLong("startDate")));
				t.setEndDate(Instant.ofEpochSecond(jo.getLong("endDate")));
				
				// Parse networks
				t.clearNetworks();
				JSONArray na = jo.getJSONArray("networks");
				for (int x = 0; x < na.length(); x++)
					t.addNetwork(OnlineNetwork.valueOf(na.getString(x)));
				
				// Parse flights
				t.clearFlights();
				JSONArray fa = jo.getJSONArray("flights");
				for (int x = 0; x < fa.length(); x++) {
					JSONObject fo = fa.getJSONObject(x);
					ScheduleEntry se = new ScheduleEntry(SystemData.getAirline(fo.getString("airline")), fo.getInt("flight"), fo.getInt("leg"));
					se.setEquipmentType(fo.getString("eqType"));
					se.setHistoric(fo.optBoolean("historic"));
					se.setAirportD(SystemData.getAirport(fo.getJSONObject("airportD").getString("iata")));
					se.setAirportA(SystemData.getAirport(fo.getJSONObject("airportA").getString("iata")));
					JSONObject tdo = fo.getJSONObject("timeD");
					LocalTime td = LocalTime.of(tdo.getInt("h"), tdo.getInt("m"));
					se.setTimeD(LocalDateTime.of(LocalDate.ofInstant(t.getStartDate(), se.getAirportD().getTZ().getZone()), td));
					JSONObject tao = fo.getJSONObject("timeA");
					LocalTime ta = LocalTime.of(tao.getInt("h"), tao.getInt("m"));
					se.setTimeA(LocalDateTime.of(LocalDate.ofInstant(t.getStartDate(), se.getAirportA().getTZ().getZone()), ta));
					t.addFlight(se);
				}
			} catch (Exception e) {
				ctx.setHeader("error", e.getMessage());
				throw error(SC_BAD_REQUEST, e.getMessage(), e);
			}
			
			// Check audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(ot, t, "buffer");
			AuditLog ae = AuditLog.create(t, delta, ctx.getUser().getID());
			
			// Write the updated object and audit log
			ctx.startTX();
			SetTour twdao = new SetTour(con);
			twdao.write(t);
			if (ae != null) {
				ae.setRemoteAddr(ctx.getRequest().getRemoteAddr());
				ae.setRemoteHost(ctx.getRequest().getRemoteHost());
				SetAuditLog awdao = new SetAuditLog(con);
				awdao.write(ae);
			}
			
			ro = serialize(t);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			ctx.setHeader("X-Error-Msg", de.getMessage());
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.println(ro.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
}