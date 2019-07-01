<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title><content:airline /> Schedule - Airlines</title>
<content:css name="main" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<content:favicon />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>
<content:sysdata var="apps" name="apps" />

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="airlines">

<!-- Table Header Bar -->
<tr class="title">
 <td style="width:5%">&nbsp;</td>
 <td style="width:20%">AIRLINE NAME</td>
 <td style="width:20%">WEB APPLICATIONS</td>
 <td style="width:8%">AIRLINE CODE</td>
 <td style="width:10%">TYPE</td>
 <td style="width:8%">AIRPORTS</td>
 <td style="width:8%">FLIGHTS</td>
 <td><el:cmdbutton url="airline" op="edit" label="NEW AIRLINE" /></td>
</tr>

<!-- Table Airline Data -->
<c:forEach var="airline" items="${airlines}">
<view:row entry="${airline}">
 <td><map:legend color="${airline.color}" legend="" /></td>
 <td class="pri bld"><el:cmd url="airline" linkID="${airline.code}" op="edit">${airline.name}</el:cmd></td>
 <td class="sec small"><c:forEach var="appCode" items="${airline.applications}" varStatus="aStatus">
<c:set var="appName" value="${apps[appCode]}" scope="page" />
${appName.name}<c:if test="${!aStatus.last}"><br /></c:if></c:forEach></td>
 <td class="bld">${airline.code}</td>
<c:if test="${airline.historic}">
 <td class="sec bld caps">Historic</td>
</c:if>
<c:if test="${!airline.historic}">
 <td class="caps">Current</td>
</c:if>
 <td class="sec bld"><fmt:int value="${apCount[airline]}" /></td>
 <td class="bld"><fmt:int value="${fCount[airline]}" /></td>
<c:if test="${airline.active}">
 <td class="ter bld">Airline is currently Active</td></c:if>
<c:if test="${!airline.active}">
 <td class="warn bld">Airline is currently Inactive</td></c:if>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="7">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
