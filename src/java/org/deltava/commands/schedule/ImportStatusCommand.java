// Copyright 2006, 2007, 2009, 2016, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.io.*;
import java.util.*;

import org.deltava.beans.schedule.*;
import org.deltava.commands.*;

import org.deltava.dao.*;
import org.deltava.dao.file.GetImportStatus;
import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display the results of a scheduled Innovata Schedule download.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class ImportStatusCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			GetRawSchedule rsdao = new GetRawSchedule(ctx.getConnection());
			Collection<ScheduleSourceInfo> srcs = rsdao.getSources(false, ctx.getDB());
			
			// Loop through all import statuses
			Collection<ImportStatus> results = new ArrayList<ImportStatus>();
			for (ScheduleSourceInfo inf : srcs) {
				String fileName = SystemData.get("airline.code") + "." + inf.getSource().name() + ".import.status.txt";
				File f = new File(SystemData.get("schedule.cache"), fileName);
				if (f.exists()) {
					try (InputStream fs = new FileInputStream(f)) {
						GetImportStatus dao = new GetImportStatus(fs);
						results.add(dao.load());
					}
				}
			}
			
			ctx.setAttribute("sourceStats", CollectionUtils.createMap(srcs, ScheduleSourceInfo::getSource), REQUEST);
			ctx.setAttribute("importStatus", results, REQUEST);
		} catch (DAOException | IOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/importStatus.jsp");
		result.setSuccess(true);
	}
}