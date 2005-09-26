// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;
import org.deltava.commands.CommandSecurityException;

import org.deltava.beans.cooler.Channel;

import org.deltava.util.RoleUtils;
import org.deltava.util.system.SystemData;

/**
 * An Access Controller for Water Cooler channels.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CoolerChannelAccessControl extends AccessControl {

    private Channel _c;
    
    private boolean _canAccess;
    private boolean _canRead;
    private boolean _canEdit;
    private boolean _canPost;
    
    /**
     * Initializes the Access Controller.
     * @param ctx the Command Context.
     * @param c the Channel
     */
    public CoolerChannelAccessControl(SecurityContext ctx, Channel c) {
        super(ctx);
        _c = c;
    }

    /**
     * Calculates access rights.
     * @throws CommandSecurityException never
     */
    public void validate() throws CommandSecurityException {
       validateContext();
       
       // Validate airlines
       if ((_c != null) && (!_c.getAirlines().contains(SystemData.get("airline.code"))))
    	   return;

        // Set state objects
        _canAccess = (_c == null) ? true : RoleUtils.hasAccess(_ctx.getRoles(), _c.getRoles());
        _canPost = _canAccess && _ctx.isAuthenticated();
        _canEdit = _ctx.isUserInRole("Admin");
        _canRead = (_c != null) && _canEdit;
    }
    
    /**
     * Returns if the Channel can be accessed.
     * @return TRUE if a thread listing can be retrieved, otherwise FALSE
     */
    public boolean getCanAccess() {
        return _canAccess;
    }
    
    /**
     * Returns if the Channel Profile can be read.
     * @return TRUE if the profile can be retrieved, otherwise FALSE
     */
    public boolean getCanRead() {
        return _canRead;
    }
    
    /**
     * Returns if the Channel Profile can be edited.
     * @return TRUE if the profile can be edited, otherwise FALSE
     */
    public boolean getCanEdit() {
        return _canEdit;
    }
    
    /**
     * Returns if new Threads can be made in this Channel.
     * @return TRUE if new posts can be made, otherwise FALSE
     */
    public boolean getCanPost() {
        return _canPost;
    }
}