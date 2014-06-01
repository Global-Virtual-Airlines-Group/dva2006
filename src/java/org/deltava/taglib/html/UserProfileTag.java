// Copyright 2005, 2012, 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

import org.deltava.beans.UserData;
import org.deltava.util.system.SystemData;

/**
 * A JSP Tag to display a User profile across applications.
 * @author Luke
 * @version 5.4
 * @since 1.0
 */

public class UserProfileTag extends ElementTag {

	private UserData _usrData;
	private final String _hostName;

	public UserProfileTag() {
		super("a");
		_hostName = SystemData.get("airline.url").replace(SystemData.get("airline.domain"), "");
	}

	/**
	 * Sets the User Location for this user.
	 * @param ud the UserData bean
	 */
	public void setLocation(UserData ud) {
		_usrData = ud;
		if (ud == null)
			return;

		// Determine the URL
		boolean isApplicant = "APPLICANTS".equals(ud.getTable());
		StringBuilder urlBuf = new StringBuilder("http://");
		urlBuf.append(_hostName);
		urlBuf.append(ud.getDomain());
		urlBuf.append('/');
		urlBuf.append(isApplicant ? "applicant.do?id=" : "profile.do?id=");
		urlBuf.append(ud.getHexID());
		_data.setAttribute("href", urlBuf.toString());
	}

	/**
	 * Opens this link element by writing an &gt;A&lt; tag.
	 * @throws JspException if an error occurs;
	 */
	@Override
	public int doStartTag() throws JspException {
		super.doStartTag();
		if (_usrData != null) {
			try {
				_out.print(_data.open(true));
			} catch (Exception e) {
				throw new JspException(e);
			}
		}

		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Closes this link element by writing an &gt;/A&lt; tag.
	 * @throws JspException if an I/O error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			if (_usrData != null)
				_out.print(_data.close());
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		return EVAL_PAGE;
	}
}