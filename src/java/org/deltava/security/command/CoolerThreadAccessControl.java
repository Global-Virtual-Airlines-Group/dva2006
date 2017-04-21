// Copyright 2005, 2006, 2007, 2008, 2011, 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import java.util.List;

import org.deltava.beans.Pilot;
import org.deltava.beans.cooler.*;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Water Cooler Threads.
 * @author Luke
 * @version 7.3
 * @since 1.0
 */

public final class CoolerThreadAccessControl extends AccessControl {

	private MessageThread _mt;
	private Channel _c;
	private CoolerChannelAccessControl _cac;

	private boolean _canRead;
	private boolean _canReply;
	private boolean _canEdit;
	private boolean _canEditTitle;
	private boolean _canAddImage;
	private boolean _canUnlinkImage;
	private boolean _canRelinkImage;
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
	 * Update the MessageThread to verify access to. Since this Access controller may be called many times in succession, it is more
	 * efficient to update the thread and channel rather than constantly creating a new CoolerThreadAccessControl object.
	 * @param t the Water Cooler message thread
	 * @param c the Water Cooler Channel
	 * @see CoolerThreadAccessControl#updateContext(SecurityContext)
	 */
	public void updateContext(MessageThread t, Channel c) {
		_mt = t;
		_c = c;
		_cac = new CoolerChannelAccessControl(_ctx, _c);
	}

	/**
	 * Updates the Security Context verify access with. Since this Access controller may be called many times in succession, it is more
	 * efficient to update the security context rather than constantly creating a new CoolerThreadAccessControl object.
	 * @param ctx the Command context
	 * @see CoolerThreadAccessControl#updateContext(MessageThread, Channel)
	 */
	public void updateContext(SecurityContext ctx) {
		_ctx = ctx;
		_cac = new CoolerChannelAccessControl(ctx, _c);
	}

	/**
	 * Calculates access rights.
	 */
	@Override
	public void validate() {
		validateContext();

		// Validate that the thread has been set
		if (_mt == null)
			throw new IllegalStateException("Mesage Thread not set");

		// Validate our channel access
		_cac.validate();
		boolean channelAccess = _cac.getCanAccess();
		boolean channelClosed = (_c != null) && !_c.getAllowNewPosts() && !_ctx.isUserInRole("Admin");

		// Get the user
		Pilot usr = (Pilot) _ctx.getUser();
		boolean isLockedOut = ((usr != null) && usr.getNoCooler());

		// Get the roles and role state - we assume it's OK if channel is null
		boolean isOurs = _ctx.isAuthenticated() && (_mt.getAuthorID() == _ctx.getUser().getID());
		boolean isModerator = _ctx.isUserInRole("Moderator");
		boolean isClosed = _mt.getLocked() || _mt.getHidden() || isLockedOut || channelClosed;
		boolean hasVoted = (_ctx.getUser() != null) && _mt.hasVoted(_ctx.getUser().getID());

		// Validate if we can read the thread
		_canRead = _ctx.isUserInRole("Admin") || (channelAccess && (!_mt.getHidden() || isModerator));
		_canReply = _ctx.isAuthenticated() && _canRead && (isModerator || !isClosed);
		_canVote = _canReply && !_mt.getOptions().isEmpty() && !hasVoted;
		_canLock = channelAccess && !isClosed && isModerator;
		_canUnlock = channelAccess && isClosed && isModerator;
		_canUnstick = channelAccess && (_mt.getStickyUntil() != null) && ((!isClosed && isOurs) || isModerator);
		_canDelete = _ctx.isUserInRole("Admin");
		_canReport = _canReply && (!isClosed) && (!_mt.getReportIDs().contains(Integer.valueOf(_ctx.getUser().getID())));
		_canAddImage = (isOurs && _canReply) || (isModerator && _canRead);
		_canUnlinkImage = _canAddImage && !_mt.getImageURLs().isEmpty();
		_canRelinkImage = _ctx.isUserInRole("Moderator") && _mt.getHasDisabledLinks();

		// Check if we can edit the title
		_canEditTitle = _canRead && (_ctx.isUserInRole("Moderator") || (isOurs && _mt.getPostCount() == 1));

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
	 * Returns whether the thread can be reported for content.
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
	 * Returns if the thread title can be edited.
	 * @return TRUE if the title can be edited, otherwise FALSE
	 */
	public boolean getCanEditTitle() {
		return _canEditTitle;
	}

	/**
	 * Returns if a user can add a Linked Image to the thread.
	 * @return TRUE if an Image can be added, otherwise FALSE
	 */
	public boolean getCanAddImage() {
		return _canAddImage;
	}

	/**
	 * Returns if a user can remove a Linked Image from the thread.
	 * @return TRUE if an Image can be unlinked, otherwise FALSE
	 */
	public boolean getCanUnlinkImage() {
		return _canUnlinkImage;
	}
	
	/**
	 * Returns if a user can restore disable Linked Images in this thread.
	 * @return TRUE if disabled linked images exists and can be enabled, otherwise FALSE
	 */
	public boolean getCanRelinkImages() {
		return _canRelinkImage;
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