// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

import org.deltava.beans.Pilot;
import org.deltava.beans.cooler.Channel;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * An Access Controller for Water Cooler channels.
 * @author Luke
 * @version 2.2
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
     */
    public void validate() {
       validateContext();
       
       // Get the user
       Pilot usr = (Pilot) _ctx.getUser();
       
        // Validate that we can access the channel
        _canAccess = true;
        if (_c != null) {
        	_canAccess = RoleUtils.hasAccess(_ctx.getRoles(), _c.getReadRoles()) || RoleUtils.hasAccess(_ctx.getRoles(), _c.getWriteRoles());
        	if (!_ctx.isAuthenticated())
        		return;
        	
        	// Get the pilot code; if none use the default
        	UserID id = new UserID(usr.getPilotCode());
        	String airlineCode = StringUtils.isEmpty(usr.getPilotCode()) ? SystemData.get("airline.code") : id.getAirlineCode();
        	_canAccess &= (_c.getAirlines().contains(airlineCode.toUpperCase()));
        }
        
        // Check if we're locked out
        boolean isLocked = ((usr != null) && usr.getNoCooler());
        boolean isClosed = (_c != null) && !_c.getAllowNewPosts() && !_ctx.isUserInRole("Admin");
        
        // Set state objects
        _canEdit = _ctx.isUserInRole("Admin");
        if (_c != null) {
        	_canRead = _ctx.isUserInRole("HR");
        	_canPost = !isLocked && !isClosed && _canAccess && RoleUtils.hasAccess(_ctx.getRoles(), _c.getWriteRoles());
        } else
        	_canPost = _ctx.isAuthenticated();
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