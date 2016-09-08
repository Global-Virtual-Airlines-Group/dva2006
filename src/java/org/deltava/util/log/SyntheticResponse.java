// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.log;

import com.newrelic.api.agent.*;

/**
 * A Response class for NewRelic non-web transactions.
 * @author Luke
 * @version 7.2
 * @since 7.2
 */

public class SyntheticResponse implements Response {

	@Override
	public HeaderType getHeaderType() {
		return HeaderType.HTTP;
	}

	@Override
	public void setHeader(String arg0, String arg1) {
		// empty
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public int getStatus() throws Exception {
		return 200;
	}

	@Override
	public String getStatusMessage() throws Exception {
		return null;
	}
}