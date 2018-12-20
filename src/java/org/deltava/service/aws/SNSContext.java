// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.aws;

import org.deltava.service.ServiceContext;

/**
 * An execution context for SNS 
 * @author Luke
 * @version 8.5
 * @since 8.5
 */

class SNSContext extends ServiceContext {

	private final SNSPayload _sns;
	
	SNSContext(ServiceContext ctx, SNSPayload sns) {
		super(ctx.getRequest(), ctx.getResponse());
		_sns = sns;
	}
	
	public SNSPayload getPayload() {
		return _sns;
	}
}