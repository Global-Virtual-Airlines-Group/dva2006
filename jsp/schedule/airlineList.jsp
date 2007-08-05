<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Schedule - Airlines</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>
<content:sysdata var="apps" name="apps" />

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" pad="default" space="default" cmd="airlines">

<!-- Table Header Bar -->
<tr class="title">
 <td width="5%">&nbsp;</td>
 <td width="25%">AIRLINE NAME</td>
 <td width="20%">WEB APPLICATIONS</td>
 <td width="15%">AIRLINE CODE</td>
 <td><el:cmdbutton url="airline" op="edit" label="NEW AIRLINE" /></td>
</tr>

<!-- Table Airline Data -->
<c:forEach var="airline" items="${airlines}">
<view:row entry="${airline}">
 <td><map:legend color="${airline.color}" legend="" /></td>
 <td class="pri bld"><el:cmd url="airline" linkID="${airline.code}" op="edit">${airline.name}</el:cmd></td>
 <td class="sec small"><c:forEach var="appCode" items="${airline.applications}">
<c:set var="appName" value="${apps[appCode]}" scope="request" />
${appName.name}<br /></c:forEach></td>
 <td class="bld">${airline.code}</td>
<c:if test="${airline.active}">
 <td class="ter bld">Airline is currently Active</td>
</c:if>
<c:if test="${!airline.active}">
 <td class="warn bld">Airline is currently Inactive</td>
</c:if>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="5">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
