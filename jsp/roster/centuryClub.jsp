<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Century Club</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/main/header.jspf" %> 
<%@include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="ccLevels" name="centuryClubLevels" />

<!-- Main Body Frame -->
<content:region id="main">
The <content:airline /> &quot;Century Club&quot; recognizes those pilots who over
the course of their career with our airline have demonstrated a superior level of commitment
and dedication, demonstrated by over one hundred logged flight legs. Over the years, the
&quot;Century Club&quot; has expanded into a number of levels as our pilots have logged more
flights.<br />
<br />
<view:table className="view" pad="default" space="default" cmd="roster">
<!-- Table Header Bar-->
<tr class="title">
 <td width="5%">#</td>
 <td width="10%">PILOT CODE</td>
 <td width="25%">PILOT NAME</td>
 <td width="10%">EQUIPMENT</td>
 <td width="16%">RANK</td>
 <td width="8%">FLIGHTS</td>
 <td width="8%">HOURS</td>
 <td>LAST FLIGHT</td>
</tr>

<!-- Table Pilot Data -->
<c:set var="level" value="${15000 / 1}" scope="request" />
<c:set var="entryNumber" value="0" scope="request" />
<c:forEach var="pilot" items="${roster}">

<c:set var="entryNumber" value="${entryNumber + 1}" scope="request" />
<c:set var="pilotLevel" value="${pilot.legs - (pilot.legs % 100)}" scope="request" />
<c:if test="${pilotLevel < level}">
<!-- Level Header Bar -->
<c:set var="level" value="${pilotLevel}" scope="request" />
<% request.setAttribute("ccLevel", "CC" + String.valueOf(request.getAttribute("pilotLevel"))); %>
<tr class="title">
 <td colspan="8" class="caps left">${ccLevels[ccLevel]} - ${pilotLevel} FLIGHT LEGS</td>
</tr>
</c:if>

<tr>
 <td class="sec bld">${entryNumber}</td>
 <td class="pri bld">${pilot.pilotCode}</td>
 <td><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
 <td class="sec bld">${pilot.equipmentType}</td>
 <td class="pri bld">${pilot.rank}</td>
 <td><fmt:int value="${pilot.legs}" /></td>
 <td><fmt:dec value="${pilot.hours}" /></td>
 <td><fmt:date fmt="d" date="${pilot.lastFlight}" /></td>
</tr>
</c:forEach>
<tr class="title">
 <td colspan="8">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
