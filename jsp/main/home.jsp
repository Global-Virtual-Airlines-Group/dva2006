<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /></title>
<content:sysdata var="airlineName" name="airline.name" />
<content:sysdata var="desc" name="airline.meta.desc" />
<content:sysdata var="keywords" name="airline.meta.keywords" />
<c:set var="serverName" value="${pageContext.request.serverName}" scope="request" />
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:rss title="${airlineName} News" url="http://${serverName}/news_rss.ws" />
<content:js name="common" />
<content:pics />
<meta name="Description" content="${desc}" />
<meta name="Keywords" content="<fmt:list value="${keywords}" delim="," />" />
</head>
<content:copyright visible="false" />
<body onload="void initLinks()">
<content:page>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>
<content:sysdata var="infoEmail" name="airline.mail.info" />
<content:sysdata var="partnerName" name="airline.partner.name" />
<content:sysdata var="partnerURL" name="airline.partner.url" />
<content:sysdata var="partnerLoc" name="airline.partner.location" />

<!-- Main Body Frame -->
<content:region id="main">
Welcome to <content:airline />' web site. We are a group of flight simulation enthusiasts who fly Delta Air
Lines and its alliance partners' routes using Microsoft's Flight Simulator 98, 2000, 2002 or Flight 
Simulator 2004: A Century of Flight. We are in no way affiliated with Delta Air Lines.<br />
<br />
Since May 2003, we have received over <fmt:int value="${httpStats.homeHits}" /> visits and received 
<fmt:int value="${httpStats.hits}" /> hits. During this time, our servers have sent out over
<fmt:int value="${httpStats.bytes}" /> bytes worth of data. <c:if test="${coolerStats > 1}">Our 
members have posted over <fmt:int value="${coolerStats}" /> messages in our Water Cooler discussion 
forum in the past 24 hours.</c:if><br />
<br />
<content:filter roles="!Pilot">
Please feel free to browse around our web site. Once you join <content:airline />' active pilot roster, 
you may submit flight reports and contribute to our image library. If you are interested in a serious 
virtual airline, designed for both the experienced pilot and the novice (and all of us that are in 
between!) we welcome your interest.<br />
<br />
<el:cmd url="register" className="pri bld">Click Here to join <content:airline />.</el:cmd><br />
<br />
If you are interested in a virtual airline with primarily ${partnerLoc} operations, we encourage you to visit 
our sister airline <a rel="external" href="http://${partnerURL}/" class="sec bld">${partnerName}</a>.<br />
<br />
</content:filter>
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
 <td><el:cmd url="notamedit" linkID="0x${notam.ID}"><fmt:text value="${notam.subject}" /></el:cmd></td>
</tr>
<tr>
 <td colspan="3" class="left"><fmt:text value="${notam.body}" /></td>
</tr>
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
 <td class="pri bld mid">${entry.subject}</td>
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
<!-- Current ACARS server connections -->
<el:table className="view" space="default" pad="default">
<tr class="title caps left">
 <td colspan="3">CURRENTLY FLYING USING <content:airline /> ACARS</td>
</tr>
<c:forEach var="con" items="${acarsPool}">
<tr>
 <td class="pri bld"><el:cmd url="profile" linkID="0x${con.user.ID}">${con.user.name}</el:cmd></td>
<c:if test="${con.flightID > 0}">
 <td class="sec bld">${con.flightInfo.flightCode}</td>
 <td class="small">${con.flightInfo.airportD.name} (<fmt:airport airport="${con.flightInfo.airportD}" />) 
- ${con.flightInfo.airportA.name} (<fmt:airport airport="${con.flightInfo.airportA}" />)</td>
</c:if>
<c:if test="${con.flightID == 0}">
 <td colspan="2" class="sec bld mid">NOT CURRENTLY IN FLIGHT</td>
</c:if>
</tr>
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
 <td class="pri bld"><el:cmd url="event" linkID="0x${event.ID}">${event.name}</el:cmd></td>
 <td class="sec bld">${event.networkName}</td>
 <td class="small bld"><fmt:date date="${event.startTime}" /> - <fmt:date date="${event.endTime}" /></td>
 <td class="left small">${eRoute.airportD.name} (<fmt:airport airport="${eRoute.airportD}" />) - 
${eRoute.airportA.name} (<fmt:airport airport="${eRoute.airportA}" />)</td>
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
<content:copyright />
</content:region>
</content:page>
</body>
</html>
