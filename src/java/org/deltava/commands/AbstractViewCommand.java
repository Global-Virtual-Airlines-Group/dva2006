// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands;

import org.deltava.util.system.SystemData;

/**
 * A class to support web site commands for pageable table views.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class AbstractViewCommand extends AbstractCommand {

	/**
	 * Initializes the view context for a command invocation.
	 * @param ctx the Command context
	 * @param defaultSize the default view page size if not specified
	 * @return the view context
	 */
    protected ViewContext initView(CommandContext ctx, int defaultSize) {
        
        // Get start/count/sortType
        ViewContext vctx = new ViewContext(ctx.getRequest(), defaultSize);
        
        // Save the start/end values
        ctx.setAttribute("viewStart", new Integer(vctx.getStart()), REQUEST);
        ctx.setAttribute("viewCount", new Integer(vctx.getCount()), REQUEST);
        
        // Save the view context in the request
        ctx.setAttribute(ViewContext.VIEW_CONTEXT, vctx, REQUEST);
        
        // Return the view context
        return vctx;
    }
    
    /**
     * Initializes the view context for a command invocation, with the default view page size
     * @param ctx the Command context
     * @return the View context
     */
    protected ViewContext initView(CommandContext ctx) {
    	return initView(ctx, SystemData.getInt("html.table.viewSize"));
    }
}