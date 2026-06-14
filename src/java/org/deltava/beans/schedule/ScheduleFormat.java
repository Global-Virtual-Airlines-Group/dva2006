// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * An enumeration of schedule formats. 
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public enum ScheduleFormat {
	CSV("text/csv", "csv", CSVScheduleFormatter.class), JSON("application/json", "json", JSONScheduleFormatter.class);
	
	private final String _mimeType;
	private final String _ext;
	private final Class<? extends ScheduleFormatter> _c;
	
	ScheduleFormat(String mimeType, String ext, Class<? extends ScheduleFormatter> c) {
		_mimeType = mimeType;
		_ext = ext;
		_c = c;
	}
	
	/**
	 * Returns this format's content type.
	 * @return the content type
	 */
	public String getContentType() {
		return _mimeType;
	}
	
	/**
	 * Returns the format's file extension.
	 * @return the extension
	 */
	public String getExtension() {
		return _ext;
	}
	
	/**
	 * Returns a new instance of an appropriate ScheduleFormatter.
	 * @return a ScheduleFormatter
	 */
	public ScheduleFormatter getIntsance() {
		try {
			ScheduleFormatter sf = _c.getDeclaredConstructor((Class[]) null).newInstance((Object[]) null);
			return sf;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}