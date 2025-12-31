// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.time.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.econ.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display a Year in Review page. 
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public class YearlyReviewCommand extends AbstractCommand {
	
	private static final Cache<CacheableCollection<FlightReport>> _cache = CacheManager.getCollection(FlightReport.class, "Logbook");

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the user ID
		int userID = ctx.getUser().getID();
		int id = StringUtils.parse(ctx.getParameter("id"), 0);
		if ((ctx.isUserInRole("Operations") || ctx.isUserInRole("HR")) && (id > 0))
			userID = id;
		
		// Get the year - don't allow current year until after 11/30
		LocalDate now = LocalDate.now();
		int currentYear = now.getYear();
		int year = StringUtils.parse(ctx.getParameter("year"), currentYear);
		if ((year > currentYear) || ((year == currentYear) && (now.getMonth() != Month.DECEMBER)))
			year = currentYear - 1;
		
		// Build date range for flights
		final Instant ed = LocalDate.of(year + 1, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
		final Instant sd = LocalDate.of(year, 1, 1).atStartOfDay().minusSeconds(1).toInstant(ZoneOffset.UTC);
		final Instant lsd = LocalDate.of(year - 1, 1, 1).atStartOfDay().minusSeconds(1).toInstant(ZoneOffset.UTC);
		
		CommandResult result = ctx.getResult();
		try {
			Connection con = ctx.getConnection();
			
			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(userID);
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + userID);
			
			// Load the log book and add to cache
			CacheableCollection<FlightReport> data = _cache.get(p.cacheKey());
			if (data == null) {
				GetFlightReports rdao = new GetFlightReports(con);
				List<FlightReport> pireps = rdao.getByPilot(p.getID(), new LogbookSearchCriteria(null, ctx.getDB()));
				rdao.loadCaptEQTypes(p.getID(), pireps, ctx.getDB());
				data = new CacheableList<FlightReport>(p.cacheKey(), pireps);
				_cache.add(data);
			}
			
			// Remove future flights - clone data before you do so since the cache may be local
			List<Integer> years = data.stream().mapToInt(fr -> LocalDate.ofInstant(fr.getDate(), ZoneOffset.UTC).getYear()).distinct().boxed().collect(Collectors.toList());
			Collection<FlightReport> flights = data.stream().filter(fr -> fr.getDate().isBefore(ed)).collect(Collectors.toList());
			Collections.sort(years, Comparator.reverseOrder());
			
			// Save pilot and years
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("years", years, REQUEST);
			ctx.setAttribute("year", Integer.valueOf(year), REQUEST);

			// Check for no flights
			if (flights.isEmpty()) {
				result.setURL("/jsp/stats/yearReviewEmpty.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Load current year and all previous years flights
			Collection<FlightReport> cyFlights = flights.stream().filter(fr -> fr.getDate().isAfter(sd)).collect(Collectors.toList());
			Collection<FlightReport> pyFlights = flights.stream().filter(fr -> !cyFlights.contains(fr)).collect(Collectors.toList());
			Collection<Airport> cyAP = cyFlights.stream().map(RoutePair::getAirports).flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));
			Collection<String> cyEQ = cyFlights.stream().map(FlightReport::getEquipmentType).collect(Collectors.toCollection(TreeSet::new));
			
			// Get last year's flights
			Collection<FlightReport> lyFlights = pyFlights.stream().filter(fr -> fr.getDate().isAfter(lsd)).collect(Collectors.toList());
			
			// Load previous airports and equipment
			Collection<Airport> pyAP = pyFlights.stream().map(RoutePair::getAirports).flatMap(Collection::stream).collect(Collectors.toSet());
			Collection<String> pyEQ = pyFlights.stream().map(FlightReport::getEquipmentType).collect(Collectors.toSet());
			Collection<Airport> newAP = cyAP.stream().filter(ap -> !pyAP.contains(ap)).collect(Collectors.toSet());
			Collection<String> newEQ = cyEQ.stream().filter(eq -> !pyEQ.contains(eq)).collect(Collectors.toSet());
			Collection<Airport> lyAP = lyFlights.stream().map(RoutePair::getAirports).flatMap(Collection::stream).collect(Collectors.toSet());
			Collection<String> lyEQ = lyFlights.stream().map(FlightReport::getEquipmentType).collect(Collectors.toSet());
			
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
					List<EliteLifetimeStatus> allELS = eldao.getAllLifetimeStatus(p.getID(), ctx.getDB());
					allELS.removeIf(els -> els.getEffectiveOn().isBefore(ed));
					EliteLifetimeStatus els = allELS.getLast();
					if ((es != null) && es.overridenBy(els))
						es = els.toStatus();
				}

				// Load elite data
				ctx.setAttribute("eliteStatus", es, REQUEST);
				ctx.setAttribute("eliteLog", eldao.getAllStatus(p.getID(), year), REQUEST);
				ctx.setAttribute("eliteTotals", elsdao.getEliteTotals(p.getID(), year), REQUEST);
				ctx.setAttribute("hasEliteInYear", Boolean.valueOf(!yrLevels.isEmpty()), REQUEST);
			}
			
			// Save in request
			ctx.setAttribute("cyFlights", cyFlights, REQUEST);
			ctx.setAttribute("lyFlights", lyFlights, REQUEST);
			ctx.setAttribute("cyAirports", cyAP, REQUEST);
			ctx.setAttribute("lyAirports", lyAP, REQUEST);
			ctx.setAttribute("cyEquipment", cyEQ, REQUEST);
			ctx.setAttribute("lyEquipment", lyEQ, REQUEST);
			ctx.setAttribute("cyDistance", Integer.valueOf(cyFlights.stream().mapToInt(FlightReport::getDistance).sum()), REQUEST);
			ctx.setAttribute("lyDistance", Integer.valueOf(lyFlights.stream().mapToInt(FlightReport::getDistance).sum()), REQUEST);
			ctx.setAttribute("newEQ", newEQ, REQUEST);
			ctx.setAttribute("newAP", newAP, REQUEST);
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