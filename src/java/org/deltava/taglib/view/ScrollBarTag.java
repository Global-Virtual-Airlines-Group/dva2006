// Copyright 2005, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.view;

import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.commands.ViewContext;

/**
 * A JSP tag to selectively display view table scroll tags.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class ScrollBarTag extends TagSupport {

	private ViewContext _vctx;
	private boolean _forceDisplay;

	/**
	 * Sets whether the tag body should be always included.
	 * @param doForce TRUE if the body should be always rendered, otherwise FALSE
	 */
	public void setForce(boolean doForce) {
		_forceDisplay = doForce;
	}

	/**
	 * Returns whether we are at the start of the view.
	 * @return TRUE if at the start of a view, otherwise FALSE
	 * @see ScrollBarTag#hasView()
	 */
	boolean isViewStart() {
		return ((_vctx == null) || (_vctx.getStart() == 0));
	}

	/**
	 * Returns whether we are at the end of the view.
	 * @return TRUE if at the end of a view, otherwise FALSE
	 * @see ScrollBarTag#hasView()
	 */
	boolean isViewEnd() {
		return ((_vctx == null) || _vctx.isEndOfView());
	}

	/**
	 * Returns whether a view context is present in the requeust.
	 * @return TRUE if a view context is present, otherwise FALSE
	 */
	boolean hasView() {
		return (_vctx != null);
	}
	
	/**
	 * Returns whether display of the scroll bar has been forced.
	 * @return TRUE if the body should always be rendered
	 */
	boolean isForced() {
		return _forceDisplay;
	}

	/**
	 * Returns the view context.
	 * @return the view context
	 */
	ViewContext getContext() {
		return _vctx;
	}

	/**
	 * Loads the view context from the page context, and determines whether to include the tag body. The tag body will
	 * only be included if the view context is present and we are not simaltaneously at the start and end of the view.
	 * @return TagSupport.EVAL_BODY_INCLUDE or TagSupport.SKIP_BODY
	 */
	public int doStartTag() {

		// Get the view context
		_vctx = (ViewContext) pageContext.findAttribute(ViewContext.VIEW_CONTEXT);

		// Check if we force display, otherwise display only if at start or end, or neither
		int result = EVAL_BODY_INCLUDE;
		if (_forceDisplay)
			result = EVAL_BODY_INCLUDE;
		else if (_vctx == null)
			result = SKIP_BODY;
		else if (isViewStart() && isViewEnd())
			result = SKIP_BODY;

		release();
		return result;
	}

	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		super.release();
		_forceDisplay = false;
	}
}