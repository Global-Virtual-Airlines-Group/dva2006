// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2016, 2017, 2018, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.util.stream.Collectors;
import java.time.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.event.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.commands.*;
import org.deltava.comparators.AirportComparator;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.EventAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to save Online Events.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class EventSaveCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Check if this is a new event
		boolean isNew = (ctx.getID() == 0);
		
		// Save the airport list
		Collection<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
		airports.addAll(SystemData.getAirports().values());
		ctx.setAttribute("airports", airports, REQUEST);
		
		// Save network names
		ctx.setAttribute("networks", SystemData.getObject("online.networks"), REQUEST);

		// Initialize the messaging context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());
		String templateName = "EVENTCREATENS";

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the online event
			Event e = null;
			if (!isNew) {
				GetEvent dao = new GetEvent(con);
				e = dao.get(ctx.getID());
				if (e == null)
					throw notFoundException("Invalid Online Event - " + ctx.getID());

				e.setName(ctx.getParameter("name"));
				ctx.setAttribute("isUpdate", Boolean.TRUE, REQUEST);
			} else {
				e = new Event(ctx.getParameter("name"));
				e.setOwner(SystemData.getApp(SystemData.get("airline.code")));
				ctx.setAttribute("isNew", Boolean.TRUE, REQUEST);
			}

			// Check our access
			EventAccessControl access = new EventAccessControl(ctx, e);
			access.validate();
			if (!access.getCanEdit())
				throw securityException("Cannot edit Online Event");
			
			// Save access
			ctx.setAttribute("access", access, REQUEST);
			
			// Get the briefing file
			FileUpload bf = ctx.getFile("briefPDF");
			boolean deletePDF = Boolean.valueOf(ctx.getParameter("deleteBrief")).booleanValue();
			if ((bf != null) && PDFUtils.isPDF(bf.getBuffer())) {
				Briefing b = new Briefing(bf.getBuffer());
				e.setBriefing(b);
			} else if (!deletePDF) {
				String btxt = ctx.getParameter("briefing");
				if (!StringUtils.isEmpty(btxt))
					e.setBriefing(new Briefing(btxt));
				else
					e.setBriefing(null);
			} else
				e.setBriefing(null);

			// Populate fields from the request
			e.setNetwork(EnumUtils.parse(OnlineNetwork.class, ctx.getParameter("network"), null));
			e.setCanSignup(Boolean.valueOf(ctx.getParameter("canSignup")).booleanValue());
			if (!e.getCanSignup() && !StringUtils.isEmpty(ctx.getParameter("signupURL")))
				e.setSignupURL(ctx.getParameter("signupURL"));
			else
				e.setSignupURL(null);
			
			// Get participating airlines
			Collection<String> aCodes = ctx.getParameters("airlines");
			if (aCodes != null) {
				e.getAirlines().clear();
				aCodes.stream().map(c -> SystemData.getApp(c)).filter(Objects::nonNull).forEach(e::addAirline);
			}
			
			// Get featured Airports
			Collection<String> faCodes = ctx.getParameters("featuredAirports"); final Event fe = e;
			if (faCodes != null) {
				e.getFeaturedAirports().clear();
				faCodes.stream().map(c -> SystemData.getAirport(c)).filter(Objects::nonNull).filter(a -> fe.getAirports().contains(a)).forEach(e::addFeaturedAirport);
			}
			
			// Parse the start/end/deadline times
			e.setStartTime(parseDateTime(ctx, "start", SystemData.get("time.date_format"), "HH:mm"));
			e.setEndTime(parseDateTime(ctx, "end", SystemData.get("time.date_format"), "HH:mm"));
			e.setSignupDeadline(e.getCanSignup() ? parseDateTime(ctx, "close", SystemData.get("time.date_format"), "HH:mm") : e.getStartTime());
			
			// Load initial flight route
			if (e.getCanSignup() && (ctx.getParameter("route") != null)) {
				Route r = new Route(0, ctx.getParameter("route"));
				r.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
				r.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
				r.setMaxSignups(StringUtils.parse(ctx.getParameter("maxSignups"), 0));
				r.setIsRNAV(Boolean.valueOf(ctx.getParameter("isRNAV")).booleanValue());
				r.setName(ctx.getParameter("routeName"));
				r.setActive(true);
				e.addRoute(r);
				
				// Add to the message context
				mctxt.addData("route", r);
				templateName = "EVENTCREATE";
			}
			
			// Get aircraft types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("allEQ", acdao.getAircraftTypes(), REQUEST);

			// Parse the equipment types
			Collection<String> eqTypes = ctx.getParameters("eqTypes");
			if (eqTypes != null) {
				e.getEquipmentTypes().clear();
				eqTypes.forEach(e::addEquipmentType);
			}

			// See which charts have been selected
			Collection<String> selectedCharts = ctx.getParameters("charts");
			if (selectedCharts != null) {
				Collection<Integer> chartIDs = selectedCharts.stream().map(id -> Integer.valueOf(StringUtils.parseHex(id))).collect(Collectors.toSet());

				// Load the charts
				e.getCharts().clear();
				GetChart cdao = new GetChart(con);
				e.addCharts(cdao.getByIDs(chartIDs));
			}
			
			// Parse contact addresses
			Collection<String> addrs = StringUtils.split(ctx.getParameter("contactAddrs"), "\n");
			if (!CollectionUtils.isEmpty(addrs)) {
				e.getContactAddrs().clear();
				addrs.forEach(e::addContactAddr);
			}

			// Save the event in the request
			ctx.setAttribute("event", e, REQUEST);
			
			// Check for a banner image
			boolean removeImg = Boolean.valueOf(ctx.getParameter("removeBannerImg")).booleanValue();
			FileUpload imgData = ctx.getFile("bannerImg");
			if (imgData != null) {
				// Check the image
				ImageInfo info = new ImageInfo(imgData.getBuffer());
				boolean imgOK = info.check();

				// Check the image dimensions
				int maxX = SystemData.getInt("online.banner_max.x");
				int maxY = SystemData.getInt("online.banner_max.y");
				if (imgOK && ((info.getWidth() > maxX) || (info.getHeight() > maxY))) {
					imgOK = false;
					ctx.setMessage("Your Banner Image is too large. (Max = " + maxX + "x" + maxY + ", Yours = " + info.getWidth() + "x" + info.getHeight());
				}
				
				// Check the image size
				int maxSize = SystemData.getInt("online.banner_max.size");
				if (imgOK && (imgData.getSize() > maxSize)) {
					imgOK = false;
					ctx.setMessage("Your Banner Image is too large. (Max = " + maxSize + "bytes, Yours =" + imgData.getSize() + " bytes");
				}
				
				// Return back to the JSP if banner invalid
				if (!imgOK) {
					ctx.release();
					
					// Convert the dates to local time for the input fields
					ZoneId tz = ctx.getUser().getTZ().getZone();
					ctx.setAttribute("startTime", ZonedDateTime.ofInstant(e.getStartTime(), tz), REQUEST);
					ctx.setAttribute("endTime", ZonedDateTime.ofInstant(e.getEndTime(), tz), REQUEST);
					ctx.setAttribute("signupDeadline", ZonedDateTime.ofInstant(e.getSignupDeadline(), tz), REQUEST);
					
					// Go to page
					CommandResult result = ctx.getResult();
					result.setURL("/jsp/event/eventEdit.jsp");
					result.setSuccess(true);
					return;
				}
				
				// Load the banner data
				e.load(imgData.getBuffer());
				ctx.setAttribute("bannerUpdated", Boolean.TRUE, REQUEST);
			}
			
			// Start a transaction
			ctx.startTX();
			
			// Write the event
			SetEvent wdao = new SetEvent(con);
			wdao.write(e);
			
			// Delete or update the banner image
			if (removeImg)
				wdao.deleteBanner(e.getID());
			else if (e.isLoaded() && !isNew)
				wdao.writeBanner(e);
			
			// Commit
			ctx.commitTX();

			// Get the DAO and save the event if we're not refreshing
			if (isNew) {
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctxt.setTemplate(mtdao.get(templateName));
				mctxt.addData("event", e);
				mctxt.setSubject("Online Event - " + e.getName());

				// Save the start/end/signup dates
				mctxt.addData("startDateTime", StringUtils.format(e.getStartTime(), "MM/dd/yyyy HH:mm"));
				mctxt.addData("endDateTime", StringUtils.format(e.getEndTime(), "MM/dd/yyyy HH:mm"));
				mctxt.addData("signupDeadline", StringUtils.format(e.getSignupDeadline(), "MM/dd/yyyy HH:mm"));

				// Get the Pilots to notify
				GetPilotNotify pdao = new GetPilotNotify(con);
				Collection<EMailAddress> pilots = pdao.getNotifications(Notification.EVENT);
				if (pilots != null) {
					e.getContactAddrs().forEach(addr -> pilots.add(MailUtils.makeAddress(addr)));
					
					Mailer mailer = new Mailer(ctx.getUser());
					mailer.setContext(mctxt);
					mailer.send(pilots);
				}
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/event/eventUpdate.jsp");
		result.setSuccess(true);
	}
}