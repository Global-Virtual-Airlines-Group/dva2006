// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.Connection;

import org.deltava.beans.FileUpload;
import org.deltava.beans.schedule.Chart;

import org.deltava.commands.*;

import org.deltava.dao.GetChart;
import org.deltava.dao.SetChart;
import org.deltava.dao.DAOException;

import org.deltava.security.command.ChartAccessControl;

import org.deltava.util.ImageInfo;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle Approach Charts.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ChartCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the Chart.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {

		// Check if we're creating a new Chart
		boolean isNew = (ctx.getID() == 0);

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the chart
			Chart c = null;
			if (!isNew) {
				GetChart dao = new GetChart(con);
				c = dao.get(ctx.getID());
				if (c == null)
					throw new CommandException("Invalid Approach Chart - " + ctx.getID());

				// Get our access
				ChartAccessControl access = new ChartAccessControl(ctx);
				access.validate();
				if (!access.getCanEdit())
					throw new CommandSecurityException("Cannot edit Approach Chart");

				// Load data from the request
				c.setName(ctx.getParameter("name"));
				c.setAirport(SystemData.getAirport(ctx.getParameter("airport")));
			} else {
				// Get our access
				ChartAccessControl access = new ChartAccessControl(ctx);
				access.validate();
				if (!access.getCanCreate())
					throw new CommandSecurityException("Cannot create Approach Chart");

				c = new Chart(ctx.getParameter("name"), SystemData.getAirport(ctx.getParameter("airport")));

				// Load the image data
				FileUpload imgData = ctx.getFile("img");
				if (imgData == null)
					throw new CommandException("No Image Data Uploaded");

				// Get image metadata
				ImageInfo info = new ImageInfo(imgData.getBuffer());
				info.check();

				// Set image properties
				c.setType(info.getFormat());
				c.load(imgData.getBuffer());
			}
			
			// Save the chart in the request
			ctx.setAttribute("chart", c, REQUEST);

			// Get the write DAO and save the image
			SetChart wdao = new SetChart(con);
			if (isNew) {
				wdao.write(c);
				ctx.setAttribute("isCreate", Boolean.valueOf(true), REQUEST);
			} else {
				wdao.update(c);
				ctx.setAttribute("isUpdate", Boolean.valueOf(true), REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/schedule/chartUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the Chart.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {

		// Check if we're creating a new Chart
		boolean isNew = (ctx.getID() == 0);

		// Get our access level
		ChartAccessControl access = new ChartAccessControl(ctx);
		access.validate();
		boolean isOK = (isNew) ? access.getCanCreate() : access.getCanEdit();
		if (!isOK)
			throw new CommandSecurityException("Cannot create/edit Approach Chart");

		// Get the command result and forward to the JSP if this is new
		CommandResult result = ctx.getResult();
		if (isNew) {
			result.setURL("/jsp/schedule/chartEdit.jsp");
			result.setSuccess(true);
			return;
		}

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the chart
			GetChart dao = new GetChart(con);
			Chart c = dao.get(ctx.getID());
			if (c == null)
				throw new CommandException("Invalid Approach Chart - " + ctx.getID());

			// Save the chart in the request
			ctx.setAttribute("chart", c, REQUEST);
			ctx.setAttribute("access", access, REQUEST);

		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		result.setURL("/jsp/schedule/chartEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the Chart.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execRead(CommandContext ctx) throws CommandException {

		// Get our access level
		ChartAccessControl access = new ChartAccessControl(ctx);
		access.validate();
		ctx.setAttribute("access", access, REQUEST);

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the chart
			GetChart dao = new GetChart(con);
			Chart c = dao.get(ctx.getID());
			if (c == null)
				throw new CommandException("Invalid Approach Chart - " + ctx.getID());

			// Save the chart and the available charts for this airport
			ctx.setAttribute("chart", c, REQUEST);
			ctx.setAttribute("charts", dao.getCharts(c.getAirport().getIATA()), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Determine if we're displaying the printer-friendly page
		boolean isPrintFriendly = "print".equals(ctx.getCmdParameter(Command.OPERATION, null));
		String JSPname = isPrintFriendly ? "chartPrint.jsp" : "chartView.jsp";

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/" + JSPname);
		result.setSuccess(true);
	}
}