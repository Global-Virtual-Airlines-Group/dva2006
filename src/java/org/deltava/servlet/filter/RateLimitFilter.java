// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.filter;

import static org.deltava.commands.HTTPContext.*;

import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.sql.Connection;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.system.*;
import org.deltava.beans.system.RateLimiter.Result;

import org.deltava.dao.GetIPLocation;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import org.gvagroup.pool.ConnectionPool;

/**
 * A servlet filter to do HTTP rate limiting.
 * @author Luke
 * @version 11.6
 * @since 11.6
 */

public class RateLimitFilter extends HttpFilter implements Thread.UncaughtExceptionHandler {

	private static final Logger log = LogManager.getLogger(RateLimitFilter.class);
	
	private RateLimiter _rl;
	private RequestPersister _rp;
	private String _jedisKey;
	
	private class RequestPersister implements Runnable {
		private final int _interval;
		private boolean _isRunning;
		private long _lastExecTime = System.currentTimeMillis();
		
		RequestPersister(int interval) {
			super();
			_interval = Math.max(10, interval) * 1000;
		}
		
		public boolean canRun() {
			return !_isRunning && (System.currentTimeMillis() > (_lastExecTime + _interval));
		}
		
		@Override
		public void run() {
			try {
				_isRunning = true;
				log.info("{} Persisting limit counters", SystemData.get("airline.code"));
				_lastExecTime = System.currentTimeMillis();
				_rl.purge();
				
				// Clone the counters
				List<RequestCounter> ctrs = _rl.getCounters().stream().map(RequestCounter::new).collect(Collectors.toList());

				// Lookup net blocks as needed
				ConnectionPool<Connection> cp = SystemData.getJDBCPool();
				try (Connection c = cp.getConnection()) {
					GetIPLocation ipdao = new GetIPLocation(c);
					for (RequestCounter rc : ctrs) {
						if (rc.getIPInfo() != null) continue;
						IPBlock ip = ipdao.get(rc.getAddress());
						rc.setIPInfo(ip);
					}
				} catch (Exception de) {
					log.atError().withThrowable(de).log("Error looking up netblocks - {}", de.getMessage());
				}
				
				// Merge the counters based on netblock
				Collection<RequestCounter> mctrs = _rl.merge();
				JedisUtils.write(_jedisKey, 1800, mctrs);
				if (mctrs.size() < ctrs.size())
					log.info("{} merged {} counters into {}", SystemData.get("airline.code"), Integer.valueOf(ctrs.size()), Integer.valueOf(mctrs.size()));
			} finally {
				_isRunning = false;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(FilterConfig cfg) throws ServletException {
		_jedisKey = String.format("RL-%s", SystemData.get("airline.code"));
		_rl = new RateLimiter(true, StringUtils.parse(cfg.getInitParameter("minRequests"), 10), StringUtils.parse(cfg.getInitParameter("minTime"), 300));
		_rl.setBlocking(StringUtils.parse(cfg.getInitParameter("blockCount"), Integer.MAX_VALUE), StringUtils.parse(cfg.getInitParameter("blockTime"), 600));
		
		// Load cached data
		Collection<RequestCounter> data = (Collection<RequestCounter>) JedisUtils.get(_jedisKey);
		if (data != null) {
			_rl.load(data);
			if (!data.isEmpty())
				log.info("Loaded {} rate limit counters", Integer.valueOf(data.size()));
		}
		
		log.info("Started - blocking after {} requests in {}s", Integer.valueOf(_rl.getMinRequests()), Long.valueOf(_rl.getMinTime().getSeconds()));
		cfg.getServletContext().setAttribute(RTLIMIT_ATTR_NAME, _rl);
		
		// Start persistence thread
		_rp = new RequestPersister(90);
	}
	
	@Override
    public void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain fc) throws IOException, ServletException {
		
		// Only validate anonymous users and those who failed CAPTCHA
		boolean isAnonymous = (req.getUserPrincipal() == null);
		HttpSession s = req.getSession(false);
		CAPTCHAResult cr = (s == null) ? null : (CAPTCHAResult) s.getAttribute(CAPTCHA_ATTR_NAME);
		boolean isOK = (cr != null) && cr.getIsSuccess();
		
		if (isAnonymous && !isOK) {
			RateLimiter.Result r = _rl.addAddress(req.getRemoteAddr());
			if (r != Result.PASS) {
				RequestCounter rc = _rl.get(req.getRemoteAddr()); int reqs = rc.getRequests();
				HTTPContextData ctx = (HTTPContextData) req.getAttribute(HTTPCTXT_ATTR_NAME);
				ctx.forceSpider();
				
				// Block if applicable
				if (r == Result.BLOCK) {
					log.error("Blocking {} ({}) after {} requests in {}s", req.getRemoteAddr(), req.getRemoteHost(), Integer.valueOf(reqs), Long.valueOf(_rl.getMinTime().getSeconds()));
					rsp.setStatus(429);
					return;
				}
				
				log.log((reqs == _rl.getMinRequests()) ? Level.WARN : Level.INFO, "Flagging {} ({} requests)", req.getRemoteAddr(), Integer.valueOf(reqs));
			}
		
			// Purge the totals
			if (_rp.canRun())
				Thread.ofVirtual().name("Rate Limit Persister").uncaughtExceptionHandler(this).start(_rp);
		} else if (!isAnonymous)
			_rl.remove(req.getRemoteAddr());
		
		fc.doFilter(req, rsp);
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log.atError().withThrowable(e).log("Error persisting limits - {}", e.getMessage());
	}
}