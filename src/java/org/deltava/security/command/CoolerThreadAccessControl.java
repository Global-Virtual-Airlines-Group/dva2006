// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import java.util.List;

import org.deltava.beans.Pilot;
import org.deltava.beans.cooler.*;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Water Cooler Threads.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public final class CoolerThreadAccessControl extends AccessControl {

    private MessageThread _mt;
    private Channel _c;
    private CoolerChannelAccessControl _cac;
    
    private boolean _canRead;
    private boolean _canReply;
    private boolean _canEdit;
    private boolean _canVote;
    private boolean _canReport;
    private boolean _canLock;
    private boolean _canUnlock;
    private boolean _canUnstick;
    private boolean _canDelete;
    
	/**
	 * Initializes the controller.
	 * @param ctx the Command context
	 */
    public CoolerThreadAccessControl(SecurityContext ctx) {
        super(ctx);
    }
    
    /**
     * Update the MessageThread to verify access to. Since this Access controller may be called many times in
     * succession, it is more efficient to update the thread and channel rather than constantly creating a new
     * CoolerThreadAccessControl object. 
	 * @param t the Water Cooler message thread
	 * @param c the Water Cooler Channel
	 * @see CoolerThreadAccessControl#updateContxt(SecurityContext)
     */
    public void updateContext(MessageThread t, Channel c) {
        _mt = t;
        _c = c;
        _cac = new CoolerChannelAccessControl(_ctx, _c);
    }
    
    /**
     * Updates the Security Context verify access with. Since this Access controller may be called many times in
     * succession, it is more efficient to update the security context rather than constantly creating a new
     * CoolerThreadAccessControl object. 
     * @param ctx the Command context
     * @see CoolerThreadAccessControl#updateContext(MessageThread, Channel)
     */
    public void updateContxt(SecurityContext ctx) {
       _ctx = ctx;
       _cac = new CoolerChannelAccessControl(ctx, _c);
    }

    /**
     * Calculates access rights.
     * @throws IllegalStateException if the message thread or cooler channel were not set
     */
    public void validate() {
        validateContext();
        
        // Validate that the thread has been set
        if (_mt == null)
            throw new IllegalStateException("Mesage Thread not set");
        
        // Validate our channel access
        _cac.validate(); 
        boolean channelAccess = _cac.getCanAccess();
        
        // Get the user
        Pilot usr = (Pilot) _ctx.getUser();
        boolean isLockedOut = ((usr != null) && usr.getNoCooler());
        
        // Get the roles and role state - we assume it's OK if channel is null
        boolean isOurs = (_ctx.getUser() != null) && (_mt.getAuthorID() == _ctx.getUser().getID());
        boolean isModerator = _ctx.isUserInRole("Moderator");
        boolean isClosed = _mt.getLocked() || _mt.getHidden() || isLockedOut;
        boolean hasVoted = (_ctx.getUser() != null) && _mt.hasVoted(_ctx.getUser().getID());
        
        // Validate if we can read the thread
        _canRead = _ctx.isUserInRole("Admin") || (channelAccess && !_mt.getHidden());
        _canReply = _ctx.isAuthenticated() && _canRead && (isModerator || !isClosed);
        _canVote = _canReply && !_mt.getOptions().isEmpty() && !hasVoted;
        _canLock = channelAccess && !isClosed && isModerator;
        _canUnlock = channelAccess && isClosed && isModerator;
        _canUnstick = channelAccess && (_mt.getStickyUntil() != null) && ((!isClosed && isOurs) || isModerator);
        _canDelete = _ctx.isUserInRole("Admin");
        _canReport = _canReply && (!isClosed) && (!_mt.getReportIDs().contains(new Integer(_ctx.getUser().getID())));
        
        // Check if we can update the thread - ie. we have written the last reply and we can edit
        if (_canReply && (!_mt.getPosts().isEmpty())) {
        	List<Message> posts = _mt.getPosts();
        	Message msg = posts.get(posts.size() - 1);
        	_canEdit = (msg.getAuthorID() == _ctx.getUser().getID());
        	_canDelete |= (isOurs && (posts.size() == 1));
        }
    }
    
    /**
     * Returns if the thread can be read.
     * @return TRUE if it can be read, otherwise FALSE
     */
    public boolean getCanRead() {
        return _canRead;
    }

	/**
     * Returns if the thread can be replied to, or a new post can be made in the Channel.
     * @return TRUE if it can be replied to, otherwise FALSE
     */
    public boolean getCanReply() {
        return _canReply;
    }
    
    /**
     * Returns wether the thread can be reported for content.
     * @return TRUE if it can be reported, otherwise FALSE
     */
    public boolean getCanReport() {
    	return _canReport;
    }
    
    /**
     * Returns if the last post can be edited.
     * @return TRUE if the post can be edited, otherwise FALSE
     */
    public boolean getCanEdit() {
    	return _canEdit;
    }
    
    /**
     * Returns if the user can cast a vote in the poll.
     * @return TRUE if a vote can be cast, otherwise FALSE
     */
    public boolean getCanVote() {
    	return _canVote;
    }
    
    /**
     * Returns if the thread can be locked.
     * @return TRUE if it can be locked, otherwise FALSE
     */
    public boolean getCanLock() {
        return _canLock;
    }
    
    /**
     * Returns if the thread can be unlocked.
     * @return TRUE if it can be unlocked, otherwise FALSE
     */
    public boolean getCanUnlock() {
        return _canUnlock;
    }
    
    /**
     * Returns if the thread can be unstuck.
     * @return TRUE if the thread can be unstuck, otherwise FALSE
     */
    public boolean getCanUnstick() {
    	return _canUnstick;
    }
    
    /**
     * Returns if the thread can be deleted.
     * @return TRUE if the thread can be deleted, otherwise FALSE
     */
    public boolean getCanDelete() {
       return _canDelete;
    }
}