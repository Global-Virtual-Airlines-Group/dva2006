<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Equipment Transfer Requests</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<view:table className="view" pad="default" space="default" cmd="eqTypes">
<!-- Table Header Bar-->
<tr class="title caps">
 <td width="30%">PILOT NAME</td>
 <td width="10%">PILOT ID</td>
 <td width="15%">CURRENT RANK</td>
 <td width="15%">CURRENT PROGRAM</td>
 <td width="15%">REQUESTED PROGRAM</td>
 <td>REQUESTED ON</td>
</tr>

<!-- Table Data -->
<c:forEach var="txreq" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[txreq.ID]}" scope="request" />
<view:row entry="${txreq}">
 <td class="bld"><el:cmd url="txreqview" linkID="0x${txreq.ID}">${pilot.name}</el:cmd></td>
 <td class="pri bld"><el:cmd url="profile" linkID="0x${pilot.ID}">${pilot.pilotCode}</el:cmd></td>
 <td class="sec bld">${pilot.rank}</td>
 <td>${pilot.equipmentType}</td>
 <td class="pri bld">${txreq.equipmentType}</td>
 <td class="sec"><fmt:date fmt="d" date="${txreq.date}" /></td>
</view:row>
</c:forEach>

<!-- Scroll bar -->
<tr class="title">
 <td colspan="6"><view:pgUp />&nbsp;<view:pgDn /></td>
</tr>
</view:table>
<content:copyright />
</div>
</body>
</html>
