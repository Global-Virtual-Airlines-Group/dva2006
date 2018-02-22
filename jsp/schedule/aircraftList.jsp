<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
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
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="aircraftlist">

<!-- Table Header Bar -->
<tr class="title">
 <td style="width:15%">AIRCRAFT NAME</td>
 <td class="nophone" style="width:20%">IATA CODE(S)</td>
 <td class="nophone" style="width:8%">&nbsp;</td>
 <td class="nophone" style="width:10%">WEIGHT</td>
 <td style="width:10%">RANGE</td>
 <td class="nophone" style="width:10%">SEATS</td>
 <td style="width:15%">VIRTUAL AIRLINES</td>
 <td><c:if test="${ac.canCreate}"><el:cmdbutton url="aircraft" op="edit" label="NEW AIRCRAFT" /></c:if>&nbsp;</td>
</tr>

<!-- Table Aircraft Data -->
<c:forEach var="aircraft" items="${viewContext.results}">
<c:set var="access" value="${accessMap[aircraft]}" scope="page" />
<view:row entry="${aircraft}">
<c:set var="opName" value="${access.canEdit ? 'edit' : null}" scope="page" />
 <td><el:cmd url="aircraft" linkID="${aircraft.name}" op="${opName}" className="pri bld">${aircraft.name}</el:cmd></td>
 <td class="nophone"><fmt:list value="${aircraft.IATA}" delim=", " /></td>
 <td class="small pri bld nophone">${aircraft.ETOPS ? 'ETOPS' : '&nbsp;'}</td>
<c:if test="${aircraft.maxWeight > 0}">
 <td class="small sec bld nophone">OK</td>
</c:if>
<c:if test="${aircraft.maxWeight == 0}">
 <td class="small nophone">N / A</td>
</c:if>
 <td class="pri bld"><fmt:distance value="${aircraft.range}" /></td>
 <td class="nophone"><fmt:int value="${aircraft.seats}" /></td>
 <td colspan="2" class="sec"><fmt:list value="${aircraft.apps}" delim=", " /></td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="8"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /><br/></view:scrollbar>
<view:legend width="110" labels="Historic,Current,No Fuel Profile" classes="opt1, ,warn" /></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
