// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.cooler.*;
import org.deltava.beans.gallery.Image;
import org.deltava.beans.system.*;
import org.deltava.commands.*;

import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;
import org.deltava.security.command.GalleryAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A web site command for viewing Water Cooler discussion threads.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class ThreadCommand extends AbstractCommand {

	private static final List<String> SCORES = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
	private static final List<String> COLORS = Arrays.asList("blue", "black", "green", "red", "purple", "grey", 
			"brown", "orange", "pink", "yellow");
	
	private static final int MIN_GALLERY_ID = 100;

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
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
			mt = tdao.getThread(ctx.getID());
			if (mt == null)
				throw notFoundException("Unknown Message Thread - " + ctx.getID());

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
				ctx.setAttribute("maxVotes", new Integer(maxVotes), REQUEST);
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
				String imgDB = (aUsrData == null) ? SystemData.get("airline.db") : aUsrData.getDB();

				GetGallery irdao = new GetGallery(con);
				Image img = irdao.getImageData(mt.getImage(), imgDB);
				ctx.setAttribute("img", img, REQUEST);
				ctx.setAttribute("imgDB", imgDB, REQUEST);

				// Get our access to the image
				GalleryAccessControl imgAccess = new GalleryAccessControl(ctx, img);
				imgAccess.validate();
				ctx.setAttribute("imgAccess", imgAccess, REQUEST);
			} else if (mt.getImageURLs().isEmpty()) {
				GetCoolerLinks ldao = new GetCoolerLinks(con);
				Collection<LinkedImage> imgURLs = ldao.getURLs(mt.getID());
				for (Iterator<LinkedImage> li = imgURLs.iterator(); li.hasNext(); )
					mt.addImageURL(li.next());
			}
			
			// Get the locations of the pilots writing updates
			Collection<Integer> updateIDs = new HashSet<Integer>();
			for (Iterator<ThreadUpdate> ui = mt.getUpdates().iterator(); ui.hasNext(); ) {
				ThreadUpdate upd = ui.next();
				updateIDs.add(new Integer(upd.getAuthorID()));
			}

			// Get the location of all the Pilots reporting/updating/posting in the thread and load all cross
			updateIDs.addAll(mt.getReportIDs());
			UserDataMap udm = uddao.getByThread(mt.getID());
			Collection<Integer> xdbIDs = udm.getAllIDs();
			xdbIDs.removeAll(udm.getIDs());
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
			
			// Get the authors and online totals for each user
			Map<Integer, Person> users = new HashMap<Integer, Person>();
			for (Iterator<String> i = udm.getTableNames().iterator(); i.hasNext();) {
				String dbTableName = i.next();

				// Get the pilots/applicants from each table and apply their online totals and certifications
				if (UserDataMap.isPilotTable(dbTableName)) {
					Map<Integer, Pilot> pilots = pdao.getByID(udm.getByTable(dbTableName), dbTableName);
					prdao.getOnlineTotals(pilots, dbTableName);
					users.putAll(pilots);
				} else {
					Map<Integer, Applicant> applicants = adao.getByID(udm.getByTable(dbTableName), dbTableName);
					users.putAll(applicants);
				}
			}
			
			// Get Flight Academy certifications 
			Map<Integer, Collection<String>> certs = acdao.getCertifications(udm.getAllIDs());
			for (Iterator<Integer> ci = certs.keySet().iterator(); ci.hasNext(); ) {
				Integer id = ci.next();
				Person cp = users.get(id);
				if ((cp != null) && (cp instanceof Pilot))
					((Pilot) cp).addCertifications(certs.get(id));
			}
			
			// Aggregate totals for pilots
			for (Iterator<Person> i = users.values().iterator(); i.hasNext(); ) {
				Person p = i.next();
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
				ctx.setAttribute("postStats", stdao.getCoolerStatistics(udm.getIDs()), REQUEST);
			}
			
			// Get the thread notifications
			ThreadNotifications nt =  tdao.getNotifications(mt.getID());
			ctx.setAttribute("notify", nt, REQUEST);
			if (ctx.isAuthenticated())
				ctx.setAttribute("doNotify", Boolean.valueOf(nt.contains(ctx.getUser().getID())), REQUEST);

			// Mark the thread as being read
			SetCoolerMessage wdao = new SetCoolerMessage(con);
			wdao.viewThread(mt.getID());
			mt.view();

			// Save all channels in the thread for the move combobox
			if (ctx.isUserInRole("Moderator")) {
				ctx.setAttribute("channel", c, REQUEST);
				Collection<Channel> channels = cdao.getChannels(airline, ctx.isUserInRole("Admin"));
				channels.remove(Channel.ALL);
				channels.remove(Channel.SHOTS);
				ctx.setAttribute("channels", channels, REQUEST);
			}
			
			// Save the thread, pilots and access controller in the request
			ctx.setAttribute("thread", mt, REQUEST);
			ctx.setAttribute("access", ac, REQUEST);
			ctx.setAttribute("pilots", users, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Mark this thread as read
		if (ctx.isAuthenticated() && ctx.isUserInRole("Pilot")) {
			Pilot p = (Pilot) ctx.getUser();
			
			@SuppressWarnings("unchecked")
			Map<Integer, Date> threadIDs = (Map<Integer, Date>) ctx.getSession().getAttribute(CommandContext.THREADREAD_ATTR_NAME);
			if (threadIDs == null) {
				threadIDs = new HashMap<Integer, Date>();
				ctx.setAttribute(CommandContext.THREADREAD_ATTR_NAME, threadIDs, SESSION);
			}
			
			// Determine the post to scroll to
			Date cutoff = threadIDs.get(new Integer(ctx.getID()));
			if (cutoff == null)
				cutoff = (Date) ctx.getSession().getAttribute(CommandContext.THREADREADOV_ATTR_NAME);
			if (cutoff == null)
				cutoff = p.getLastLogoff();
			if (cutoff == null)
				cutoff = p.getLastLogin();
			
			// Save unread list
			List<Boolean> unRead = new ArrayList<Boolean>(mt.getPostCount());
			ctx.setAttribute("unread", unRead, REQUEST);
			
			// Find the first unread post if we've read some
			boolean hasFirstUnread = false;
			Message msg = mt.getPosts().get(0);
			if (p.getShowNewPosts() && msg.getCreatedOn().before(cutoff)) {
				for (Iterator<Message> i = mt.getPosts().iterator(); i.hasNext(); ) {
					msg = i.next();
					boolean isUnread = msg.getCreatedOn().after(cutoff);
					unRead.add(Boolean.valueOf(isUnread));
					if (isUnread && !hasFirstUnread) {
						ctx.setAttribute("firstUnreadTime", msg.getCreatedOn(), REQUEST);
						hasFirstUnread = true;
					}
				}
			} else {
				for (int x = 0; x < mt.getPostCount(); x++)
					unRead.add(Boolean.FALSE);
			}
			
			// Add thread and save
			threadIDs.put(new Integer(ctx.getID()), new Date());
		}
		
		// If the sticky date is in the past, clear it
		if ((mt.getStickyUntil() != null) && (mt.getStickyUntil().getTime() < System.currentTimeMillis()))
			mt.setStickyUntil(null);
		
		// Save the sticky date in the user's time zone
		if (ctx.isUserInRole("Moderator") && (mt.getStickyUntil() != null)) {
			DateTime sdt = new DateTime(mt.getStickyUntil());
			sdt.convertTo(ctx.getUser().getTZ());
			ctx.setAttribute("stickyDate", sdt.getDate(), REQUEST);
		}

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