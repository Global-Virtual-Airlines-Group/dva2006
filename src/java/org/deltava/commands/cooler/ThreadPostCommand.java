// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.cooler.*;
import org.deltava.beans.gallery.Image;
import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.MultiUserSecurityContext;
import org.deltava.security.command.CoolerChannelAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle new Water Cooler message threads.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class ThreadPostCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Set result URL
		CommandResult result = ctx.getResult();

		// Get the user for the channel list
		Person p = ctx.getUser();

		// Get the default airline and channel name
		AirlineInformation airline = SystemData.getApp(SystemData.get("airline.code"));
		String cName = (String) ctx.getCmdParameter(Command.ID, "General Discussion");
		
		// Initialze the Mailer context
		Collection<Pilot> nPilots = new HashSet<Pilot>();
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		try {
			Connection con = ctx.getConnection();

			// Get the Pilot's airline
			GetUserData uddao = new GetUserData(con);
			UserData usrData = uddao.get(p.getID());
			if (usrData != null)
				airline = SystemData.getApp(usrData.getAirlineCode());

			// Get the channel DAO and the list of channels
			GetCoolerChannels dao = new GetCoolerChannels(con);
			List<Channel> channels = dao.getChannels(airline, ctx.getRoles());
			Channel ch = dao.get(cName);
			ctx.release();
			
			// Remove channels we cannot post to
			channels.remove(Channel.ALL);
			channels.remove(Channel.SHOTS);
			if (!ctx.isUserInRole("Modeator")) {
				for (Iterator<Channel> i = channels.iterator(); i.hasNext(); ) {
					Channel c = i.next();
					if (!c.getAllowNewPosts())
						i.remove();
				}
			}
			
			// Save channels
			ctx.setAttribute("channel", ch, REQUEST);
			ctx.setAttribute("channels", channels, REQUEST);

			// Initialize the channel access controller
			CoolerChannelAccessControl access = new CoolerChannelAccessControl(ctx, ch);
			access.validate();
			ctx.setAttribute("channelAccess", access, REQUEST);

			// Check to see if we can post in this channel
			if (!access.getCanPost())
				throw securityException("Cannot post in channel " + cName);

			// Check if we're doing a GET; if so jump to the create JSP
			if ("GET".equalsIgnoreCase(ctx.getRequest().getMethod())) {
				result.setURL("/jsp/cooler/threadCreate.jsp");
				result.setSuccess(true);
				return;
			}

			// Check if we are loading an image. If so, check the image size
			FileUpload img = ctx.getFile("img");
			if (img != null) {
				ImageInfo imgInfo = new ImageInfo(img.getBuffer());
				boolean imgOK = imgInfo.check();

				// Save image info
				ctx.setAttribute("imgSize", Integer.valueOf(img.getSize()), REQUEST);
				ctx.setAttribute("imgX", Integer.valueOf(imgInfo.getWidth()), REQUEST);
				ctx.setAttribute("imgY", Integer.valueOf(imgInfo.getHeight()), REQUEST);

				// Validate the image
				boolean badSize = img.getSize() > SystemData.getInt("cooler.img_max.size");
				boolean badDim = (imgInfo.getHeight() > SystemData.getInt("cooler.img_max.y"))
						|| (imgInfo.getWidth() > SystemData.getInt("cooler.img_max.x"));

				// If the image is too big, figure out what to do
				if (!imgOK || badSize || badDim) {
					ctx.setAttribute("imgInvalid", Boolean.valueOf(!imgOK), REQUEST);
					ctx.setAttribute("imgBadSize", Boolean.valueOf(badSize), REQUEST);
					ctx.setAttribute("imgBadDim", Boolean.valueOf(badDim), REQUEST);

					// Redirect back to the JSP
					result.setURL("/jsp/cooler/threadCreate.jsp");
					result.setSuccess(true);
					return;
				}
			}

			// If we have no subject, redirect back
			if (StringUtils.isEmpty(ctx.getParameter("subject"))) {
				result.setURL("/jsp/cooler/threadCreate.jsp");
				result.setSuccess(true);
				return;
			}

			// Create the new thread bean
			MessageThread mt = new MessageThread(ctx.getParameter("subject"));
			mt.setChannel(cName);
			mt.setAuthorID(ctx.getUser().getID());

			// Parse the sticky date
			if (!StringUtils.isEmpty(ctx.getParameter("stickyDate"))) 
				mt.setStickyUntil(parseDateTime(ctx, "sticky", "MM/dd/yyyy", null));
			
			// Check if we are adding linked Images - parse the JSON and add
			if (!StringUtils.isEmpty(ctx.getParameter("imgData"))) {
				try {
					JSONObject jo = new JSONObject(ctx.getParameter("imgData"));
					JSONArray jURLs = jo.optJSONArray("URLs");
					JSONArray jDescs = jo.optJSONArray("descs");
					for (int x = 0; x < jURLs.length(); x++) {
						LinkedImage li = new LinkedImage(x+1, String.valueOf(jURLs.get(x)));
						li.setDescription(String.valueOf(jDescs.get(x)));
						mt.addImageURL(li);
					}
				} catch (JSONException je) {
					throw new CommandException(je);
				}
			}

			// Create the Pilot poll
			boolean hasPoll = Boolean.valueOf(ctx.getParameter("hasPoll")).booleanValue();
			if (hasPoll) {
				Collection<String> opts = StringUtils.split(ctx.getParameter("pollOptions"), "\n");
				for (Iterator<String> i = opts.iterator(); i.hasNext();)
					mt.addOption(new PollOption(1, i.next()));
			}

			// Create the first post in the thread
			Message msg = new Message(p.getID());
			msg.setRemoteAddr(ctx.getRequest().getRemoteAddr());
			msg.setRemoteHost(ctx.getRequest().getRemoteHost());
			msg.setBody(ctx.getParameter("msgText"));
			mt.addPost(msg);
			
			// Grab the connection back and start the transaction
			con = ctx.getConnection();
			ctx.startTX();

			// Write the image to the gallery if we have one
			if (img != null) {
				String forumName = SystemData.get("airline.forum");
				Image gImg = new Image(mt.getSubject(), forumName + " Screen Shot");
				gImg.setAuthorID(p.getID());
				gImg.setCreatedOn(Instant.now());
				gImg.load(img.getBuffer());

				// Save the image to the gallery
				SetGalleryImage imgdao = new SetGalleryImage(con);
				imgdao.write(gImg);

				// Save the image ID
				mt.setImage(gImg.getID());
				ctx.setAttribute("hasImage", Boolean.TRUE, REQUEST);
			}

			// Get the write DAO and write to the database
			SetCoolerMessage wdao = new SetCoolerMessage(con);
			wdao.write(mt);

			// Write any image links
			SetCoolerLinks lwdao = new SetCoolerLinks(con);
			lwdao.write(mt);

			// Create a notification entry if we requested on
			if (Boolean.valueOf(ctx.getParameter("updateNotify")).booleanValue()) {
				SetCoolerNotification nwdao = new SetCoolerNotification(con);
				nwdao.add(mt.getID(), ctx.getUser().getID());
				ctx.setAttribute("isNotify", Boolean.TRUE, REQUEST);
			}
			
			// Commit the transaction
			ctx.commitTX();
			
			// Load the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("THREADNOTIFY"));
			
			// Save thread data
			mctxt.addData("thread", mt);
			
			// Load users to notify
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			for (String appCode : ch.getAirlines()) {
				AirlineInformation ai = SystemData.getApp(appCode);
				for (String role : ch.getNotifyRoles())
					nPilots.addAll(pdao.getByRole(role, ai.getDB()));
			}
			
			// Filter notify users
			MultiUserSecurityContext sctx = new MultiUserSecurityContext(ctx);
			for (Iterator<Pilot> i = nPilots.iterator(); i.hasNext();) {
				Pilot usr = i.next();
				sctx.setUser(usr);

				// Validate this user's access to the channel
				access.updateContext(sctx);
				access.validate();
				if (!access.getCanAccess() || (usr.getStatus() != Pilot.ACTIVE))
					i.remove();
			}
			
			// Save the thread in the request
			ctx.setAttribute("thread", mt, REQUEST);
			ctx.setAttribute("isPosted", Boolean.TRUE, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Send the notification messages
		if (!nPilots.isEmpty()) {
			Mailer mailer = new Mailer(null);
			mailer.setContext(mctxt);
			mailer.send(nPilots);

			// Save notification message count
			ctx.setAttribute("notifyMsgs", Integer.valueOf(nPilots.size()), REQUEST);
		}

		// Forward to the JSP
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/cooler/threadUpdate.jsp");
		result.setSuccess(true);
	}
}