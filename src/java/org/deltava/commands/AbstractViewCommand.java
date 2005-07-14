package org.deltava.commands;

/**
 * A class to support web site commands for pageable table views.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractViewCommand extends AbstractCommand {

    protected ViewContext initView(CommandContext ctx) {
        
        // Get start/count/sortType
        ViewContext vctx = new ViewContext(ctx.getRequest());
        
        // Save the start/end values
        ctx.setAttribute("viewStart", new Integer(vctx.getStart()), REQUEST);
        ctx.setAttribute("viewCount", new Integer(vctx.getCount()), REQUEST);
        
        // Save the view context in the request
        ctx.setAttribute(ViewContext.VIEW_CONTEXT, vctx, REQUEST);
        
        // Return the view context
        return vctx;
    }
}