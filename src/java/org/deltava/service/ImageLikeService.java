// Copyright 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import java.sql.Connection;

import org.json.*;

import org.deltava.beans.gallery.Image;
import org.deltava.dao.*;

import org.deltava.security.command.GalleryAccessControl;

import org.deltava.util.*;

/**
 * A voting service for Image Gallery images. 
 * @author Luke
 * @version 7.3
 * @since 5.0
 */

public class ImageLikeService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Create the response
		JSONObject jo = new JSONObject();
		
		try {
			Connection con = ctx.getConnection();
			GetGallery idao = new GetGallery(con);
			Image img = idao.getImageData(StringUtils.parseHex(ctx.getParameter("id")));
			if (img == null)
				throw new ServiceException(SC_NOT_FOUND, "Invalid Image ID", false);

			// Check our access
			GalleryAccessControl ac = new GalleryAccessControl(ctx, img);
			ac.validate();
			
			// Check if we're liking
			boolean isVoting = Boolean.valueOf(ctx.getParameter("like")).booleanValue();
			if (isVoting && ac.getCanLike()) {
				SetGalleryImage iwdao = new SetGalleryImage(con);
				iwdao.like(ctx.getUser().getID(), img.getID());
				img.addLike(ctx.getUser().getID());
			}
			
			// Set response
			jo.put("likes", img.getLikeCount());
			jo.put("mine", img.hasLiked(ctx.getUser()));
			jo.put("canLike", (!isVoting && ac.getCanLike()));
		} catch (NullPointerException npe) {
			return SC_BAD_REQUEST;
		} catch (DAOException de) {
			throw new ServiceException(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_INTERNAL_SERVER_ERROR, "I/O Error", false);
		}
			
		return SC_OK;
	}
}