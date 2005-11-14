package org.deltava.commands.gallery;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.gallery.Image;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.ComboUtils;
import org.deltava.util.StringUtils;

/**
 * Command to display the Fleet Gallery.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public class FleetGalleryCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {

        List results = null;
        try {
            Connection con = ctx.getConnection();

            // Get the fleet gallery
            GetGallery dao = new GetGallery(con);
            results = dao.getFleetGallery();
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
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
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/gallery/fleet.jsp");
        result.setSuccess(true);
    }
}