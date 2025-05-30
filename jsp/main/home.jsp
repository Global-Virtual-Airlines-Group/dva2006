<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /></title>
<content:sysdata var="forumName" name="airline.forum" />
<content:sysdata var="airlineName" name="airline.name" />
<content:sysdata var="airlineURL" name="airline.url" />
<content:sysdata var="desc" name="airline.meta.desc" />
<content:sysdata var="keywords" name="airline.meta.keywords" />
<content:canonical url="https://${airlineURL}/" />
<content:css name="main" />
<content:css name="view" />
<content:rss title="${airlineName} News" path="/news_rss.ws" />
<content:js name="common" />
<content:googleAnalytics />
<content:captcha action="home" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<meta name="Description" content="${desc}" />
<meta name="Keywords" content="<fmt:list value="${keywords}" delim="," />" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="infoEmail" name="airline.mail.info" />

<!-- Main Body Frame -->
<content:region id="main">
Welcome to the <span class="bld"><content:airline /></span> web site. We are a group of flight simulation enthusiasts who fly Delta Air Lines and its alliance partners' routes using Microsoft Flight Simulator 2004, Flight  Simulator X (including Steam Edition)
and Microsoft Flight Simulator 2020. We also support Lockheed-Martin Prepar3D v1-5 and Laminar Research X-Plane 10/11/12. We are in no way affiliated with Delta Air Lines.<br />
<br />
Since May 2003, we have received over <fmt:int value="${httpStats.homeHits}" /> visits and received <fmt:int value="${httpStats.hits}" /> hits. During this time, our servers have sent out over <fmt:int value="${httpStats.bytes}" /> bytes worth of data.
<c:if test="${coolerStats > 1}"> Our members have posted over <fmt:quantity value="${coolerStats}" single="message" /> in our ${forumName} discussion forum in the past 24 hours.</c:if>
<c:if test="${!empty runTime}"> Our web server has been running for <fmt:quantity value="${runTime.toDays()}" single="day" />, <fmt:quantity value="${runTime.toHoursPart()}" single="hour" /> and <fmt:quantity value="${runTime.toMinutesPart()}" single="minute" />.</c:if>
<br />
<br />
<content:filter roles="!Pilot">
Please feel free to browse around our web site. Once you join the <content:airline /> active pilot roster, you may submit flight reports and contribute to our discussion forums and image library. If you are interested in 
a serious virtual airline, designed for both the experienced pilot and the novice (and all of us that are in between!) we welcome your interest. <el:cmd url="register" className="pri bld">Click Here to join <content:airline />.</el:cmd><br />
<br /></content:filter>
<br />
<!-- Dynamic Content Type ${dynContentType} -->
<c:if test="${noUpcomingEvents}"><!-- No upcoming Online Events, skipped --></c:if>
<c:if test="${noACARSUsers}"><!-- No connected ACARS users, skipped --></c:if>
<c:if test="${!empty notams}">
<!-- New NOTAMs since last login -->
<el:table className="view">
<tr class="title caps">
 <td colspan="3">THE FOLLOWING NOTAMS HAVE GONE INTO EFFECT SINCE YOUR LAST LOGIN</td>
</tr>
<c:forEach var="notam" items="${notams}">
<tr>
 <td class="priB"><fmt:int value="${notam.ID}" /></td>
 <td style="width:10%;" class="bld"><fmt:date fmt="d" date="${notam.date}" /></td>
 <td><el:cmd url="notamedit" link="${notam}"><fmt:text value="${notam.subject}" /></el:cmd></td>
</tr>
<c:if test="${notam.isHTML}">
 <td colspan="3" class="left notam"><c:if test="${notam.hasImage}"><div class="hdr"><el:dbimg img="${notam}" style="width:${notam.bannerWidth}%; max-width=${notam.bannerWidth}%" caption="${notam.subject}"  /></div></c:if>
 ${notam.body}</td>
</c:if>
<c:if test="${!notam.isHTML}">
 <td colspan="3" class="left notam"><fmt:msg value="${notam.body}" bbCode="true" /></td>
</c:if>
</c:forEach>
</el:table>
</c:if>
<c:if test="${!empty acarsPool}">
<content:attr attr="isHR" roles="HR" value="true" />
<!-- Current ACARS server connections -->
<el:table className="view">
<tr class="title caps left">
 <td colspan="7">CURRENTLY FLYING USING <content:airline /> ACARS</td>
</tr>
<c:forEach var="con" items="${acarsPool}">
<c:if test="${!con.userHidden || isHR}">
<c:set var="inf" value="${con.flightInfo}" scope="page" />
<tr>
 <td class="pri bld"><el:cmd url="profile" link="${con.user}">${con.user.name}</el:cmd></td>
<c:choose>
<c:when test="${!empty inf.flightCode && empty inf.endTime}">
 <td class="sec bld" title="ACARS Flight ${inf.ID}">${inf.flightCode}</td>
 <td class="nophone small bld">${inf.equipmentType}</td>
 <td class="nophone small ter">${inf.simulator}</td>
 <td class="nophone small sec">${con.flightPhase.name}</td>
 <td class="nophone bld"><fmt:duration duration="${inf.duration}" t="HH:mm" /></td>
 <td class="small">${inf.airportD.name} (<fmt:airport airport="${inf.airportD}" />) - ${inf.airportA.name} (<fmt:airport airport="${inf.airportA}" />)</td>
</c:when>
<c:when test="${con.dispatch}">
 <td colspan="6" class="pri bld mid">PROVIDING ACARS DISPATCHER SERVICES</td>
</c:when>
<c:otherwise>
 <td colspan="6" class="sec bld mid">NOT CURRENTLY IN FLIGHT</td>
</c:otherwise>
</c:choose>
</tr>
</c:if>
</c:forEach>
</el:table>
</c:if>
<c:if test="${!empty centuryClub}">
<!-- Latest Century Club members -->
<el:table className="view mid" style="width:100%; max-width:510px;">
<tr class="title caps left">
 <td colspan="2">OUR NEWEST CENTURY CLUB MEMBERS</td>
</tr>
<c:forEach var="entry" items="${centuryClub}">
<c:set var="pilot" value="${updPilots[entry.ID]}" scope="page" />
<tr>
 <td class="priB mid">${pilot.name}</td>
 <td class="def mid">${entry.description} on <fmt:date fmt="d" date="${entry.date}" /></td>
</tr>
</c:forEach>
</el:table>
</c:if>
<c:if test="${!empty promotions}">
<!-- Latest Pilot Promotions -->
<el:table className="view mid" style="width:100%; max-width:510px;">
<tr class="title caps left">
 <td colspan="2"><content:airline /> CONGRATULATES</td>
</tr>
<c:forEach var="entry" items="${promotions}">
<c:set var="pilot" value="${updPilots[entry.ID]}" scope="page" />
<tr>
 <td class="priB mid">${pilot.name}</td>
 <td class="def mid">${entry.description} on <fmt:date fmt="d" date="${entry.date}" /></td>
</tr>
</c:forEach>
</el:table>
</c:if>
<c:if test="${!empty toursCompleted}">
<!-- Recently Completed Flight Tours -->
<el:table className="view mid" style="width:100%; max-width:600px;">
<tr class="title caps left">
 <td colspan="2">RECENTLY COMPLETED FLIGHT TOURS</td>
</tr>
<c:forEach var="tour" items="${toursCompleted}">
<c:set var="pilot" value="${updPilots[tour.ID]}" scope="page" />
<tr>
 <td class="priB mid">${pilot.name}</td>
 <td class="def mid">${tour.description} on <fmt:date fmt="d" date="${tour.date}" /></td>
</tr>
</c:forEach>
</el:table>
</c:if>
<c:if test="${!empty futureEvents}">
<!-- Future Online Events -->
<el:table className="view mid" style="width:90%; max-width:980px;">
<tr class="title caps left">
 <td colspan="4">UPCOMING <content:airline /> ONLINE EVENTS</td>
</tr>
<c:forEach var="event" items="${futureEvents}">
<c:set var="eRoute" value="${fn:first(event.routes)}" scope="page" />
<tr>
 <td class="pri bld"><el:cmd url="event" link="${event}"><fmt:text value="${event.name}" /></el:cmd></td>
 <td class="sec bld">${event.network}</td>
 <td class="small bld"><fmt:date t="HH:mm" date="${event.startTime}" /> - <fmt:date t="HH:mm" date="${event.endTime}" /></td>
<c:if test="${empty eRoute}">
 <td class="nophone small caps">NO AVAILABLE ROUTES</td>
</c:if>
<c:if test="${!empty eRoute}">
 <td class="nophone left small">${eRoute.airportD.name} (<el:cmd url="airportInfo" linkID="${eRoute.airportD.IATA}" authOnly="true" className="plain"><fmt:airport airport="${eRoute.airportD}" /></el:cmd>) - 
 ${eRoute.airportA.name} (<el:cmd url="airportInfo" linkID="${eRoute.airportA.IATA}" authOnly="true" className="plain"><fmt:airport airport="${eRoute.airportA}" /></el:cmd>)</td>
</c:if>
</tr>
</c:forEach>
</el:table>
</c:if>
<c:if test="${!empty toLand}">
<!-- Latest Takeoffs/Landings -->
<el:table className="view mid" style="width:100%; max-width:790px;">
<tr class="title caps left">
 <td colspan="3">LATEST <content:airline /> ACARS FLIGHT DEPARTURES AND ARRIVALS</td>
</tr>
<c:forEach var="tl" items="${toLand.keySet()}">
<c:set var="info" value="${toLand[tl]}" scope="page" />
<tr>
 <td class="priB" style="width:115px;">${info.flightCode}</td>
 <td class="secB nophone" style="width:145px;">${info.equipmentType}</td>
<c:if test="${tl.isTakeoff}">
 <td class="left">Departed from ${info.airportD.name} (<el:cmd url="airportInfo" linkID="${info.airportD.IATA}" authOnly="true" className="plain"><fmt:airport airport="${info.airportD}" /></el:cmd>) at <fmt:date date="${tl.date}" t="HH:mm" /></td></c:if>
<c:if test="${!tl.isTakeoff}">
 <td class="left">Arrived at ${info.airportA.name} (<el:cmd url="airportInfo" linkID="${info.airportA.IATA}" authOnly="true" className="plain"><fmt:airport airport="${info.airportA}" /></el:cmd>) at <fmt:date date="${tl.date}" t="HH:mm" /></td></c:if>
</tr>
</c:forEach>
</el:table>
</c:if>
<c:if test="${!empty latestPilots}">
<!-- Latest Pilot Hires -->
<el:table className="view mid" style="width:100%; max-width:530px;">
<tr class="title caps left">
 <td colspan="2"><content:airline /> WELCOMES OUR NEWEST PILOTS</td>
</tr>
<c:forEach var="pilot" items="${latestPilots}">
<tr>
 <td class="priB mid">${pilot.pilotCode}</td>
 <td class="def mid">${pilot.rank.name}&nbsp;${pilot.name} (${pilot.equipmentType})</td>
</tr>
</c:forEach>
</el:table>
</c:if>
<c:if test="${!empty latestNews}">
<br />
<!-- Latest News -->
<el:table className="view">
<c:forEach var="entry" items="${latestNews}">
<c:set var="author" value="${authors[entry.authorID]}" scope="page" />
<tr>
 <td class="priB" style="width:20%;"><fmt:date fmt="d" date="${entry.date}" /></td>
 <td class="pri bld mid"><fmt:text value="${entry.subject}" /></td>
 <td class="secB" style="width:20%;">${author.name}</td>
</tr>
<tr>
<c:if test="${entry.isHTML}">
 <td class="left news" colspan="3"><c:if test="${entry.hasImage}"><div class="hdr"><el:dbimg img="${entry}" style="width:${entry.bannerWidth}%; max-width=${entry.bannerWidth}%" caption="${entry.subject}"  /></div></c:if>
${entry.body}</td>
</c:if>
<c:if test="${!entry.isHTML}">
 <td class="left news" colspan="3"><fmt:msg value="${entry.body}" bbCode="true" /></td>
</c:if>
</tr>
</c:forEach>
</el:table>
</c:if>
<br />
If you have questions or comments, please direct them to our Corporate Offices at <a href="mailto:${infoEmail}">${infoEmail}</a>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
