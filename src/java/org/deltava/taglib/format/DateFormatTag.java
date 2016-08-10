// Copyright 2005, 2010, 2012, 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;

import javax.servlet.jsp.*;

import org.deltava.beans.TZInfo;
import org.deltava.util.system.SystemData;

/**
 * A JSP tag to support the display of formatted date/time values.
 * @author Luke
 * @version 7.1
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
	private Instant _dt;
	private boolean _showZone = true;

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
	 * @throws IllegalArgumentException if {@link DateTimeFormatter} cannot interpret the pattern
	 */
	public void setD(String pattern) {
		DateTimeFormatter df = DateTimeFormatter.ofPattern(pattern); // validate pattern
		if (df != null)
			_dateFormat = pattern;
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
	 * @throws IllegalArgumentException if {@link DateTimeFormatter} cannot interpret the pattern
	 */
	public void setT(String pattern) {
		DateTimeFormatter df = DateTimeFormatter.ofPattern(pattern); // validate pattern
		if (df != null)
			_timeFormat = pattern;
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
	 * @param i the date/time
	 */
	public void setDate(Temporal i) {
		if (i instanceof Instant)
			_dt = (Instant) i;
		else if (i instanceof ZonedDateTime)
			_dt = ((ZonedDateTime) i).toInstant();
		else if (i instanceof LocalDate) {
			_dt = Instant.ofEpochMilli(((LocalDate) i).toEpochDay() * ChronoUnit.DAYS.getDuration().getSeconds());
		} else if (i instanceof LocalDateTime)
			_dt = ((LocalDateTime) i).toInstant(ZoneOffset.UTC);
		else if (i != null)
			throw new IllegalArgumentException("Invalid temporal type - " + i.getClass().getSimpleName());
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
	 */
	public void setShowZone(boolean showZone) {
		_showZone = showZone;
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
		_showZone = true;
	}

	/**
	 * Formats the date/time and writes it to the JSP output writer.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {

		// Build the format string
		StringBuilder fmtPattern = new StringBuilder();
		switch (_dtInclude) {
		case D:
			fmtPattern.append(_dateFormat);
			_showZone = false;
			break;

		case T:
			fmtPattern.append(_timeFormat);
			break;

		default:
			fmtPattern.append(_dateFormat).append(' ').append(_timeFormat);
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
			if (_dt != null) {
				if (_tz == null) _tz = DEFAULT_TZ;
				DateTimeFormatter df = DateTimeFormatter.ofPattern(fmtPattern.toString());
				ZonedDateTime zdt = ZonedDateTime.ofInstant(_dt, _tz.getZone());
				out.print(df.format(zdt));
				if (_showZone) {
					out.print(' ');
					out.print(_tz.getAbbr());
				}
			} else
				out.print(_nullData);
			
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