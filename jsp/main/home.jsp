<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /></title>
<content:sysdata var="forumName" name="airline.forum" />
<content:sysdata var="airlineName" name="airline.name" />
<content:sysdata var="desc" name="airline.meta.desc" />
<content:sysdata var="keywords" name="airline.meta.keywords" />
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:rss title="${airlineName} News" path="/news_rss.ws" />
<content:js name="common" />
<content:pics />
<meta name="Description" content="${desc}" />
<meta name="Keywords" content="<fmt:list value="${keywords}" delim="," />" />
</head>
<content:copyright visible="false" />
<body onload="void initLinks()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="infoEmail" name="airline.mail.info" />
<content:sysdata var="partnerName" name="airline.partner.name" />
<content:sysdata var="partnerURL" name="airline.partner.url" />
<content:sysdata var="partnerLoc" name="airline.partner.location" />

<!-- Main Body Frame -->
<content:region id="main">
Welcome to <content:airline />' web site. We are a group of flight simulation enthusiasts who fly Delta Air
Lines and its alliance partners' routes using Microsoft's Flight Simulator 2002, 2004 or Flight 
Simulator X, and Laminar Research X-Plane. We are in no way affiliated with Delta Air Lines.<br />
<br />
Since May 2003, we have received over <fmt:int value="${httpStats.homeHits}" /> visits and received 
<fmt:int value="${httpStats.hits}" /> hits. During this time, our servers have sent out over
<fmt:int value="${httpStats.bytes}" /> bytes worth of data.
<c:if test="${coolerStats > 1}"> Our members have posted over <fmt:quantity value="${coolerStats}" single="message" /> in 
our ${forumName} discussion forum in the past 24 hours.</c:if>
<c:if test="${(!empty runTimeDays) && (runTimeDays > 0)}"> Our web server has been running for <fmt:quantity value="${runTimeDays}" single="day" />, 
<fmt:quantity value="${runTimeHours}" single="hour" /> and <fmt:quantity value="${runTimeMinutes}" single="minute" />.</c:if>
<br />
<br />
<content:filter roles="!Pilot">
Please feel free to browse around our web site. Once you join <content:airline />' active pilot roster, 
you may submit flight reports and contribute to our image library. If you are interested in a serious 
virtual airline, designed for both the experienced pilot and the novice (and all of us that are in 
between!) we welcome your interest. <el:cmd url="register" className="pri bld">Click Here to join 
<content:airline />.</el:cmd><br />
<br />
If you are interested in a virtual airline with primarily ${partnerLoc} operations, we encourage you to visit 
our sister airline <a rel="external" href="http://${partnerURL}/" class="sec bld">${partnerName}</a>.<br />
<br /></content:filter>
<!-- Dynamic Content Type #${dynContentType} -->
<c:if test="${noUpcomingEvents}"><!-- No upcoming Online Events, skipped --></c:if>
<c:if test="${noACARSUsers}"><!-- No connected ACARS users, skipped --></c:if>
<c:if test="${!empty notams}">
<!-- New NOTAMs since last login -->
<el:table className="view" space="default" pad="default">
<tr class="title caps">
 <td colspan="3">THE FOLLOWING NOTAMS HAVE GONE INTO EFFECT SINCE YOUR LAST LOGIN</td>
</tr>
<c:forEach var="notam" items="${notams}">
<tr>
 <td class="priB"><fmt:int value="${notam.ID}" /></td>
 <td width="10%" class="bld"><fmt:date fmt="d" date="${notam.date}" /></td>
 <td><el:cmd url="notamedit" link="${notam}"><fmt:text value="${notam.subject}" /></el:cmd></td>
</tr>
<c:if test="${notam.isHTML}">
 <td colspan="3" class="left">${notam.body}</td>
</c:if>
<c:if test="${!notam.isHTML}">
 <td colspan="3" class="left"><fmt:text value="${notam.body}" /></td>
</c:if>
</c:forEach>
</el:table>
<br />
</c:if>
<c:if test="${!empty latestNews}">
<!-- Latest News -->
<el:table className="view" space="default" pad="default">
<c:forEach var="entry" items="${latestNews}">
<tr>
 <td class="priB" width="20%"><fmt:date fmt="d" date="${entry.date}" /></td>
 <td class="pri bld mid"><fmt:text value="${entry.subject}" /></td>
 <td class="secB" width="20%">${entry.authorName}</td>
</tr>
<tr>
 <td class="left" colspan="3"><fmt:text value="${entry.body}" /></td>
</tr>
</c:forEach>
</el:table>
<br />
</c:if>
<c:if test="${!empty acarsPool}">
<content:filter roles="HR"><c:set var="isHR" value="${true}" scope="request" /></content:filter>
<!-- Current ACARS server connections -->
<el:table className="view" space="default" pad="default">
<tr class="title caps left">
 <td colspan="4">CURRENTLY FLYING USING <content:airline /> ACARS</td>
</tr>
<c:forEach var="con" items="${acarsPool}">
<c:if test="${!con.userHidden || isHR}">
<tr>
 <td class="pri bld"><el:cmd url="profile" link="${con.user}">${con.user.name}</el:cmd></td>
<c:if test="${!empty con.flightInfo.flightCode}">
 <td class="sec bld">${con.flightInfo.flightCode}</td>
 <td class="small bld">${con.flightInfo.equipmentType}</td>
 <td class="small">${con.flightInfo.airportD.name} (<fmt:airport airport="${con.flightInfo.airportD}" />) 
- ${con.flightInfo.airportA.name} (<fmt:airport airport="${con.flightInfo.airportA}" />)</td>
</c:if>
<c:if test="${empty con.flightInfo.flightCode}">
 <td colspan="3" class="sec bld mid">NOT CURRENTLY IN FLIGHT</td>
</c:if>
</tr>
</c:if>
</c:forEach>
</el:table>
</c:if>
<c:if test="${!empty centuryClub}">
<!-- Latest Century Club members -->
<center><div style="width:510px;">
<el:table className="view" space="default" pad="default">
<tr class="title caps left">
 <td colspan="2">OUR NEWEST CENTURY CLUB MEMBERS</td>
</tr>
<c:forEach var="entry" items="${centuryClub}">
<tr>
 <td class="priB mid">${entry.firstName} ${entry.lastName}</td>
 <td class="def mid">${entry.description} on <fmt:date fmt="d" date="${entry.createdOn}" /></td>
</tr>
</c:forEach>
</el:table>
</div></center>
</c:if>
<c:if test="${!empty promotions}">
<!-- Latest Pilot Promotions -->
<center><div style="width:510px;">
<el:table className="view" space="default" pad="default">
<tr class="title caps left">
 <td colspan="2"><content:airline /> CONGRATULATES</td>
</tr>
<c:forEach var="entry" items="${promotions}">
<tr>
 <td class="priB mid">${entry.firstName} ${entry.lastName}</td>
 <td class="def mid">${entry.description} on <fmt:date fmt="d" date="${entry.createdOn}" /></td>
</tr>
</c:forEach>
</el:table>
</div></center>
</c:if>
<c:if test="${!empty futureEvents}">
<!-- Future Online Events -->
<el:table className="view" space="default" pad="default">
<tr class="title caps left">
 <td colspan="4">UPCOMING <content:airline /> ONLINE EVENTS</td>
</tr>
<c:forEach var="event" items="${futureEvents}">
<c:set var="eRoute" value="${fn:first(event.routes)}" scope="request" />
<tr>
 <td class="pri bld"><el:cmd url="event" link="${event}"><fmt:text value="${event.name}" /></el:cmd></td>
 <td class="sec bld">${event.networkName}</td>
 <td class="small bld"><fmt:date t="HH:mm" date="${event.startTime}" /> - <fmt:date t="HH:mm" date="${event.endTime}" /></td>
<c:if test="${empty eRoute}">
 <td class="small caps">NO AVAILABLE ROUTES</td>
</c:if>
<c:if test="${!empty eRoute}">
 <td class="left small">${eRoute.airportD.name} (<fmt:airport airport="${eRoute.airportD}" />) - 
${eRoute.airportA.name} (<fmt:airport airport="${eRoute.airportA}" />)</td>
</c:if>
</tr>
</c:forEach>
</el:table>
</c:if>
<c:if test="${!empty latestPilots}">
<!-- Latest Pilot Hires -->
<center><div style="width:530px;">
<el:table className="view" space="default" pad="default">
<tr class="title caps left">
 <td colspan="2"><content:airline /> WELCOMES OUR NEWEST PILOTS</td>
</tr>
<c:forEach var="pilot" items="${latestPilots}">
<tr>
 <td class="priB mid">${pilot.pilotCode}</td>
 <td class="def mid">${pilot.rank} ${pilot.name} (${pilot.equipmentType})</td>
</tr>
</c:forEach>
</el:table>
</div></center>
<br />
</c:if>
If you have questions or comments, please direct them to our Corporate Offices at 
<a href="mailto:${infoEmail}">${infoEmail}</a>.<br />
<br />
<div class="mid"><a rel="external" href="http://www.vatsim.net/"><el:img src="network/vatsim_button.png" caption="VATSIM Partner Airline" border="0" /></a></div>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
