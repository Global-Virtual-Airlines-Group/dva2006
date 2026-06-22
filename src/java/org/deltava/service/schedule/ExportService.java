// Copyright 2019, 2020, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static jakarta.servlet.http.HttpServletResponse.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to export Raw Schedule entries.
 * @author Luke
 * @version 12.5
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
		
		// Get export format
		ScheduleFormat sfmt = EnumUtils.parse(ScheduleFormat.class, ctx.getParameter("fmt"), ScheduleFormat.CSV);
		ScheduleFormatter sf = sfmt.getIntsance();

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

		// Set the content type and force Save As
		ctx.setContentType(sfmt.getContentType(), "utf-8");
		ctx.setHeader("X-Schedule-Name", String.format("%s_raw_schedule.%s", SystemData.get("airline.code").toLowerCase(), sfmt.getExtension()));

		try (PrintWriter out = ctx.getResponse().getWriter()) {
			out.print(sf.getHeader());
			for (Iterator<RawScheduleEntry> i = entries.iterator(); i.hasNext(); ) {
				RawScheduleEntry entry = i.next();
				out.print(sf.format(entry));
				if (i.hasNext())
					out.println(sf.getSeparator());
			}

	         out.println(sf.getFooter());
	         ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}

	@Override
	public final boolean isSecure() {
		return true;
	}
}