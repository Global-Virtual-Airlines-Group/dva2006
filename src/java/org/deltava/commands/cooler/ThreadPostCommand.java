// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.awt.Dimension;

import java.util.*;
import java.net.*;
import java.io.IOException;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.cooler.*;
import org.deltava.beans.gallery.Image;
import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerChannelAccessControl;

import org.deltava.util.*;
import org.deltava.util.http.HttpTimeoutHandler;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle new Water Cooler message threads.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ThreadPostCommand extends AbstractCommand {

	private static Collection _imgMimeTypes;

	/* private static final String[] IMG_OPTIONS = { "Let me resize the Image", "Resize the Image automatically" };
	 * private static final String[] IMG_ALIASES = { "0", "1" };
	 * private static final int IMG_REJECT = 0; 
	 * private static final int IMG_RESIZE = 1;
	 */

	/**
	 * Initializes this command.
	 * @param cmdName the name of the command
	 * @throws CommandException if the command name is null
	 * @throws IllegalStateException if the command has already been initialized
	 */
	public void init(String id, String cmdName) throws CommandException {
		super.init(id, cmdName);
		_imgMimeTypes = (Collection) SystemData.getObject("cooler.imgurls.mime_types");
	}

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Set result URL
		CommandResult result = ctx.getResult();

		// Get the user for the channel list
		Person p = ctx.getUser();

		// Get the default airline
		AirlineInformation airline = SystemData.getApp(SystemData.get("airline.code"));

		// Get the channel name
		String cName = (String) ctx.getCmdParameter(Command.ID, "General Discussion");
		//ctx.setAttribute("imgOpts", ComboUtils.fromArray(IMG_OPTIONS, IMG_ALIASES), REQUEST);

		try {
			Connection con = ctx.getConnection();

			// Get the Pilot's airline
			GetUserData uddao = new GetUserData(con);
			if (p != null) {
				UserData usrData = uddao.get(p.getID());
				if (usrData != null)
					airline = SystemData.getApp(usrData.getAirlineCode());
			}

			// Get the channel DAO and the list of channels
			GetCoolerChannels dao = new GetCoolerChannels(con);
			List<Channel> channels = dao.getChannels(airline, ctx.getRoles());
			Channel ch = dao.get(cName);
			channels.remove(Channel.ALL);
			channels.remove(Channel.SHOTS);
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

			// Check if we are adding a linked Image and returning back
			boolean addImage = Boolean.valueOf(ctx.getParameter("addImage")).booleanValue();
			if (addImage) {
				@SuppressWarnings("unchecked")
				Collection<LinkedImage> imgURLs = (Collection) ctx.getRequest().getSession().getAttribute("imageURLs");
				if (imgURLs == null) {
					imgURLs = new LinkedHashSet<LinkedImage>();
					ctx.setAttribute("imageURLs", imgURLs, SESSION);
				}

				// Validate the image
				try {
					URL url = new URL(null, ctx.getParameter("imgURL"), new HttpTimeoutHandler(1750));
					HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
					urlcon.setRequestMethod("HEAD");
					urlcon.connect();

					// Validate the result code
					int resultCode = urlcon.getResponseCode();
					if (resultCode == HttpURLConnection.HTTP_OK) {
						String cType = urlcon.getHeaderField("Content-Type");
						if (!_imgMimeTypes.contains(cType))
							ctx.setMessage("Invalid MIME type for " + url + " - " + cType);
						else {
							LinkedImage img = new LinkedImage(imgURLs.size() + 1, url.toString());
							img.setDescription(ctx.getParameter("desc"));
							imgURLs.add(img);
						}
					} else
						ctx.setMessage("Invalid Image HTTP result code - " + resultCode);

					urlcon.disconnect();
				} catch (MalformedURLException mue) {
					ctx.setMessage("Invalid linked Image URL - " + ctx.getParameter("imageURL"));
				} catch (IOException ie) {
					ctx.setMessage("I/O Error - " + ie.getMessage());
				}

				// Redirect back to the page
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
				ctx.setAttribute("imgSize", new Integer(img.getSize()), REQUEST);
				ctx.setAttribute("imgX", new Integer(imgInfo.getWidth()), REQUEST);
				ctx.setAttribute("imgY", new Integer(imgInfo.getHeight()), REQUEST);

				// Validate the image
				boolean badSize = img.getSize() > SystemData.getInt("cooler.img_max.size");
				boolean badDim = (imgInfo.getHeight() > SystemData.getInt("cooler.img_max.y"))
						|| (imgInfo.getWidth() > SystemData.getInt("cooler.img_max.x"));

				// If the image is too big, figure out what to do
				/* int imgOpt = Integer.parseInt(ctx.getParameter("imgOption")); */
				if (!imgOK || badSize || (badDim /* && (imgOpt == IMG_REJECT ) */)) {
					ctx.setAttribute("imgInvalid", Boolean.valueOf(!imgOK), REQUEST);
					ctx.setAttribute("imgBadSize", Boolean.valueOf(badSize), REQUEST);
					ctx.setAttribute("imgBadDim", Boolean.valueOf(badDim), REQUEST);

					// Redirect back to the JSP
					result.setURL("/jsp/cooler/threadCreate.jsp");
					result.setSuccess(true);
					return;
				}

				// Resize the image - DISABLED
				/*
				 * if (badDim) { ImageScaler scaler = new ImageScaler(img.getBuffer());
				 * scaler.setImageSize(getNewImageSize(imgInfo.getWidth(), imgInfo.getHeight())); // Replace the
				 * FileUpload data try { img.load(new ByteArrayInputStream(scaler.scale("jpeg")));
				 * ctx.setAttribute("imgResized", Boolean.TRUE, REQUEST); } catch (IOException ie) { log.warn("Error
				 * scaling image - " + ie.getMessage(), ie); img = null; } }
				 */
			}

			// If we have no subject, redirect back
			if (StringUtils.isEmpty(ctx.getParameter("subject"))) {
				result.setURL("/jsp/cooler/threadCreate.jsp");
				result.setSuccess(true);
				return;
			}

			// Create the new thread bean
			MessageThread mt = new MessageThread(ProfanityFilter.filter(ctx.getParameter("subject")));
			mt.setChannel(cName);
			mt.setAuthorID(p.getID());

			// Parse the sticky date
			if (!StringUtils.isEmpty(ctx.getParameter("stickyDate"))) {
				try {
					mt.setStickyUntil(StringUtils.parseDate(ctx.getParameter("stickyDate"), "MM/dd/yyyy"));
				} catch (IllegalArgumentException iae) {
					CommandException ce = new CommandException(iae);
					ce.setLogStackDump(false);
					throw ce;
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
			msg.setContentWarning(ProfanityFilter.flag(msg.getBody()));
			mt.addPost(msg);

			// Load linked images
			Collection imgURLs = (Collection) ctx.getRequest().getSession().getAttribute("imageURLs");
			if (imgURLs != null) {
				for (Iterator i = imgURLs.iterator(); i.hasNext();)
					mt.addImageURL((LinkedImage) i.next());
			}

			// Start the transaction
			ctx.startTX();

			// Write the image to the gallery if we have one
			if (img != null) {
				Image gImg = new Image(mt.getSubject(), "Water Cooler Screen Shot");
				gImg.setAuthorID(p.getID());
				gImg.setCreatedOn(new Date());
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
			
			// Mark this thread as read
			@SuppressWarnings("unchecked")
			Map<Integer, Date> threadIDs = (Map<Integer, Date>) ctx.getSession().getAttribute(CommandContext.THREADREAD_ATTR_NAME);
			if (threadIDs == null) {
				threadIDs = new HashMap<Integer, Date>();
				ctx.setAttribute(CommandContext.THREADREAD_ATTR_NAME, threadIDs, SESSION);
			}

			// Add thread and save
			threadIDs.put(new Integer(mt.getID()), new Date());

			// Save the thread in the request
			ctx.setAttribute("thread", mt, REQUEST);
			ctx.setAttribute("isPosted", Boolean.TRUE, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Remove image URLs
		ctx.getRequest().getSession().removeAttribute("imageURLs");

		// Forward to the JSP
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/cooler/threadUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Helper method to calculate the new image size for a rescaled image
	 */
	public Dimension getNewImageSize(int imgX, int imgY) {

		// Figure out the scaling required to bring each dimension into compliance, and get the smallest one
		double scaleX = (imgX / SystemData.getInt("cooler.img_max.x"));
		double scaleY = (imgY / SystemData.getInt("cooler.img_max.y"));
		double scale = Math.min(scaleX, scaleY);

		// Generate the new dimensions
		return new Dimension((int) Math.round(imgX * scale) - 1, (int) Math.round(imgY * scale) - 1);
	}
}