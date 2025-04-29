// Copyright 2007, 2008, 2009, 2015, 2017, 2021, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.net.*;
import java.net.http.*;
import java.util.*;
import java.io.IOException;

import java.sql.Connection;
import java.time.Duration;

import org.deltava.beans.cooler.*;
import org.deltava.beans.system.VersionInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to link an Image to a Water Cooler discussion thread.
 * @author Luke
 * @version 11.6
 * @since 1.0
 */

public class LinkImageCommand extends AbstractCommand {
	
	private Collection<?> _imgMimeTypes;
	
	/**
	 * Initializes this command.
	 * @param cmdName the name of the command
	 * @throws IllegalStateException if the command has already been initialized
	 */
	@Override
	public void init(String id, String cmdName) {
		super.init(id, cmdName);
		_imgMimeTypes = (Collection<?>) SystemData.getObject("cooler.imgurls.mime_types");
	}

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
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
			Map<String, LinkedImage> urlMap = CollectionUtils.createMap(ldao.getURLs(mt.getID(), true), LinkedImage::getURL);
			Collection<String> imgURLs = new LinkedHashSet<String>(urlMap.keySet());
			
			// Validate the image
			LinkedImage img = null;
			try {
				URI url = new URI(ctx.getParameter("imgURL"));
				if (!(url.getScheme().startsWith("http")))
					throw new MalformedURLException();
				else if (imgURLs.contains(url.toString()))
					throw new MalformedURLException("Duplicate Image URL");
				
				// Init the HTTP client
				try (HttpClient hc = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build()) {
					HttpRequest req = HttpRequest.newBuilder().timeout(Duration.ofMillis(2500)).uri(url).header("user-agent", VersionInfo.getUserAgent()).HEAD().build();
				
					// Validate the result code
					HttpResponse<String> rsp = hc.send(req, HttpResponse.BodyHandlers.ofString());
					if (rsp.statusCode() == HttpURLConnection.HTTP_OK) {
						Optional<String> ct = rsp.headers().firstValue("Content-Type");
						String cType = ct.orElse("unknown");
						if (!_imgMimeTypes.contains(cType))
							ctx.setMessage("Invalid MIME type for " + url + " - " + cType);
						else {
							img = new LinkedImage(imgURLs.size() + 1, url.toString());
							img.setDescription(ctx.getParameter("desc"));
							imgURLs.add(url.toString());
						}
					} else
						ctx.setMessage("Invalid Image HTTP result code - " + rsp.statusCode());
				}
			} catch (MalformedURLException | URISyntaxException se) {
				ctx.setMessage("Invalid linked Image URL - " + ctx.getParameter("imageURL"));
			} catch (IOException | InterruptedException ie) {
				ctx.setMessage(String.format("%s - %s", ie.getClass().getSimpleName(), ie.getMessage()));
			}
			
			// If we don't have a linked image bean, abort
			if (img == null) {
				ctx.release();
				result.setURL("thread", null, ctx.getID());
				result.setType(ResultType.REDIRECT);
				result.setSuccess(true);
				return;
			}
			
			// Create the status message
			ThreadUpdate upd = new ThreadUpdate(mt.getID());
			upd.setAuthorID(ctx.getUser().getID());
			upd.setDescription("Added link to " + img.getURL());
			
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
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}