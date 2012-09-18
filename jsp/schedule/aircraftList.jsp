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
<title><content:airline /> Schedule - Aircraft</title>
<content:css name="main" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" cmd="aircraftlist">

<!-- Table Header Bar -->
<tr class="title">
 <td style="width:15%">AIRCRAFT NAME</td>
 <td style="width:20%">IATA CODE</td>
 <td style="width:5%">&nbsp;</td>
 <td style="width:5%">WEIGHT</td>
 <td style="width:10%">SEATS</td>
 <td style="width:25%">WEB APPLICATIONS</td>
 <td><el:cmdbutton url="aircraft" op="edit" label="NEW AIRCRAFT" /></td>
</tr>

<!-- Table Aircraft Data -->
<c:forEach var="aircraft" items="${aircraftInfo}">
<view:row entry="${aircraft}">
 <td><el:cmd url="aircraft" linkID="${aircraft.name}" op="edit" className="pri bld">${aircraft.name}</el:cmd></td>
 <td><fmt:list value="${aircraft.IATA}" delim=", " /></td>
 <td class="small pri bld">${aircraft.ETOPS ? 'ETOPS' : '&nbsp;'}</td>
<c:if test="${aircraft.maxWeight > 0}">
 <td class="small sec bld">OK</td>
</c:if>
<c:if test="${aircraft.maxWeight == 0}">
 <td class="small">N / A</td>
</c:if>
 <td><fmt:int value="${aircraft.seats}" /></td>
 <td colspan="2" class="sec"><fmt:list value="${aircraft.apps}" delim=", " /></td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="7"><view:legend width="100" labels="Historic,Current" classes="opt1, " /></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
