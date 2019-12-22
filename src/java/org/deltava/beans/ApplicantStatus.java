// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration of Applicant statuses.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public enum ApplicantStatus implements ViewEntry, ComboAlias {
	PENDING("opt1"), APPROVED(null), REJECTED("err");
	
	private final String _viewCSS;
	
	ApplicantStatus(String viewCSS) {
		_viewCSS = viewCSS;
	}
	
	/**
	 * Returns the status description.
	 * @return the description
	 */
	public String getDescription() {
		return name().substring(0, 1) + name().substring(1).toLowerCase();
	}

	@Override
	public String getRowClassName() {
		return _viewCSS;
	}
	
	/**
	 * Exception-safe value parser.
	 * @param name the value
	 * @param defaultValue the default value if the value cannot be parsed
	 * @return an ApplicantStatus
	 */
	public static ApplicantStatus fromName(String name, ApplicantStatus defaultValue) {
		try {
			return ApplicantStatus.valueOf(name.toUpperCase());
		} catch (Exception e) {
			return defaultValue;
		}
	}

	@Override
	public String getComboAlias() {
		return name();
	}

	@Override
	public String getComboName() {
		return getDescription();
	}
}