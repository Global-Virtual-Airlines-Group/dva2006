// Copyright 2020, 2021, 2022, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;
import static org.deltava.commands.HTTPContext.CAPTCHA_ATTR_NAME;

import java.io.*;

import javax.servlet.http.HttpSession;

import org.deltava.beans.system.*;
import org.deltava.dao.DAOException;
import org.deltava.dao.http.*;

import org.deltava.util.StringUtils;

/**
 * A Web Service to validate Google RECAPTCHA tokens.
 * @author Luke
 * @version 11.6
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
			CAPTCHAResult cr = (CAPTCHAResult) s.getAttribute(CAPTCHA_ATTR_NAME); 
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
			cdao.setCompression(Compression.GZIP, Compression.BROTLI);
			String rsp = sr.readLine(); 
			if (!StringUtils.isEmpty(rsp)) {
				CAPTCHAResult cr = cdao.validate(rsp, ctx.getRequest().getRemoteAddr());
				s = ctx.getRequest().getSession(true);
				s.setAttribute(CAPTCHA_ATTR_NAME, cr);
			}
		} catch (IOException ie) {
			throw error(SC_INTERNAL_SERVER_ERROR, ie.getMessage(), false);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), true);
		} catch (Exception e) {
			throw error(SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
		}
		
		return SC_OK;
	}
}