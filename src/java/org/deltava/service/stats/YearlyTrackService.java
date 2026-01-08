// Copyright 2025, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static jakarta.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.*;

import java.sql.Connection;

import org.json.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.acars.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Sevice to display a yaer's worth of flight tracks for the Year in Review page.
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public class YearlyTrackService extends WebService {
	
	private static final Logger log = LogManager.getLogger(YearlyTrackService.class);
	
	private static final Cache<CacheableCollection<FlightReport>> _cache = CacheManager.getCollection(FlightReport.class, "Logbook");
	
	private record FlightLandingScore(int id, double score) {
		public JSONArray toJSON() {
			LandingRating lr = LandingRating.rate((int)score);
			JSONArray ja = new JSONArray();
			ja.put(id);
			ja.put(score);
			ja.put(String.format("color:#%s", lr.getHexColor()));
			ja.put(String.format("%.2f - %s", Double.valueOf(score), lr.getDescription()));
			return ja;
		}
	}
	
	private record TrackData(int id, boolean isACARS, Instant date, Collection<? extends GeoLocation> track) implements Comparable<TrackData> {
		@Override
		public int compareTo(TrackData td2) {
			int tmpResult = date.compareTo(td2.date);
			return (tmpResult == 0) ? Integer.compare(id, td2.id) : tmpResult;
		}
	}
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the year and user ID
		int year = StringUtils.parse(ctx.getParameter("year"), LocalDate.now().getYear());
		int userID = ctx.getUser().getID();
		int id = StringUtils.parse(ctx.getParameter("id"), 0);
		if ((ctx.isUserInRole("Operations") || ctx.isUserInRole("HR")) && (id > 0))
			userID = id;
		
		// Build date range for flights
		final Instant ed = LocalDate.of(year + 1, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
		final Instant sd = LocalDate.of(year, 1, 1).atStartOfDay().minusSeconds(1).toInstant(ZoneOffset.UTC);

		String code = SystemData.get("airline.code");
		Collection<TrackData> tracks = new TreeSet<TrackData>();
		Collection<Count<String>> eqCounts = new TreeSet<Count<String>>(Collections.reverseOrder());
		Collection<FlightLandingScore> landingScores = new ArrayList<FlightLandingScore>();
		Collection<Count<FlightScore>> scoreCounts = new TreeSet<Count<FlightScore>>(Count.labelComparator(FlightScore.class));
		try {
			Connection con = ctx.getConnection();
			
			// Get the Flight reports
			CacheableCollection<FlightReport> data = _cache.get(Integer.valueOf(id));
			if (data == null) {
				GetFlightReports rdao = new GetFlightReports(con);
				List<FlightReport> pireps = rdao.getByPilot(userID, new LogbookSearchCriteria(null, ctx.getDB()));
				rdao.loadCaptEQTypes(id, pireps, ctx.getDB());
				data = new CacheableList<FlightReport>(Integer.valueOf(id), pireps);
				_cache.add(data);
			}
			
			// Get this year's flights, split into ACARS and non-ACARS
			Collection<FlightReport> flights = data.stream().filter(fr -> fr.getDate().isAfter(sd) && fr.getDate().isBefore(ed)).collect(Collectors.toList());
			eqCounts.addAll(CollectionUtils.count(flights, FlightReport::getEquipmentType));
			Collection<ACARSFlightReport> acarsFlights = flights.stream().filter(ACARSFlightReport.class::isInstance).map(ACARSFlightReport.class::cast).collect(Collectors.toList());
			flights.removeAll(acarsFlights);
			
			// Calculate flight scores
			int idx = 0;
			GetAircraft acdao = new GetAircraft(con);
			GetACARSData fidao = new GetACARSData(con);
			GetACARSPositions posdao = new GetACARSPositions(con);
			posdao.setAllowMissingMetadata(true);
			
			Collection<FlightScore> scores = new ArrayList<FlightScore>();
			for (ACARSFlightReport afr : acarsFlights) {
				landingScores.add(new FlightLandingScore(++idx, afr.getLandingScore()));
				int flightID = afr.getDatabaseID(DatabaseID.ACARS);
				FlightInfo inf = fidao.getInfo(flightID);
				if (inf == null) {
					log.warn("No ACARS Flight Info for Flight {}", Integer.valueOf(flightID));
					continue;
				}

				// Calculate flight score
				Aircraft acInfo = acdao.get(afr.getEquipmentType());
				AircraftPolicyOptions opts = (acInfo == null) ? null : acInfo.getOptions(code);
				ScorePackage pkg = new ScorePackage(acInfo, afr, inf.getRunwayD(), inf.getRunwayA(), opts);
				FlightScore fs = FlightScorer.score(pkg);
				if (fs != FlightScore.INCOMPLETE)
					scores.add(fs);
				
				// Load sparse track data
				Instant dt = (afr.getSubmittedOn() == null) ? afr.getDate() : afr.getSubmittedOn();
				try {
					List<GeoLocation> track = new ArrayList<GeoLocation>();
					track.addAll(posdao.getRouteEntries(inf.getID(), inf.getArchived()));
					if (GeoUtils.crossesMeridian(afr.getAirportD(), afr.getAirportA(), -179.5))
						GeoUtils.translate(track);
					
					tracks.add(new TrackData(afr.getID(), true, dt, GeoUtils.thin(track, 2)));
				} catch (ArchiveValidationException ave) {
					log.warn("{} - exists={}", ave.getMessage(), Boolean.valueOf(ave.getFileExists()));
					tracks.add(new TrackData(afr.getID(), false, dt, GeoUtils.greatCircle(afr.getAirports())));
				}
			}
			
			// Build GC track for non-ACARS flights
			flights.stream().map(fr -> new TrackData(fr.getID(), false, fr.getDate(), GeoUtils.greatCircle(fr.getAirports()))).forEach(tracks::add);
			scoreCounts.addAll(CollectionUtils.count(scores, Function.identity()));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Create the track data
		JSONObject jo = new JSONObject();
		jo.put("userID", userID);
		jo.put("size", tracks.size());
		jo.put("year", year);
		for (TrackData td : tracks) {
			JSONObject to = new JSONObject();
			to.put("id", td.id);
			to.put("isACARS", td.isACARS);
			td.track.forEach(loc -> to.accumulate("trk", JSONUtils.format(loc)));
			JSONUtils.ensureArrayPresent(to, "trk");
			jo.accumulate("tracks", to);
		}
		
		// Add score statistics
		scoreCounts.stream().map(JSONUtils::format).forEach(ja -> jo.accumulate("flightScores", ja));
		landingScores.stream().map(FlightLandingScore::toJSON).forEach(ja -> jo.accumulate("landingScores", ja));
		eqCounts.stream().map(JSONUtils::format).forEach(ja -> jo.accumulate("eqCounts", ja));
		
		// Get landing ratings
		List<Count<LandingRating>> ratingCount = CollectionUtils.count(landingScores.stream().map(fls -> LandingRating.rate((int)fls.score)).collect(Collectors.toList()), Function.identity());
		ratingCount.sort(Count.labelComparator(LandingRating.class).reversed());
		ratingCount.stream().map(JSONUtils::format).forEach(ja -> jo.accumulate("lsCounts", ja));
				
		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "tracks", "landingScores", "flightScores", "eqCounts", "lsCounts");
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.setExpiry(7200);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}
	
	@Override
	public final boolean isSecure() {
		return true;
	}

	@Override
	public final boolean isLogged() {
		return false;
	}
}