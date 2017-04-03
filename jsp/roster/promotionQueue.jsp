<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Promotion Queue</title>
<content:css name="main" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:attr attr="isHR" value="true" roles="HR" />

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="view">
<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:15%">&nbsp;</td>
 <td style="width:10%">PILOT CODE</td>
 <td style="width:${isHR ? 25 : 30}%">PILOT NAME</td>
<c:if test="${isHR}"><td style="width:10%">EQUIPMENT</td></c:if>
 <td style="width:10%">TOTAL</td>
 <td style="width:10%">ACARS</td>
 <td style="width:10%">ONLINE</td>
 <td>LAST FLIGHT</td>
</tr>

<!-- Table Pilot Data -->
<c:forEach var="pilot" items="${queue}">
<c:set var="access" value="${accessMap[pilot.ID]}" scope="page" />
<view:row entry="${pilot}">
<c:if test="${access.canPromote}">
 <td><el:cmdbutton url="promote" link="${pilot}" label="PROMOTE" /></td>
</c:if>
<c:if test="${!access.canPromote}">
 <td>&nbsp;</td>
</c:if>
 <td class="pri bld">${pilot.pilotCode}</td>
 <td><el:cmd url="profile" link="${pilot}" className="bld">${pilot.name}</el:cmd></td>
<c:if test="${isHR}"><td class="sec bld">${pilot.equipmentType}</td></c:if>
 <td class="small"><fmt:int value="${pilot.legs}" /> legs, <fmt:dec value="${pilot.hours}" /> hours</td>
 <td class="pri small"><fmt:int value="${pilot.ACARSLegs}" /> legs, <fmt:dec value="${pilot.ACARSHours}" /> hours</td>
 <td class="sec small"><fmt:int value="${pilot.onlineLegs}" /> legs, <fmt:dec value="${pilot.onlineHours}" /> hours</td>
 <td><fmt:date fmt="d" date="${pilot.lastFlight}" default="-" /></td>
</view:row>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title caps">
 <td colspan="${isHR ? 8 : 9 }">&nbsp;</td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
