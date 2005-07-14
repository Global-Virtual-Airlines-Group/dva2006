// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.gallery;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.gallery.Image;
import org.deltava.commands.*;

import org.deltava.dao.GetGallery;
import org.deltava.dao.GetPilot;
import org.deltava.dao.DAOException;

import org.deltava.security.command.GalleryAccessControl;

import org.deltava.util.ComboUtils;

/**
 * A Web Site Command to view the Image Gallery.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GalleryCommand extends AbstractViewCommand {
	
	private static final List _sortOptions = ComboUtils.fromArray(new String[] {"Image Date", "Feedback Count", "Average Score"},
			new String[] {"I.DATE DESC", "VC DESC", "SC DESC, VC DESC"});

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {

        // Get/set start/count parameters
        ViewContext vc = initView(ctx);
        vc.setDefaultSortType("I.DATE DESC");
        
        // Save sort options
        ctx.setAttribute("sortOptions", _sortOptions, REQUEST);

        try {
        	Connection con = ctx.getConnection();
        	
        	// Get the Gallery DAO
        	GetGallery dao = new GetGallery(con);
            dao.setQueryStart(vc.getStart());
            dao.setQueryMax(vc.getCount());

            // Get the images
            List results = dao.getPictureGallery(vc.getSortType(), (String) ctx.getCmdParameter(Command.OPERATION, null)); 
            vc.setResults(results);
            
            // Get all the Author IDs
            Set authorIDs = new HashSet();
            for (Iterator i = results.iterator(); i.hasNext(); ) {
            	Image img = (Image) i.next();
            	authorIDs.add(new Integer(img.getAuthorID()));
            }
            
            // Load the Image Authors
            GetPilot pdao = new GetPilot(con);
            ctx.setAttribute("pilots", pdao.getByID(authorIDs, "PILOTS"), REQUEST);
            
            // Save the list of months
            ctx.setAttribute("months", dao.getMonths(), REQUEST);
        } catch (DAOException de) {
        	throw new CommandException(de);
        } finally {
        	ctx.release();
        }
        
        // Calculate access control for new Images
        GalleryAccessControl access = new GalleryAccessControl(ctx, null);
        access.validate();
        ctx.setAttribute("access", access, REQUEST);
        
        // Forward to the JSP
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/gallery/imageList.jsp");
        result.setSuccess(true);
	}
}