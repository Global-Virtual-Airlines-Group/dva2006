// Copyright 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.gallery;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.gallery.Image;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.GalleryAccessControl;

import org.deltava.util.*;

/**
 * A Web Site Command to display the Fleet Gallery.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public class FleetGalleryCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {
    	
    	// Determining if we're opening the admin view
    	boolean doAdmin = Boolean.valueOf((String) ctx.getCmdParameter(ID, null)).booleanValue();

        List<ComboAlias> results = null;
        try {
            Connection con = ctx.getConnection();

            // Get the fleet gallery
            GetGallery dao = new GetGallery(con);
            results = new ArrayList<ComboAlias>(dao.getFleetGallery());
            
			// Get all the Author IDs
			Set<Integer> authorIDs = new HashSet<Integer>();
			for (Iterator i = results.iterator(); i.hasNext();) {
				Image img = (Image) i.next();
				authorIDs.add(new Integer(img.getAuthorID()));
			}

			// Load the Image Authors
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
    		ViewContext vctx = initView(ctx);
    		vctx.setResults(results);

    		// Forward to the JSP
    		result.setURL("/jsp/gallery/imageList.jsp");
    		result.setSuccess(true);
    		return;
        }
        
        // Build the description array
        StringBuilder buf = new StringBuilder();
        for (Iterator i = results.iterator(); i.hasNext(); ) {
            Image img = (Image) i.next();
            buf.append(StringUtils.stripInlineHTML(img.getDescription()));
            if (i.hasNext())
                buf.append(',');
        }
        
        // Add <SELECT> combo entry
        results.add(0, ComboUtils.fromString("< SELECT AIRCRAFT >", ""));
        
        // Save the results and description array
        ctx.setAttribute("fleetGallery", results, REQUEST);
        ctx.setAttribute("fleetGalleryDesc", buf.toString(), REQUEST);
        
        // Redirect to the display page
        result.setURL("/jsp/gallery/fleet.jsp");
        result.setSuccess(true);
    }
}