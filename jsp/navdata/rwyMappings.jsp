<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Runway Mappings</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:googleAnalytics />
<content:js name="common" />
<content:cspHeader />
<script>
golgotha.local.update = function() { return document.forms[0].submit(); };
golgotha.local.newAP = function() {
	var icao = document.forms[0].icao;
	if (icao.length > 2)
		self.location = '/rwymapping.do?id=' + icao.toUpper();

	return (icao.length > 2);
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="rwymappings.do" method="post" validate="return false">
<view:table cmd="rwymappings">
<!-- Table Header Bar -->
<tr class="title">
  <td style="width:35%">AIRPORT</td>
  <td style="width:10%">OLD RUNWAY</td>
  <td style="width:10%">NEW RUNWAY</td>
  <td class="left" style="width:15%">INFO</td>
  <td class="right">AIRPORT <el:combo name="id" idx="*"  options="${airports}" required="true" firstEntry="[ SELECT AIRPORT ]" value="${airport}" onChange="void golgotha.local.update()" /></td>
 </tr>
 
 <!-- Table Runway Data -->
<c:forEach var="rm" items="${viewContext.results}">
<c:set var="ap" value="${allAirports[rm.ICAO]}" scope="page" />
<c:set var="rwy" value="${runways[rm.oldCode]}" scope="page" />
<view:row entry="${rm}">
  <td class="bld">${ap.name} (<el:cmd url="airportinfo" linkID="${ap.ICAO}"><fmt:airport airport="${ap}" /></el:cmd>)</td>
  <td class="sec bld"><el:cmd url="rwymapping" linkID="${rm.oldCode}&airport=${ap.ICAO}">${rm.oldCode}</el:cmd></td>
  <td class="pri bld">${rm.newCode}</td>
  <td colspan="2"><fmt:int value="${rwy.length}" /> feet, Heading ${rwy.heading}&deg;, <span class="ter bld">${rwy.surface}</span></td>
 </view:row>
 </c:forEach>
 
 <!-- Scroll Bar -->
<tr class="title">
 <td colspan="5"><el:cmdbutton url="rwymapping" linkID="${ap.ICAO}" label="EDIT RUNWAY MAPPINGS" /> NEW AIRPORT <el:text name="icao" idx="*" size="3" max="4" value="" />&nbsp;<el:button onClick="void gologhta.local.newAP()" label="ADD NEW AIRPORT" /></td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
