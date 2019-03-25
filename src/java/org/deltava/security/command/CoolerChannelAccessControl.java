// Copyright 2005, 2006, 2008, 2011, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

import org.deltava.beans.Pilot;
import org.deltava.beans.cooler.Channel;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * An Access Controller for Water Cooler channels.
 * @author Luke
 * @version 8.6
 * @since 1.0
 */

public class CoolerChannelAccessControl extends AccessControl {

    private final Channel _c;
    
    private boolean _canAccess;
    private boolean _canRead;
    private boolean _canEdit;
    private boolean _canPost;
    private boolean _canDelete;
    
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
     * Updates the Security Context verify access with. Since this Access controller may be called many times in
     * succession, it is more efficient to update the security context rather than constantly creating a new
     * CoolerChannelAccessControl object. 
     * @param ctx the SecurityContext
     */
    public void updateContext(SecurityContext ctx) {
       _ctx = ctx;
    }

    /**
     * Calculates access rights.
     */
    @Override
	public void validate() {
       validateContext();
       
       // Get the user
       Pilot usr = (Pilot) _ctx.getUser();
       
        // Validate that we can access the channel
        _canAccess = true;
        if (_c != null) {
        	_canAccess = RoleUtils.hasAccess(_ctx.getRoles(), _c.getReadRoles()) || RoleUtils.hasAccess(_ctx.getRoles(), _c.getWriteRoles());
        	if (!_ctx.isAuthenticated()) {
        		_canAccess &= _c.getAirlines().contains(SystemData.get("airline.code")); 
        		return;
        	}
        	
        	// Get the pilot code; if none use the default
        	UserID id = new UserID(usr.getPilotCode());
        	String airlineCode = StringUtils.isEmpty(usr.getPilotCode()) ? SystemData.get("airline.code") : id.getAirlineCode();
        	_canAccess &= _c.getAirlines().contains(airlineCode.toUpperCase());
        }
        
        // Check if we're locked out
        boolean isLocked = ((usr != null) && usr.getNoCooler());
        boolean isClosed = (_c != null) && !_c.getAllowNewPosts() && !_ctx.isUserInRole("Admin");
        
        // Set state objects
        _canEdit = _ctx.isUserInRole("Admin");
        if (_c != null) {
        	_canRead = _ctx.isUserInRole("HR");
        	_canPost = !isLocked && !isClosed && _canAccess && RoleUtils.hasAccess(_ctx.getRoles(), _c.getWriteRoles());
        	_canDelete = _ctx.isUserInRole("Admin") && (_c.getPostCount() == 0);
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
    
    /**
     * Returns if this Channel can be deleted.
     * @return TRUE if it can be deleted, otherwise FALSE
     */
    public boolean getCanDelete() {
    	return _canDelete;
    }
}