// Copyright 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration of Applicant statuses.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public enum ApplicantStatus implements ViewEntry, EnumDescription {
	PENDING("opt1"), APPROVED(null), REJECTED("err");
	
	private final String _viewCSS;
	
	ApplicantStatus(String viewCSS) {
		_viewCSS = viewCSS;
	}
	
	@Override
	public String getRowClassName() {
		return _viewCSS;
	}
}