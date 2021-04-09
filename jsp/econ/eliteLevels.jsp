<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="eliteName" name="econ.elite.name" />
<html lang="en">
<head>
<title><content:airline /> - ${eliteName} Status Levels</title>
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
<view:table cmd="elitelevels">

<!-- Table Header Bar -->
<tr class="title caps">
 <td colspan="4" class="left"><content:airline />&nbsp;${eliteName} STATUS LEVELS</td>
 <td colspan="4" class="right"><el:cmd url="elitelevel">NEW STATUS LEVEL</el:cmd></td>
</tr>

<tr class="title">
 <td>NAME</td>
 <td style="width:10%">YEAR</td>
 <td style="width:10%">LEGS</td>
 <td style="width:15%">DISTANCE</td>
 <td style="width:15%">POINTS</td>
 <td style="width:10%">POINT BONUS</td>
 <td style="width:10%">TARGET %ILE</td>
 <td style="width:10%">&nbsp;</td>
</tr>

<!-- Table Airline Data -->
<c:forEach var="lvl" items="${viewContext.results}">
<tr>
 <td style="color:#ffffff; background-color:#${lvl.hexColor}"><el:cmd url="elitelevel" className="bld" linkID="${lvl.name}-${lvl.year}" op="edit">${lvl.name}</el:cmd></td>
 <td class="bld">${lvl.year}</td>
 <td class="pri bld"><fmt:int value="${lvl.legs}" />
 <td><fmt:distance value="${lvl.distance}" />
 <td><fmt:int value="${lvl.points}" />
 <td><fmt:dec value="${lvl.bonusFactor * 100.0}" />%</td>
 <td class="bld">${lvl.targetPercentile}</td>
 <td class="sec bld">${lvl.isVisible ? 'VISIBLE' : 'HIDDEN'}</td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="8"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
