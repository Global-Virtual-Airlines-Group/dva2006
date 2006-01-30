// Copyright (c) 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;
import org.deltava.commands.CommandException;

/**
 * A class to support dynamic access calculators for commands. Subclasses should implement boolean read-only
 * properties that can be called by the display JSP via the JSP Expression Language to determine what options
 * to display.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class AccessControl {

    /**
     * The security context to operate on.
     */
    protected SecurityContext _ctx;
    
    /**
     * Initializes the Access Controller using a specific command context.
     * @param ctx the command context
     */
    public AccessControl(SecurityContext ctx) {
        super();
        _ctx = ctx;
    }

    /**
     * Validates the command context for this access controller. The validate() method should
     * call this method first.
     * @throws IllegalStateException if _ctx is null
     */
    protected void validateContext() {
        if (_ctx == null)
            throw new IllegalStateException("Command Context is empty");
    }
    
    /**
     * Calculates access control rights
     * @throws CommandException if no access at all is possible
     */
    public abstract void validate() throws CommandException;
}