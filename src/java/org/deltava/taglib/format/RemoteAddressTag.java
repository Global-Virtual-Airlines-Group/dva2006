// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.*;

import org.deltava.beans.RemoteAddressBean;
import org.deltava.beans.system.IPBlock;

import org.deltava.taglib.html.ImageTag;

/**
 * A JSP tag to format IP address information.
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class RemoteAddressTag extends ImageTag {
	
	private RemoteAddressBean _addr;
	private IPBlock _addrInfo;
	private boolean _showFlag;
	
	/**
	 * Creates the tag.
	 */
	public RemoteAddressTag() {
		super();
		setX(16);
		setY(11);
	}
	
	/**
	 * Sets the remote address to display.
	 * @param addr a RemoteAddressBean
	 */
	public void setAddr(RemoteAddressBean addr) {
		_addr = addr;
	}
	
	/**
	 * Sets information about the IP address block.
	 * @param ib the IPBlock
	 */
	public void setInfo(IPBlock ib) {
		_addrInfo = ib;
	}
	
	/**
	 * Sets whether to show the national flag of the IP block.
	 * @param showFlag TRUE to display the flag, otherwise FALSE
	 */
	public void setShowFlag(boolean showFlag) {
		_showFlag = showFlag;
	}
	
	@Override
	public void release() {
		super.release();
		setX(16);
		setY(11);
		_addrInfo = null;
		_showFlag = false;
	}
	
	/**
	 * Calculates the flag data, if requested.
	 * @return SKIP_BODY always
	 */
	@Override
	public int doStartTag() {
		_showFlag &= (_addrInfo != null);
		if (_showFlag && (_addrInfo != null)) {
			StringBuilder buf = new StringBuilder("flags/");
			buf.append(_addrInfo.getCountry().getCode().toLowerCase());
			buf.append(".png");
			setSrc(buf.toString());
		}
		
		return SKIP_BODY;
	}
	
	/**
	 * Displays the address information.
	 * @return EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
	
		boolean isResolved = _addr.getIsResolved();
		boolean showInfo = (isResolved || (_addrInfo != null));
		try {
			JspWriter out = pageContext.getOut();
			out.print(_addr.getRemoteAddr());
			if (showInfo)
				out.print(" (");
			if (isResolved) {
				out.print(_addr.getRemoteHost());
				if (_addrInfo != null)
					out.print(' ');
			}
			
			if (_addrInfo != null) {
				if (_showFlag) {
					_data.setAttribute("title", _addrInfo.getCountry().getName());
					renderHTML();
					out.print(' ');
				}
				
				out.print(_addrInfo.getLocation());
			}
			
			if (showInfo)
				out.print(')');
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}