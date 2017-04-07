// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.FileUpload;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.Country;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.file.GetAirspaceDefinition;

import org.deltava.security.command.ScheduleAccessControl;
import org.deltava.util.CollectionUtils;

/**
 * A Web Site Command to import Airspace boundary data. 
 * @author Luke
 * @version 7.3
 * @since 7.3
 */

public class AirspaceImportCommand extends NavDataImportCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the Command result
		CommandResult result = ctx.getResult();

		// Check our access level
		ScheduleAccessControl access = new ScheduleAccessControl(ctx);
		access.validate();
		if (!access.getCanImport())
			throw securityException("Cannot import Navigation Data");
		
		// Load the data
		CycleInfo inf = getCurrrentCycle(ctx);
		ctx.setAttribute("currentNavCycle", inf, REQUEST);
		ctx.setAttribute("countries", Country.getAll(), REQUEST);
		
		// If we're doing a GET, then redirect to the JSP
		FileUpload navData = ctx.getFile("navData");
		if (navData == null) {
			result.setURL("/jsp/navdata/airspaceImport.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Load airspaceTypes
		Collection<AirspaceType> types = new HashSet<AirspaceType>();
		if (!CollectionUtils.isEmpty(ctx.getParameters("types")))
			ctx.getParameters("types").stream().map(t -> AirspaceType.fromName(t)).filter(Objects::nonNull).forEach(at -> types.add(at));
		
		boolean doPurge = Boolean.valueOf(ctx.getParameter("doPurge")).booleanValue();
		Country c = Country.get(ctx.getParameter("country"));
		try (InputStream is = navData.getInputStream()) {
			Connection con = ctx.getConnection();
			ctx.startTX();
			
			// Load the data
			GetAirspaceDefinition dao = new GetAirspaceDefinition(is);
			Collection<Airspace> airspaces = dao.load().stream().filter(a -> types.isEmpty() || types.contains(a.getType())).collect(Collectors.toList());
			airspaces.forEach(a -> a.setCountry(c));
			
			// Get the write DAO, clear and update
			SetAirspace wdao = new SetAirspace(con);
			if (doPurge)
				wdao.clear(c);
			for (Airspace a : airspaces)
				wdao.write(a);
			
			ctx.commitTX();
		} catch (IOException | DAOException ie) {
			ctx.rollbackTX();
			throw new CommandException(ie);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		ctx.setAttribute("airspaceData", Boolean.TRUE, REQUEST);
		result.setURL("/jsp/navdata/navDataUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}