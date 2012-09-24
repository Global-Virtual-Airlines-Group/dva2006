// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.IOException;
import java.sql.Connection;

import org.jdom2.*;

import org.deltava.beans.gallery.Image;
import org.deltava.dao.*;

import org.deltava.security.command.GalleryAccessControl;

import org.deltava.util.*;

/**
 * A voting service for Image Gallery images. 
 * @author Luke
 * @version 5.0
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
		Document doc = new Document();
		Element re = new Element("img");
		doc.setRootElement(re);
		
		try {
			int id = StringUtils.parseHex(ctx.getParameter("id"));
			Connection con = ctx.getConnection();
			GetGallery idao = new GetGallery(con);
			Image img = idao.getImageData(id);
			if (img == null)
				throw new ServiceException(SC_NOT_FOUND, "Invalid Image ID", false);

			// Check our access
			GalleryAccessControl ac = new GalleryAccessControl(ctx, img);
			ac.validate();
			
			// Check if we're liking
			boolean isVoting = Boolean.valueOf(ctx.getParameter("like")).booleanValue();
			if (isVoting) {
				if (ac.getCanLike()) {
					SetGalleryImage iwdao = new SetGalleryImage(con);
					iwdao.like(ctx.getUser().getID(), img.getID());
					img.addLike(ctx.getUser().getID());
				}
			}
			
			// Set response
			re.setAttribute("likes", String.valueOf(img.getLikeCount()));
			re.setAttribute("mine", String.valueOf(img.hasLiked(ctx.getUser())));
			re.setAttribute("canLike", String.valueOf(!isVoting && ac.getCanLike()));
		} catch (NullPointerException npe) {
			return SC_BAD_REQUEST;
		} catch (DAOException de) {
			throw new ServiceException(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Dump the XML to the output stream
		try {
			ctx.setContentType("text/xml", "UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_INTERNAL_SERVER_ERROR, "I/O Error", false);
		}
			
		return SC_OK;
	}
}