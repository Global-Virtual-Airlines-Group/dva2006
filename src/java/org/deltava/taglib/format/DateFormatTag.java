// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.format;

import java.util.Date;
import java.io.IOException;
import java.text.SimpleDateFormat;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.beans.*;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to support the display of formatted date/time values.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class DateFormatTag extends TagSupport {
   
   private static final TZInfo _defaultTZ = TZInfo.init(SystemData.get("time.timezone"));

	private static final int DATE_TIME = 0;
	private static final int DATE_ONLY = 1;
	private static final int TIME_ONLY = 2;
	private static final String[] FORMAT = { "dt", "d", "t" };

	private int _dtInclude = DateFormatTag.DATE_TIME;
	private String _dateFormat = SystemData.get("time.date_format");
	private String _timeFormat = SystemData.get("time.time_format");
	private TZInfo _tz = _defaultTZ;
	private DateTime _dt;

	private String _className;
	private String _nullData;

	/**
	 * Updates this tag's page context and loads the user object from the request.
	 * @param ctxt the new JSP page context
	 */
	public final void setPageContext(PageContext ctxt) {
		super.setPageContext(ctxt);
		HttpServletRequest req = (HttpServletRequest) ctxt.getRequest();
		Principal user = req.getUserPrincipal();
		if (user instanceof Person) {
			Person p = (Person) user;
			_dateFormat = p.getDateFormat();
			_timeFormat = p.getTimeFormat();
			_tz = p.getTZ();
		}
	}

	/**
	 * Updates the CSS class for this formatted date/time. This will automatically enclose the output in a
	 * &lt;SPAN&gt; tag.
	 * @param cName the class Name(s)
	 */
	public void setClassName(String cName) {
		_className = cName;
	}

	/**
	 * Updates the date format pattern.
	 * @param pattern the pattern string
	 * @throws IllegalArgumentException if SimpleDateFormat cannot interpret the pattern
	 * @see SimpleDateFormat#applyPattern(String)
	 */
	public void setD(String pattern) {
		SimpleDateFormat df = new SimpleDateFormat(pattern); // validate pattern
		_dateFormat = df.toPattern();
	}

	/**
	 * Updates the text to display if the date is null.
	 * @param defaultText the text to display
	 */
	public void setDefault(String defaultText) {
		_nullData = defaultText;
	}

	/**
	 * Updates the time format pattern.
	 * @param pattern the pattern string
	 * @throws IllegalArgumentException if SimpleDateFormat cannot interpret the pattern
	 * @see SimpleDateFormat#applyPattern(String)
	 */
	public void setT(String pattern) {
		SimpleDateFormat df = new SimpleDateFormat(pattern); // validate pattern
		_timeFormat = df.toPattern();
	}

	/**
	 * Sets what components of the date/time to display.
	 * @param dtFmt &quot;dt&quot; to show date/time, &quot;d&quot; for date only, &quot;t&quot; for time only
	 */
	public void setFmt(String dtFmt) {
		for (int x = 0; x < DateFormatTag.FORMAT.length; x++) {
			if (DateFormatTag.FORMAT[x].equalsIgnoreCase(dtFmt)) {
				_dtInclude = x;
				return;
			}
		}
	}

	/**
	 * Sets the date/time to display.
	 * @param d the date/time
	 * @throws NullPointerException if d is null
	 */
	public void setDate(Date d) {
		if (d != null) {
			_dt = new DateTime(d);
			_dt.convertTo(_tz);
		}
	}

	/**
	 * Overrides the time zone used to display the date/time with.
	 * @param tz the time zone
	 */
	public void setTz(TZInfo tz) {
		_tz = tz;
	}
	
	/**
	 * Overrides the time zone used to display the date/time with.
	 * @param tzName the time zone name
	 */
	public void setTzName(String tzName) {
		_tz = TZInfo.init(tzName);
	}

	/**
	 * Overrides wether the time zone should be displayed.
	 * @param showZone TRUE if the time zone should be displayed, otherwise FALSE
	 * @see DateTime#showZone(boolean)
	 */
	public void setShowZone(boolean showZone) {
		_dt.showZone(showZone);
	}

	/**
	 * Releases this tag's state variables.
	 */
	public void release() {
		super.release();
		_dtInclude = DateFormatTag.DATE_TIME;
		_dateFormat = SystemData.get("time.date_format");
		_timeFormat = SystemData.get("time.time_format");
		_tz = _defaultTZ;
		_dt = null;
		_className = null;
	}

	/**
	 * Formats the date/time and writes it to the JSP output writer.
	 * @throws JspException if an error occurs
	 */
	public int doEndTag() throws JspException {

		// Build the format string
		String fmtPattern;
		switch (_dtInclude) {
			case DATE_ONLY:
				fmtPattern = _dateFormat;
				if (_dt != null)
					_dt.showZone(false);
				break;

			case TIME_ONLY:
				fmtPattern = _timeFormat;
				break;

			default:
				fmtPattern = _dateFormat + " " + _timeFormat;
		}

		// Update the dateTime value formatter
		if (_dt != null)
			_dt.setDateFormat(fmtPattern);

		// Write the datetime value
		JspWriter out = pageContext.getOut();
		try {
			if (_className != null) {
				out.print("<span class=\"");
				out.print(_className);
				out.print("\">");
			}

			// Write the formatted date
			out.print((_dt != null) ? _dt.toString() : _nullData);

			if (_className != null)
				out.print("</span>");
		} catch (IOException ie) {
			JspException je = new JspException(ie.getMessage());
			je.initCause(ie);
			throw je;
		}

		release();
		return EVAL_PAGE;
	}
}