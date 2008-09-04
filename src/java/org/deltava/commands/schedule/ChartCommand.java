// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.List;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ChartAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle Approach Charts.
 * @author Luke
 * @version 2.2
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
			Chart c = new Chart("", null);
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
			} else {
				// Get our access
				ChartAccessControl access = new ChartAccessControl(ctx);
				access.validate();
				if (!access.getCanCreate())
					throw securityException("Cannot create Approach Chart");
			}
			
			// Load data from the request
			c.setName(ctx.getParameter("name"));
			c.setAirport(SystemData.getAirport(ctx.getParameter("airport")));
			c.setType(ctx.getParameter("chartType"));
			
			// Load the image data
			FileUpload imgData = ctx.getFile("img");
			boolean hasData = ((imgData != null) && (imgData.getSize() > 10));
			
			// Check for PDF
			if (hasData) {
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
					c.setImgType(Chart.PDF);
			
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
				if (hasData)
					wdao.write(c);
				else
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
		
		// Get chart types (and remove Unknown)
		List<ComboAlias> cTypes = ComboUtils.fromArray(Chart.TYPENAMES, Chart.TYPES);
		cTypes.remove(0);

		// Save chart types and ICAO
		ctx.setAttribute("chartTypes", cTypes, REQUEST);
		ctx.setAttribute("doICAO", Boolean.valueOf(ctx.getUser().getAirportCodeType() == Airport.ICAO), REQUEST);

		// Load the chart if not new
		if (!isNew) {
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
		}

		// Save chart access 
		ctx.setAttribute("access", access, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
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

		Chart c = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the chart
			GetChart dao = new GetChart(con);
			c = dao.get(ctx.getID());
			if (c == null)
				throw notFoundException("Invalid Approach Chart - " + ctx.getID());

			// Save the chart and the available charts for this airport
			ctx.setAttribute("chart", c, REQUEST);
			ctx.setAttribute("isPDF", Boolean.valueOf(c.getImgType() == Chart.PDF), REQUEST);
			ctx.setAttribute("charts", dao.getCharts(c.getAirport()), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Get comamnd result - if we're a PDF, just redirect there
		CommandResult result = ctx.getResult();
		result.setSuccess(true);
		if (c.getImgType() == Chart.PDF) {
			result.setType(CommandResult.REDIRECT);
			result.setURL("/charts/" + c.getHexID() + ".pdf");
		} else {
			// Determine if we're displaying the printer-friendly page
			boolean isPrintFriendly = "print".equals(ctx.getCmdParameter(Command.OPERATION, null));
			String JSPname = isPrintFriendly ? "chartPrint.jsp" : "chartView.jsp";

			// Forward to the JSP
			result.setURL("/jsp/schedule/" + JSPname);
		}
	}
}