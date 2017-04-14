// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;
import java.time.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.cooler.*;
import org.deltava.beans.gallery.Image;
import org.deltava.beans.stats.Accomplishment;
import org.deltava.beans.system.*;
import org.deltava.commands.*;

import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;
import org.deltava.security.command.GalleryAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command for viewing Water Cooler discussion threads.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class ThreadCommand extends AbstractCommand {

	private static final List<String> SCORES = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
	private static final List<String> COLORS = Arrays.asList("blue", "black", "green", "red", "purple", "grey", "brown", "orange", "pink", "yellow");

	private static final int MIN_GALLERY_ID = 100;

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Determine if we are editing the last post
		boolean doEdit = "edit".equals(ctx.getCmdParameter(OPERATION, null));

		// Get the default airline
		MessageThread mt = null;
		AirlineInformation airline = SystemData.getApp(SystemData.get("airline.code"));
		try {
			Connection con = ctx.getConnection();

			// Get the Pilot's airline
			GetUserData uddao = new GetUserData(con);
			if (ctx.isAuthenticated()) {
				UserData usrData = uddao.get(ctx.getUser().getID());
				if (usrData != null)
					airline = SystemData.getApp(usrData.getAirlineCode());
			}

			// Get the Message Thread
			GetCoolerThreads tdao = new GetCoolerThreads(con);
			mt = tdao.getThread(ctx.getID(), true);
			if (mt == null)
				throw notFoundException("Unknown Message Thread - " + ctx.getID());
			
			// Get lastRead
			if (ctx.isAuthenticated()) {
				GetCoolerLastRead lrdao = new GetCoolerLastRead(con);
				Instant lastRead = lrdao.getLastRead(mt.getID(), ctx.getUser().getID());
				ctx.setAttribute("lastReadPostID", Integer.valueOf(mt.getNextPostID(lastRead)), REQUEST);
			}

			// Get the channel profile
			GetCoolerChannels cdao = new GetCoolerChannels(con);
			Channel c = cdao.get(mt.getChannel());
			
			// Load the poll options (if any)
			GetCoolerPolls tpdao = new GetCoolerPolls(con);
			mt.addOptions(tpdao.getOptions(mt.getID()));
			mt.addVotes(tpdao.getVotes(mt.getID()));
			if (!mt.getOptions().isEmpty()) {
				int maxVotes = 0;
				for (Iterator<PollOption> i = mt.getOptions().iterator(); i.hasNext(); ) {
					PollOption opt = i.next();
					maxVotes = Math.max(maxVotes, opt.getVotes());
				}
				
				ctx.setAttribute("barColors", COLORS, REQUEST);
				ctx.setAttribute("maxVotes", Integer.valueOf(maxVotes), REQUEST);
			}

			// Check user access
			CoolerThreadAccessControl ac = new CoolerThreadAccessControl(ctx);
			ac.updateContext(mt, c);
			ac.validate();
			if (!ac.getCanRead())
				throw securityException("Cannot read Message Thread " + ctx.getID());
			
			// Make sure we can edit the thread and save the last post content
			doEdit &= ac.getCanEdit();
			if (doEdit)
				ctx.setAttribute("lastPost", mt.getLastPost(), REQUEST);

			// If we have an image, load its metadata
			if (mt.getImage() >= MIN_GALLERY_ID) {
				// Figure out who started the thread, since the image will be in their gallery
				UserData aUsrData = uddao.get(mt.getAuthorID());
				AirlineInformation ai = SystemData.getApp((aUsrData == null) ? null : aUsrData.getAirlineCode());

				GetGallery irdao = new GetGallery(con);
				Image img = irdao.getImageData(mt.getImage(), ai.getDB());
				ctx.setAttribute("img", img, REQUEST);
				ctx.setAttribute("imgApp", ai, REQUEST);

				// Get our access to the image
				GalleryAccessControl imgAccess = new GalleryAccessControl(ctx, img);
				imgAccess.validate();
				ctx.setAttribute("imgAccess", imgAccess, REQUEST);
			} else if (mt.getImageURLs().isEmpty()) {
				final MessageThread thread = mt; 
				GetCoolerLinks ldao = new GetCoolerLinks(con);
				ldao.getURLs(mt.getID(), ctx.isUserInRole("Moderator")).forEach(li -> thread.addImageURL(li));
			}
			
			// Get the locations of the pilots writing updates
			Collection<Integer> updateIDs = new HashSet<Integer>();
			for (ThreadUpdate upd : mt.getUpdates())
				updateIDs.add(Integer.valueOf(upd.getAuthorID()));

			// Get the location of all the Pilots reporting/updating/posting in the thread and load all cross
			updateIDs.addAll(mt.getReportIDs());
			UserDataMap udm = uddao.getByThread(mt.getID());
			Collection<Integer> xdbIDs = udm.getAllIDs();
			xdbIDs.removeAll(udm.keySet());
			udm.putAll(uddao.get(xdbIDs));
			udm.putAll(uddao.get(updateIDs));
			
			// Save the user data in the request
			ctx.setAttribute("userData", udm, REQUEST);
			ctx.setAttribute("userDomains", udm.getDomains(), REQUEST);

			// Get the DAOs
			GetPilot pdao = new GetPilot(con);
			GetApplicant adao = new GetApplicant(con);
			GetFlightReports prdao = new GetFlightReports(con);
			GetAcademyCourses acdao = new GetAcademyCourses(con);
			GetACARSDispatchStats dspstdao = new GetACARSDispatchStats(con);
			
			// Get the authors, accomploshments and online totals for each user
			GetAccomplishment accdao = new GetAccomplishment(con);
			Map<Integer, Person> users = new HashMap<Integer, Person>();
			Map<Integer, Collection<? extends Accomplishment>> accs = new HashMap<Integer, Collection<? extends Accomplishment>>();
			for (String dbTableName : udm.getTableNames()) {
				// Get the pilots/applicants from each table and apply their online totals and certifications
				if (UserData.isPilotTable(dbTableName)) {
					Map<Integer, Pilot> pilots = pdao.getByID(udm.getByTable(dbTableName), dbTableName);
					prdao.getOnlineTotals(pilots, dbTableName);
					dspstdao.getDispatchTotals(pilots);
					users.putAll(pilots);
					accs.putAll(accdao.get(pilots, dbTableName));
				} else
					users.putAll(adao.getByID(udm.getByTable(dbTableName), dbTableName));
			}
			
			// Get Flight Academy certifications
			boolean showAll = ctx.isUserInRole("HR") || ctx.isUserInRole("Instructor") || ctx.isUserInRole("AcademyAdmin") || ctx.isUserInRole("AcademyAudit");
			Map<Integer, Collection<String>> certs = acdao.getCertifications(udm.getAllIDs(), !showAll);
			for (Integer id : certs.keySet()) {
				Person cp = users.get(id);
				if ((cp != null) && (cp instanceof Pilot))
					((Pilot) cp).addCertifications(certs.get(id));
			}
			
			// Aggregate totals for pilots
			for (Person p : users.values()) {
				if (p instanceof Pilot) {
					Collection<Integer> ids = udm.get(p.getID()).getIDs();
					Pilot usr = (Pilot) p;
				
					// Add the totals
					int totalLegs = 0;
					double totalHours = 0;
					for (Iterator<Integer> ii = ids.iterator(); ii.hasNext(); ) {
						Integer userID = ii.next();
						Person p2 = users.get(userID);
						if (p2 instanceof Pilot) {
							Pilot usr2 = (Pilot) p2;
							totalLegs += usr2.getLegs();
							totalHours += usr2.getHours();
							usr.addCertifications(usr2.getCertifications());
						}
					}
					
					// Set the totals
					usr.setTotalHours(totalHours);
					usr.setTotalLegs(totalLegs);
				}
			}
			
			// Get statistics for all posters
			if (ctx.isUserInRole("Moderator")) {
				GetStatistics stdao = new GetStatistics(con);
				ctx.setAttribute("postStats", stdao.getCoolerStatistics(udm.keySet()), REQUEST);
			}
			
			// Get the thread notifications
			ThreadNotifications nt =  tdao.getNotifications(mt.getID());
			ctx.setAttribute("notify", nt, REQUEST);
			if (ctx.isAuthenticated())
				ctx.setAttribute("doNotify", Boolean.valueOf(nt.contains(ctx.getUser().getID())), REQUEST);

			// Mark the thread as being read
			ctx.startTX(); mt.view();
			SetCoolerMessage wdao = new SetCoolerMessage(con);
			wdao.viewThread(mt.getID());
			if (ctx.isAuthenticated())
				wdao.markRead(mt.getID(), ctx.getUser().getID());
			
			ctx.commitTX();

			// Save all channels in the thread for the move combobox
			if (ctx.isUserInRole("Moderator") || ctx.isUserInRole("HR")) {
				ctx.setAttribute("channel", c, REQUEST);
				boolean isAdmin = ctx.isUserInRole("Admin");
				Collection<Channel> channels = cdao.getChannels(isAdmin ? null : airline, isAdmin, isAdmin);
				channels.remove(Channel.ALL);
				channels.remove(Channel.SHOTS);
				ctx.setAttribute("channels", channels, REQUEST);
				
				// Load poster IP addreses
				GetIPLocation ipdao = new GetIPLocation(con);
				Map<String, IPBlock> addrInfo = new HashMap<String, IPBlock>();
				for (Iterator<Message> i = mt.getPosts().iterator(); i.hasNext(); ) {
					Message msg = i.next();
					IPBlock info = ipdao.get(msg.getRemoteAddr());
					if (info != null)
						addrInfo.put(msg.getRemoteAddr(), info);
				}
			
				ctx.setAttribute("addrInfo", addrInfo, REQUEST);
			}
			
			// Save the thread, pilots and access controller in the request
			ctx.setAttribute("thread", mt, REQUEST);
			ctx.setAttribute("access", ac, REQUEST);
			ctx.setAttribute("pilots", users, REQUEST);
			ctx.setAttribute("accomplishments", accs, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// If the sticky date is in the past, clear it
		if ((mt.getStickyUntil() != null) && (mt.getStickyUntil().toEpochMilli() < System.currentTimeMillis()))
			mt.setStickyUntil(null);
		
		// Save the sticky date in the user's time zone
		if (ctx.isUserInRole("Moderator") && (mt.getStickyUntil() != null))
			ctx.setAttribute("stickyDate", ZonedDateTime.ofInstant(mt.getStickyUntil(), ctx.getUser().getTZ().getZone()), REQUEST);

		// Save scores choices and if we are editing
		ctx.setAttribute("doEdit", Boolean.valueOf(doEdit), REQUEST);
		ctx.setAttribute("scores", SCORES, REQUEST);
		
		// Disable content filter if requested
		boolean forceFilter = Boolean.valueOf(ctx.getParameter("filter")).booleanValue();
		boolean noFilter = (!forceFilter) || ctx.isUserInRole("HR") || ctx.isUserInRole("Moderator");
		ctx.setAttribute("noFilter", Boolean.valueOf(noFilter), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/cooler/threadShow.jsp");
		result.setSuccess(true);
	}
}