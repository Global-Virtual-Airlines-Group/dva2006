// Copyright 2005, 2006, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.gallery;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.gallery.Image;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.GalleryAccessControl;

import org.deltava.util.*;

/**
 * A Web Site Command to display the Fleet Gallery.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class FleetGalleryCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
    public void execute(CommandContext ctx) throws CommandException {
    	
    	// Determining if we're opening the admin view
    	boolean doAdmin = Boolean.valueOf((String) ctx.getCmdParameter(ID, null)).booleanValue();

        List<Image> results = new ArrayList<Image>();
        try {
            Connection con = ctx.getConnection();

            // Get the fleet gallery
            GetGallery dao = new GetGallery(con);
            results.addAll(dao.getFleetGallery());
            
			// Load the Image Authors
            Collection<Integer> authorIDs = results.stream().map(Image::getAuthorID).collect(Collectors.toSet());
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(authorIDs, "PILOTS"), REQUEST);
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Get command results
        CommandResult result = ctx.getResult();
        if (doAdmin) {
    		ctx.setAttribute("sortOptions", GalleryCommand.SORT_OPTS, REQUEST);

    		// Calculate access control for new Images
    		GalleryAccessControl access = new GalleryAccessControl(ctx, null);
    		access.validate();
    		ctx.setAttribute("access", access, REQUEST);
    		
    		// Initialize the view context
    		ViewContext<Image> vctx = initView(ctx, Image.class);
    		vctx.setResults(results);

    		// Forward to the JSP
    		result.setURL("/jsp/gallery/imageList.jsp");
    		result.setSuccess(true);
    		return;
        }
        
        // Build the description array
        List<String> descs = new ArrayList<String>();
        for (Image img : results)
            descs.add(StringUtils.stripInlineHTML(img.getDescription()));
        
        // Save the results and description array
        ctx.setAttribute("fleetGallery", results, REQUEST);
        ctx.setAttribute("fleetGalleryDesc", descs, REQUEST);
        
        // Redirect to the display page
        result.setURL("/jsp/gallery/fleet.jsp");
        result.setSuccess(true);
    }
}