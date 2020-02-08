// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.*;

import javax.servlet.http.HttpSession;

import org.deltava.commands.HTTPContext;
import org.deltava.dao.http.GetGoogleCAPTCHA;

/**
 * A Web Service to validate Google RECAPTCHA tokens.
 * @author Luke
 * @version 9.0
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

		// Validate the token
		try (BufferedReader sr = new BufferedReader(new InputStreamReader(ctx.getRequest().getInputStream()))) {
			GetGoogleCAPTCHA cdao = new GetGoogleCAPTCHA();
			cdao.setConnectTimeout(2500);
			cdao.setReadTimeout(3500);
			String token = sr.readLine();
			boolean isOK = cdao.validate(token, ctx.getRequest().getRemoteAddr());
			HttpSession s = ctx.getRequest().getSession();
			s.setAttribute(HTTPContext.CAPTCHA_ATTR_NAME, Boolean.valueOf(isOK));	
		} catch (Exception e) {
			throw error(SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
		
		return SC_OK;
	}
}