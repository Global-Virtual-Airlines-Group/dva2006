// Copyright 2025, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.util.stream.Collectors;
import java.time.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.econ.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.stats.*;
import org.deltava.beans.system.Prompt;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.http.GetAIYearlyReview;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display a Year in Review page. 
 * @author Luke
 * @version 13.0
 * @since 12.4
 */

public class YearlyReviewCommand extends AbstractTestHistoryCommand {
	
	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check for elevated options
		boolean isAdmin = ctx.isUserInRole("Operations") || ctx.isUserInRole("HR");
		
		// Get the user ID
		int userID = ctx.getUser().getID();
		int id = StringUtils.parse(ctx.getParameter("id"), 0);
		if (isAdmin && (id > 0))
			userID = id;
		
		// Get the year - don't allow current year until after 11/30
		LocalDate now = LocalDate.now();
		int currentYear = now.getYear();
		int year = StringUtils.parse(ctx.getParameter("year"), currentYear);
		if ((year > currentYear) || ((year == currentYear)) && (now.getMonth() != Month.DECEMBER) && (!isAdmin && !ctx.isUserInRole("Developer")))
			year = currentYear - 1;
		
		CommandResult result = ctx.getResult();
		try {
			Connection con = ctx.getConnection();
			
			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(userID);
			if (p == null)
				throw notFoundException("Invalid Pilot", userID);
			
			// Load tests and populate logbook
			TestingHistoryHelper th = initTestHistory(p, con);
			Collection<FlightReport> data = getLogbook(p);
			
			// Remove future flights - clone data before you do so since the cache may be local
			List<Integer> years = data.stream().mapToInt(fr -> LocalDate.ofInstant(fr.getDate(), ZoneOffset.UTC).getYear()).distinct().boxed().collect(Collectors.toList());
			Collections.sort(years, Comparator.reverseOrder());
			
			// Save pilot and years
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("years", years, REQUEST);
			ctx.setAttribute("year", Integer.valueOf(year), REQUEST);

			// Load current year and all previous years flights
			YearlyFilter yf = new YearlyFilter(p.getID(), data);
			YearlySummary cy = yf.getSummary(year);
			YearlySummary py = yf.getSummary(year - 1);
			
			// Check for no flights
			if (cy.size() == 0) {
				result.setURL("/jsp/stats/yearReviewEmpty.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Load elite status
			if (SystemData.getBoolean("econ.elite.enabled")) {
				GetElite eldao = new GetElite(con);
				GetEliteStatistics elsdao = new GetEliteStatistics(con);
				SortedSet<EliteLevel> yrLevels = new TreeSet<EliteLevel>(eldao.getLevels(year));
				EliteStatus es = eldao.getStatus(p.getID(), year);
				if ((es == null) && !yrLevels.isEmpty())
					es = new EliteStatus(p.getID(), yrLevels.first());
				
				// Check lifetime elite status
				if (!yrLevels.isEmpty()) {
					final Instant ed = LocalDate.of(year + 1, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
					List<EliteLifetimeStatus> allELS = eldao.getAllLifetimeStatus(p.getID(), ctx.getDB());
					allELS.removeIf(els -> els.getEffectiveOn().isBefore(ed));
					if (!allELS.isEmpty()) {
						EliteLifetimeStatus els = allELS.getLast();
						if ((es != null) && es.overridenBy(els))
							es = els.toStatus();
					}
				}

				// Load elite data
				ctx.setAttribute("eliteStatus", es, REQUEST);
				ctx.setAttribute("eliteLog", eldao.getAllStatus(p.getID(), year), REQUEST);
				ctx.setAttribute("eliteTotals", elsdao.getEliteTotals(p.getID(), year), REQUEST);
				ctx.setAttribute("hasEliteInYear", Boolean.valueOf(!yrLevels.isEmpty()), REQUEST);
			}
			
			// Calculate AI summary
			GetAISummary sdao = new GetAISummary(con);
			AISummary s = sdao.getYearlyReview(p.getID(), year);
			if ((s == null) && SystemData.has("security.key.claude")) {
				GetSystemData ptdao = new GetSystemData(con);
				Prompt pr = ptdao.getPrompt("YEARLYREVIEW", ctx.getDB());
				
				try {
					GetAIYearlyReview aidao = new GetAIYearlyReview(p, cy);
					aidao.setConnectTimeout(2500);
					aidao.setReadTimeout(35500);
					aidao.addExams(th.getExams().stream().filter(YearlyFilter.filter(year)).collect(Collectors.toList()));
					aidao.setPrompt(pr);
					s = aidao.execute();
				} catch (DAOException de) {
					s = new AISummary(p.getID(), de.getMessage());
				}
				
				// Save the summary
				if (s.getGenerationTime() > 0) {
					SetAISummary swdao = new SetAISummary(con);
					swdao.writeSummary(year, s);
				}
			}
				
			// Save in request
			ctx.setAttribute("current", cy, REQUEST);
			ctx.setAttribute("previous", py, REQUEST);
			ctx.setAttribute("summary", s, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		result.setURL("/jsp/stats/yearReview.jsp");
		result.setSuccess(true);
	}
}