// Copyright 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.*;

import javax.servlet.http.HttpSession;

import org.deltava.beans.system.*;

import org.deltava.commands.HTTPContext;
import org.deltava.dao.http.GetGoogleCAPTCHA;

import org.deltava.util.StringUtils;

/**
 * A Web Service to validate Google RECAPTCHA tokens.
 * @author Luke
 * @version 10.0
 * @since 9.0
 */

public class ValidateCAPTCHAService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// If this isn't a post, just return a 400
		if (!ctx.getRequest().getMethod().equalsIgnoreCase("post"))
			return SC_BAD_REQUEST;
		
		// Check if we have something
		HttpSession s = ctx.getRequest().getSession(false);
		if (s != null) {
			CAPTCHAResult cr = (CAPTCHAResult) s.getAttribute(HTTPContext.CAPTCHA_ATTR_NAME); 
			if ((cr != null) && cr.getIsSuccess()) {
				ctx.getResponse().addDateHeader("Date", cr.getChallengeTime().toEpochMilli());
				return SC_NOT_MODIFIED;
			}
		}
		
		// Validate the token
		APILogger.add(new APIRequest(API.Google.createName("CAPTCHA"), !ctx.isAuthenticated()));
		try (BufferedReader sr = new BufferedReader(new InputStreamReader(ctx.getRequest().getInputStream()))) {
			GetGoogleCAPTCHA cdao = new GetGoogleCAPTCHA();
			cdao.setConnectTimeout(2500);
			cdao.setReadTimeout(3500);
			String rsp = sr.readLine(); 
			if (!StringUtils.isEmpty(rsp)) {
				CAPTCHAResult cr = cdao.validate(rsp, ctx.getRequest().getRemoteAddr());
				s = ctx.getRequest().getSession(true);
				s.setAttribute(HTTPContext.CAPTCHA_ATTR_NAME, cr);
			}
		} catch (Exception e) {
			throw error(SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
		}
		
		return SC_OK;
	}
}