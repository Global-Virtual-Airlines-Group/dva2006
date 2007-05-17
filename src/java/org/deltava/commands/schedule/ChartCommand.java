// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.Connection;

import org.deltava.beans.FileUpload;
import org.deltava.beans.schedule.Chart;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ChartAccessControl;

import org.deltava.util.ComboUtils;
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
					throw notFoundException("Invalid Approach Chart - " + ctx.getID());

				// Get our access
				ChartAccessControl access = new ChartAccessControl(ctx);
				access.validate();
				if (!access.getCanEdit())
					throw securityException("Cannot edit Approach Chart");

				// Load data from the request
				c.setName(ctx.getParameter("name"));
				c.setAirport(SystemData.getAirport(ctx.getParameter("airport")));
				c.setType(ctx.getParameter("chartType"));
			} else {
				// Get our access
				ChartAccessControl access = new ChartAccessControl(ctx);
				access.validate();
				if (!access.getCanCreate())
					throw securityException("Cannot create Approach Chart");

				c = new Chart(ctx.getParameter("name"), SystemData.getAirport(ctx.getParameter("airport")));
				c.setType(ctx.getParameter("chartType"));

				// Load the image data
				FileUpload imgData = ctx.getFile("img");
				if ((imgData == null) || (imgData.getSize() < 10)) {
					CommandException ce = new CommandException("No Image Data Uploaded");
					ce.setLogStackDump(false);
					throw ce;
				}
				
				// Check for PDF
				boolean isPDF = true;
				byte[] buffer = imgData.getBuffer();
				for (int x = 0; x < Chart.PDF_MAGIC.length(); x++)
					isPDF &= (buffer[x] == Chart.PDF_MAGIC.getBytes()[x]);
				
				// Get image metadata
				if (!isPDF) {
					ImageInfo info = new ImageInfo(buffer);
					info.check();
					c.setImgType(info.getFormat());
				} else
					c.setImgType(Chart.IMG_PDF);
				
				c.load(buffer);
			}

			// Save the chart in the request
			ctx.setAttribute("chart", c, REQUEST);

			// Get the write DAO and save the image
			SetChart wdao = new SetChart(con);
			if (isNew) {
				wdao.write(c);
				ctx.setAttribute("isCreate", Boolean.TRUE, REQUEST);
			} else {
				wdao.update(c);
				ctx.setAttribute("isUpdate", Boolean.TRUE, REQUEST);
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
			throw securityException("Cannot create/edit Approach Chart");

		// Save chart types
		ctx.setAttribute("chartTypes", ComboUtils.fromArray(Chart.TYPENAMES, Chart.TYPES), REQUEST);

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
				throw notFoundException("Invalid Approach Chart - " + ctx.getID());

			// Save the chart in the request
			ctx.setAttribute("chart", c, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save chart access
		ctx.setAttribute("access", access, REQUEST);

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
				throw notFoundException("Invalid Approach Chart - " + ctx.getID());

			// Save the chart and the available charts for this airport
			ctx.setAttribute("chart", c, REQUEST);
			ctx.setAttribute("isPDF", Boolean.valueOf(c.getType() == Chart.IMG_PDF), REQUEST);
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