// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static jakarta.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;
import java.time.LocalDate;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.EnumUtils;

/**
 * A Web Service to transfer Raw Schedule entries between environments. 
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class RawScheduleTransferService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Check for system / admin user
		if (!ctx.getUser().isInRole("Admin"))
			return SC_FORBIDDEN;
		
		// Get the schedule source and the date
		ScheduleSource src = EnumUtils.parse(ScheduleSource.class, ctx.getParameter("src"), ScheduleSource.AVSTACK);
		LocalDate ld = LocalDate.parse(ctx.getParameter("date"));
		Collection<RawScheduleEntry> entries = new ArrayList<RawScheduleEntry>();
		try {
			GetRawSchedule rsdao = new GetRawSchedule(ctx.getConnection());
			entries.addAll(rsdao.load(src, ld));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Dump the JSON to the output stream
		ScheduleFormatter fmt = new JSONScheduleFormatter();
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.setExpiry(3600);
			ctx.print(fmt.getHeader());
			for (Iterator<RawScheduleEntry> i = entries.iterator(); i.hasNext(); ) {
				RawScheduleEntry rse = i.next();
				ctx.print(fmt.format(rse));
				if (i.hasNext())
					ctx.println(fmt.getSeparator());
			}
			
			ctx.println(fmt.getFooter());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	@Override
	public boolean isSecure() {
		return true;
	}
}