// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.filter;

import static org.deltava.commands.HTTPContext.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.system.*;

import org.deltava.util.StringUtils;

/**
 * A servlet filter to do spider detection.
 * @author Luke
 * @version 11.6
 * @since 11.6
 */

public class SpiderFilter extends HttpFilter {

	private static final Logger log = LogManager.getLogger(SpiderFilter.class);
	
	private SpiderHunter _spdr;
	private int _purgeReqCount;
	private final AtomicInteger _reqs = new AtomicInteger();
	
	@Override
	public void init(FilterConfig cfg) throws ServletException {
		_spdr = new SpiderHunter(StringUtils.parse(cfg.getInitParameter("minRequests"), 10), StringUtils.parse(cfg.getInitParameter("minTime"), 300));
		cfg.getServletContext().setAttribute("spiderHunter", _spdr);
		log.info("Started - {} requests in {}s", Integer.valueOf(_spdr.getRequests()), Long.valueOf(_spdr.getMinTime().getSeconds()));
		_purgeReqCount = StringUtils.parse(cfg.getInitParameter("purgeCount"), 2000);
	}
	
	@Override
    public void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain fc) throws IOException, ServletException {
		
		// Only validate anonymous users and those who failed CAPTCHA
		boolean isAnonymous = (req.getUserPrincipal() == null);
		HttpSession s = req.getSession(false);
		CAPTCHAResult cr = (s == null) ? null : (CAPTCHAResult) s.getAttribute(CAPTCHA_ATTR_NAME);
		boolean isOK = (cr != null) && cr.getIsSuccess();
		
		if (isAnonymous && !isOK) {
			int totalReqs = _reqs.incrementAndGet();
			boolean isSpider = _spdr.addAddress(req.getRemoteAddr());
			if (isSpider) {
				RequestCounter rc = _spdr.get(req.getRemoteAddr());
				log.log((rc.getRequests() == _spdr.getRequests()) ? Level.WARN : Level.INFO, "Spider detected - {} ({} requests)", req.getRemoteAddr(), Integer.valueOf(rc.getRequests()));
				HTTPContextData ctx = (HTTPContextData) req.getAttribute(HTTPCTXT_ATTR_NAME);
				ctx.forceSpider();
			}
		
			// Purge the totals
			if (totalReqs > _purgeReqCount) {
				log.debug("Purging Spider Hunter after {} requests", Integer.valueOf(totalReqs));
				_spdr.purge();
				_reqs.set(0);
			}
		} else if (!isAnonymous)
			_spdr.remove(req.getRemoteAddr());
		
		fc.doFilter(req, rsp);
	}
}