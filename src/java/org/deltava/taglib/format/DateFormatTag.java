// Copyright 2005, 2010, 2012, 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.util.Date;
import java.text.SimpleDateFormat;

import javax.servlet.jsp.*;

import org.deltava.beans.*;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to support the display of formatted date/time values.
 * @author Luke
 * @version 5.2
 * @since 1.0
 */

public class DateFormatTag extends UserSettingsTag {
	
	private final String DEFAULT_D_FMT = SystemData.get("time.date_format");
	private final String DEFAULT_T_FMT = SystemData.get("time.time_format");
	private final TZInfo DEFAULT_TZ = TZInfo.get(SystemData.get("time.timezone"));

	private enum Format {
		DT, D, T
	}

	private Format _dtInclude = Format.DT;
	private String _dateFormat = DEFAULT_D_FMT;
	private String _timeFormat = DEFAULT_T_FMT;
	private TZInfo _tz = DEFAULT_TZ;
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
		if (_user != null) {
			_dateFormat = _user.getDateFormat();
			_timeFormat = _user.getTimeFormat();
			_tz = _user.getTZ();
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
		_dateFormat = DEFAULT_D_FMT;
		_timeFormat = DEFAULT_T_FMT;
		_tz = DEFAULT_TZ;
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