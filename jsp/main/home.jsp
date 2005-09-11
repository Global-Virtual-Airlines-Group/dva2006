<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
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
<content:pics />
<meta name="Description" content="${desc}" />
<meta name="Keywords" content="<fmt:list value="${keywords}" delim="," />" />
</head>
<content:copyright visible="false" />
<body>
<%@ include file="/jsp/main/header.jsp" %> 
<%@ include file="/jsp/main/sideMenu.jsp" %>
<content:sysdata var="infoEmail" name="airline.mail.info" />
<content:sysdata var="partnerName" name="airline.partner.name" />
<content:sysdata var="partnerURL" name="airline.partner.url" />

<!-- Main Body Frame -->
<div id="main">
Welcome to <content:airline />' web site. We are a group of flight simulation enthusiasts who fly Delta Air
Lines and its alliance partners' routes using Microsoft's Flight Simulator 98, 2000, 2002 or Flight 
Simulator 2004: A Century of Flight. We are in no way affiliated with Delta Air Lines.<br />
<br />
Since May 2003, we have received over <fmt:int value="${httpStats.homeHits}" /> visits and received 
<fmt:int value="${httpStats.hits}" /> hits. During this time, our servers have sent out over
<fmt:int value="${httpStats.bytes}" /> bytes worth of data.<br />
<br />
Please feel free to browse around our web site. Once you join <content:airline />' active pilot roster, 
you may submit flight reports and contribute to our image library. If you are interested in a serious 
virtual airline, designed for both the experienced pilot and the novice (and all of us that are in 
between!) we welcome your interest.<br />
<br />
If you are interested in a virtual airline with primarily European operations, we encourage you to visit 
our sister airline <a href="http://${partnerURL}/" class="sec bld">${partnerName}</a>.<br />
<br />
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
</c:if>
<c:if test="${empty notams}">
<el:table className="view" space="default" pad="default">
<c:forEach var="entry" items="${latestNews}">
<tr>
 <td class="priB" width="20%"><fmt:date fmt="d" date="${entry.date}" /></td>
 <td class="pri bld mid">${entry.subject}</td>
 <td class="secB" width="20%">${entry.authorName}</td>
</tr>
<tr>
 <td class="left" colspan="3">${entry.body}</td>
</tr>
</c:forEach>
</el:table>
</c:if>
<br />
<c:if test="${!empty latestPilots}">
<center><div style="width:510px;">
<table cellspacing="3" cellpadding="3">
<tr>
 <td class="priB mid" colspan="2">WELCOME TO OUR NEWEST PILOTS:</td>
</tr>
<c:forEach var="pilot" items="${latestPilots}">
<tr>
 <td class="priB mid">${pilot.pilotCode}</td>
 <td class="def mid">${pilot.rank} ${pilot.name} (${pilot.equipmentType})</td>
</tr>
</c:forEach>
</table>
</div></center>
<br />
</c:if>
If you have questions or comments, please direct them to our Corporate Offices at 
<a href="${infoEmail}">${infoEmail}</a>.<br />
<br />
<content:copyright />
<br />
</div>
</body>
</html>
