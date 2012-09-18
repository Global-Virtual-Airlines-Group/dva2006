// Copyright 2005, 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.util.Date;
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
 * @version 5.0
 * @since 1.0
 */

public class DateFormatTag extends TagSupport {

	private enum Format {
		DT, D, T
	}

	private Format _dtInclude = Format.DT;
	private String _dateFormat = SystemData.get("time.date_format");
	private String _timeFormat = SystemData.get("time.time_format");
	private TZInfo _tz = TZInfo.get(SystemData.get("time.timezone"));
	private DateTime _dt;

	private String _className;
	private String _nullData;

	/**
	 * Updates this tag's page context and loads the user object from the request.
	 * @param ctxt the new JSP page context
	 */
	@Override
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
	 * Updates the CSS class for this formatted date/time. This will automatically enclose the output in a &lt;SPAN&gt; tag.
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
		try {
			_dtInclude = Format.valueOf(dtFmt.toUpperCase());
		} catch (Exception e) {
			_dtInclude = Format.DT;
		}
	}

	/**
	 * Sets the date/time to display.
	 * @param d the date/time
	 */
	public void setDate(Date d) {
		if (d != null)
			_dt = new DateTime(d);
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
		_tz = TZInfo.get(tzName);
	}

	/**
	 * Overrides whether the time zone should be displayed.
	 * @param showZone TRUE if the time zone should be displayed, otherwise FALSE
	 * @see DateTime#showZone(boolean)
	 */
	public void setShowZone(boolean showZone) {
		_dt.showZone(showZone);
	}

	/**
	 * Releases this tag's state variables.
	 */
	@Override
	public void release() {
		super.release();
		_dtInclude = Format.DT;
		_dateFormat = SystemData.get("time.date_format");
		_timeFormat = SystemData.get("time.time_format");
		_tz = TZInfo.get(SystemData.get("time.timezone"));
		_dt = null;
		_className = null;
	}

	/**
	 * Mashes the date and time together in a date/time object.
	 * @return TagSupport.SKIP_BODY
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doStartTag() throws JspException {
		if (_dt != null)
			_dt.convertTo(_tz);

		return SKIP_BODY;
	}

	/**
	 * Formats the date/time and writes it to the JSP output writer.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {

		// Build the format string
		if (_dt != null) {
			StringBuilder fmtPattern = new StringBuilder();
			switch (_dtInclude) {
			case D:
				fmtPattern.append(_dateFormat);
				if (_dt != null)
					_dt.showZone(false);
				break;

			case T:
				fmtPattern.append(_timeFormat);
				break;

			default:
				fmtPattern.append(_dateFormat);
				fmtPattern.append(' ');
				fmtPattern.append(_timeFormat);
			}

			_dt.setDateFormat(fmtPattern.toString());
		}

		// Write the datetime value
		try {
			JspWriter out = pageContext.getOut();
			if (_className != null) {
				out.print("<span class=\"");
				out.print(_className);
				out.print("\">");
			}

			// Write the formatted date
			out.print((_dt != null) ? _dt.toString() : _nullData);

			if (_className != null)
				out.print("</span>");
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		return EVAL_PAGE;
	}
}