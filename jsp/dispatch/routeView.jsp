<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title><content:airline /> ACARS Dispatcher Route - <fmt:int value="${route.ID}" /></title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<content:js name="googleMaps" />
<map:api version="2" />
<map:vml-ie />
</head>
<content:copyright visible="false" />
<body onunload="GUnload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="imgPath" name="path.img" />

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="4">ACARS DISPATCH ROUTE - ROUTE #<fmt:int value="${route.ID}" /></td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data">${route.airportD.name} (<fmt:airport airport="${route.airportD}" />)</td>
 <td class="label">Departure Procedure</td>
 <td class="data pri">${(empty route.SID) ? 'NONE' : route.SID}</td>
</tr>
<tr>
 <td class="label">Arriving at</td>
 <td class="data">${route.airportA.name} (<fmt:airport airport="${route.airportA}" />)</td>
 <td class="label">Arrival Route</td>
 <td class="data pri">${(empty route.STAR) ? 'NONE' : route.STAR}</td>
</tr>
<c:if test="${!empty route.airportL}">
<tr>
 <td class="label">Alternate</td>
 <td class="data" colspan="3">${route.airportL.name} (<fmt:airport airport="${route.airportL}" />)</td>
</tr>
</c:if>
<tr>
 <td class="label">Dispatcher Name</td>
 <td class="data">${author.pilotCode} ${author.name}</td>
 <td class="label">Created on</td>
 <td class="data bld"><fmt:date date="${route.createdOn}" /> (used <fmt:int value="${route.useCount}" /> times)</td>
</tr>
<c:if test="${!empty route.comments}">
<tr>
 <td class="label" valign="top">Dispatcher Comments</td>
 <td class="data" colspan="3"><fmt:text value="${route.comments}" /></td>
</tr>
</c:if>
<tr>
 <td class="label" valign="top">Flight Route</td>
 <td class="data" colspan="3">${route.route}</td>
</tr>
<tr>


</tr>
