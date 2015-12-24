// Copyright 2005, 2008, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

import org.deltava.util.system.SystemData;

/**
 * A class to support web site commands for pageable table views.
 * @author Luke
 * @version 6.3
 * @since 1.0
 */

public abstract class AbstractViewCommand extends AbstractCommand {

	/**
	 * Initializes the view context for a command invocation and saves it in the request.
	 * @param ctx the Command context
	 * @param defaultSize the default view page size if not specified
	 * @return the view context
	 */
    protected static ViewContext initView(CommandContext ctx, int defaultSize) {
        
        // Get start/count/sortType
        ViewContext vctx = new ViewContext(ctx.getRequest(), defaultSize);
        ctx.setAttribute(ViewContext.VIEW_CONTEXT, vctx, REQUEST);
        return vctx;
    }
    
    /**
     * Initializes the view context for a command invocation, with the default view page size
     * @param ctx the Command context
     * @return the View context
     */
    protected static ViewContext initView(CommandContext ctx) {
    	
    	// Get the default view size for the user if authenticated
    	int defaultSize = ctx.isAuthenticated() ? ctx.getUser().getViewCount() : SystemData.getInt("html.table.viewSize"); 
    	return initView(ctx, defaultSize);
    }
}