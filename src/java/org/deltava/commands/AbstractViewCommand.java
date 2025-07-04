// Copyright 2005, 2008, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

import org.deltava.util.system.SystemData;

/**
 * A class to support web site commands for pageable table views.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public abstract class AbstractViewCommand extends AbstractCommand {

	/**
	 * Initializes the view context for a command invocation and saves it in the request.
	 * @param ctx the Command context
	 * @param c the result type Class
	 * @param defaultSize the default view page size if not specified
	 * @return the view context
	 */
    protected static <T extends Object> ViewContext<T> initView(CommandContext ctx, Class<T> c, int defaultSize) {
        
        ViewContext<T> vctx = new ViewContext<T>(ctx.getRequest(), defaultSize);
        ctx.setAttribute(ViewContext.VIEW_CONTEXT, vctx, REQUEST);
        return vctx;
    }
    
    /**
     * Initializes the view context for a command invocation, with the default view page size, either for the system or
     * for the authenticated user (if any).
     * @param ctx the Command context
     * @param c the result type Class
     * @return the View context
     */
    protected static <T extends Object> ViewContext<T> initView(CommandContext ctx, Class<T> c) {
    	
    	int defaultSize = ctx.isAuthenticated() ? ctx.getUser().getViewCount() : SystemData.getInt("html.table.viewSize"); 
    	return initView(ctx, c, defaultSize);
    }
}