// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.net.*;
import java.util.*;
import java.io.IOException;

import java.sql.Connection;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;

import org.deltava.beans.cooler.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to link an Image to a Water Cooler discussion thread.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class LinkImageCommand extends AbstractCommand {
	
	private Collection _imgMimeTypes;
	private final HttpConnectionParams _hcp = new HttpConnectionParams();
	
	/**
	 * Initializes this command.
	 * @param cmdName the name of the command
	 * @throws CommandException if the command name is null
	 * @throws IllegalStateException if the command has already been initialized
	 */
	public void init(String id, String cmdName) throws CommandException {
		super.init(id, cmdName);
		_imgMimeTypes = (Collection) SystemData.getObject("cooler.imgurls.mime_types");
		_hcp.setConnectionTimeout(2000);
		_hcp.setTcpNoDelay(false);
		_hcp.setSoTimeout(2500);
	}

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the command results
		CommandResult result = ctx.getResult();
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the Message Thread
			GetCoolerThreads tdao = new GetCoolerThreads(con);
			MessageThread mt = tdao.getThread(ctx.getID());
			if (mt == null)
				throw notFoundException("Unknown Message Thread - " + ctx.getID());
			
			// Get the Channel
			GetCoolerChannels chdao = new GetCoolerChannels(con);
			Channel ch = chdao.get(mt.getChannel());
			if (ch == null)
				throw notFoundException("Unknown Channel - " + mt.getChannel());
			
			// Validate our access
			CoolerThreadAccessControl access = new CoolerThreadAccessControl(ctx);
			access.updateContext(mt, ch);
			access.validate();
			if (!access.getCanAddImage())
				throw securityException("Cannot Link Image");
			
			// Get the linked images
			GetCoolerLinks ldao = new GetCoolerLinks(con);
			Map<String, LinkedImage> urlMap = CollectionUtils.createMap(ldao.getURLs(mt.getID()), "URL");
			Collection<String> imgURLs = new LinkedHashSet<String>(urlMap.keySet());
			
			// Validate the image
			LinkedImage img = null;
			try {
				URL url = new URL(ctx.getParameter("imgURL"));
				if (!(url.getProtocol().startsWith("http")))
					throw new MalformedURLException();
				else if (imgURLs.contains(url.toString()))
					throw new MalformedURLException("Duplicate Image URL");
				
				// Open the connection
				HttpClient hc = new HttpClient();
				HeadMethod hm = new HeadMethod(url.toExternalForm());
				hm.setFollowRedirects(false);
				
				// Validate the result code
				int resultCode = hc.executeMethod(hm);
				if (resultCode == HttpURLConnection.HTTP_OK) {
					Header[] hdrs = hm.getResponseHeaders("Content-Type");
					String cType = (hdrs.length == 0) ? "unknown" : hdrs[0].getValue();
					if (!_imgMimeTypes.contains(cType))
						ctx.setMessage("Invalid MIME type for " + url + " - " + cType);
					else {
						img = new LinkedImage(imgURLs.size() + 1, url.toString());
						img.setDescription(ctx.getParameter("desc"));
						imgURLs.add(url.toString());
					}
				} else
					ctx.setMessage("Invalid Image HTTP result code - " + resultCode);
			} catch (MalformedURLException mue) {
				ctx.setMessage("Invalid linked Image URL - " + ctx.getParameter("imageURL"));
			} catch (IOException ie) {
				ctx.setMessage("I/O Error - " + ie.getMessage());
			}
			
			// If we don't have a linked image bean, abort
			if (img == null) {
				ctx.release();
				result.setURL("thread", null, ctx.getID());
				result.setType(CommandResult.REDIRECT);
				result.setSuccess(true);
				return;
			}
			
			// Create the status message
			ThreadUpdate upd = new ThreadUpdate(mt.getID());
			upd.setAuthorID(ctx.getUser().getID());
			upd.setMessage("Added link to " + img.getURL());
			
			// Start a JDBC transaction
			ctx.startTX();
			
			// Write the image
			SetCoolerLinks wdao = new SetCoolerLinks(con);
			wdao.add(mt.getID(), img);
			
			// Write the status message
			SetCoolerMessage mwdao = new SetCoolerMessage(con);
			mwdao.write(upd);
			
			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward back to the thread
		result.setURL("thread", null, ctx.getID());
		result.setType(CommandResult.REDIRECT);
		result.setSuccess(true);
	}
}