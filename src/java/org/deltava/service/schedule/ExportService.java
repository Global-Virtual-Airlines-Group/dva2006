// Copyright 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.time.*;
import java.time.format.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to export Raw Schedule entries.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class ExportService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Check our access level
		ScheduleAccessControl access = new ScheduleAccessControl(ctx);
		access.validate();
		if (!access.getCanExport())
			throw error(SC_FORBIDDEN, "Cannot export Flight Schedule");

		// Load Sources
		Collection<ScheduleSource> srcs = StringUtils.split(ctx.getParameter("src"), ",").stream().map(sc -> ScheduleSource.valueOf(sc)).collect(Collectors.toCollection(TreeSet::new));
		Collection<RawScheduleEntry> entries = new ArrayList<RawScheduleEntry>();
		try {
			GetRawSchedule dao = new GetRawSchedule(ctx.getConnection());
			for (ScheduleSource src : srcs)
				entries.addAll(dao.load(src, null));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Create formatters
		DateTimeFormatter df = new DateTimeFormatterBuilder().appendPattern("dd-MMM").toFormatter();
		DateTimeFormatter tf = new DateTimeFormatterBuilder().appendPattern("HH:mm").toFormatter();
		boolean doICAO = (ctx.getUser().getAirportCodeType() == Airport.Code.ICAO);

		// Set the content type and force Save As
		String aCode = SystemData.get("airline.code");
		ctx.setContentType("text/csv", "utf-8");
		ctx.setHeader("X-Schedule-Name", SystemData.get("airline.code").toLowerCase() + "_raw_schedule.csv");

		try (PrintWriter out = ctx.getResponse().getWriter()) {
			// Write the header
			out.println("; " + aCode + " Flight Schedule - exported on " + StringUtils.format(Instant.now(), "MM/dd/yyyy HH:mm:ss") + " UTC");
			out.println("; SOURCE,LINE,STARTS,ENDS,AIRLINE,NUMBER,LEG,EQTYPE,FROM,DTIME,TO,ATIME,DISTANCE,HISTORIC");

	         for (RawScheduleEntry entry : entries) {
	             StringBuilder buf = new StringBuilder(entry.getSource().name());
	             buf.append(',');
	             buf.append(entry.getLineNumber());
	             buf.append(',');
	             buf.append(df.format(entry.getStartDate()));
	             buf.append(',');
	             buf.append(df.format(entry.getEndDate()));
	             buf.append(',');
	             buf.append(entry.getAirline().getCode());
	             buf.append(',');
	             buf.append(StringUtils.format(entry.getFlightNumber(), "#000"));
	             buf.append(',');
	             buf.append(String.valueOf(entry.getLeg()));
	             buf.append(',');
	             buf.append(entry.getEquipmentType());
	             buf.append(',');
	             buf.append(doICAO ? entry.getAirportD().getICAO() : entry.getAirportD().getIATA());
	             buf.append(',');
	             buf.append(tf.format(entry.getTimeD()));
	             buf.append(',');
	             buf.append(doICAO ? entry.getAirportA().getICAO() : entry.getAirportA().getIATA());
	             buf.append(',');
	             buf.append(tf.format(entry.getTimeA()));
	             buf.append(',');
	             buf.append(entry.getDistance());
	             buf.append(',');
	             buf.append(entry.getHistoric());
	             out.println(buf.toString());
	          }
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}

	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE always
	 */
	@Override
	public final boolean isSecure() {
		return true;
	}
}