// Copyright 2018, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.aws;

import static javax.servlet.http.HttpServletResponse.*;

import javax.servlet.http.HttpServletRequest;

import org.json.*;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;

import org.deltava.dao.*;
import org.deltava.dao.http.GetURL;

import org.deltava.crypt.SNSVerifier;

import org.deltava.service.*;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to receive Amazon SNS messages.
 * @author Luke
 * @version 9.1
 * @since 8.5
 */

abstract class SNSReceiverService extends WebService {
	
	private static final Logger log = Logger.getLogger(SNSReceiverService.class);
	
	private Pilot _usr;

	/**
	 * Validates an SNS message.
	 * @param sns an SNSPayload
	 * @throws ServiceException the payload fails validation
	 */
	protected static void validate(SNSPayload sns) throws ServiceException {
		
		// Validate basic fields
		if (StringUtils.isEmpty(sns.getID()))
			throw error(SC_BAD_REQUEST, "Invalid Message ID");
		if (StringUtils.isEmpty(sns.getTopic()))
			throw error(SC_BAD_REQUEST, "Invalid SNS Topic");
		if (StringUtils.isEmpty(sns.getType()))
			throw error(SC_BAD_REQUEST, "Invalid Message Type");
		if (sns.getSignatureVersion() != 1)
			throw error(SC_BAD_REQUEST, "Invalid Signature Version - " + sns.getSignatureVersion());

		// Validate the signature
		try {
			boolean isOK = SNSVerifier.validate(sns.getBody());
			if (!isOK)
				throw new SecurityException("Cannot validate SNS message");
		} catch (SecurityException se) {
			throw error(SC_BAD_REQUEST, se.getMessage());
		}
	}
	
	/**
	 * Confirms an SNS subscription.
	 * @param sns the SNS payload
	 * @return TRUE if successfully confirmed, otherwise FALSE
	 * @throws ServiceException if an error occurs
	 */
	protected static boolean confirm(SNSPayload sns) throws ServiceException {
		
		log.warn("Received subscription confirmation for " + sns.getTopic());
		String url = sns.getBody().getString("SubscribeURL");
		try {
			log.info("Fetching confirmation URL " + url);
			GetURL urldao = new GetURL(url, "");
			boolean isOK = urldao.isAvailable();
			log.warn("Subscription confirmation " + (isOK ? "SUCCEESS" : "FAILED"));
			return isOK;
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		}
	}
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return 200 Always
	 */
	@Override
	public final int execute(ServiceContext ctx) throws ServiceException {
		
		// Load and validate the message
		HttpServletRequest req = ctx.getRequest();
		SNSPayload sns = new SNSPayload(req.getHeader("x-amz-sns-message-id"), req.getHeader("x-amz-sns-topic-arn"), req.getHeader("x-amz-sns-message-type"));
		sns.setBody(new JSONObject(ctx.getBody()));
		validate(sns);
		
		// If it's a confirmation, accept it
		boolean isSubConfirm = "SubscriptionConfirmation".equalsIgnoreCase(sns.getBody().optString("Type"));
		if (isSubConfirm) {
			boolean isOK = confirm(sns);
			return isOK ? SC_OK : SC_INTERNAL_SERVER_ERROR;
		}
		
		// Load the User
		if (_usr == null) {
			try {
				GetPilotDirectory pdao = new GetPilotDirectory(ctx.getConnection());
				_usr = pdao.getByCode(SystemData.get("users.tasks_by"));
			} catch (DAOException de) {
				throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());				
			} finally {
				ctx.release();
			}
		}
		
		// Create the context and execute
		SNSContext sctx = new SNSContext(ctx, sns);
		sctx.setUser(_usr);
		return execute(sctx);
	}
	
	/**
	 * Processes the SNS payload.
	 * @param ctx the SNSContext
	 * @return an HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	protected abstract int execute(SNSContext ctx) throws ServiceException;
}