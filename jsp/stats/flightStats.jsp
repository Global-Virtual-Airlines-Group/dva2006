<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Flight Statistics</title>
<content:css name="main" />
<content:css name="view" />
<content:css name="form" />
<content:js name="common" />
<content:js name="tableSort" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script async>
golgotha.local.updateSort = function() { return document.forms[0].submit(); };
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<c:set var="sortExclude" value="${isCharter ? 'AVGHOURS,AVGMILES,OVLEGS,OILEGS' : 'OVLEGS,OILEGS,PIDS'}" scope="page" />
<content:enum var="sortTypes" className="org.deltava.beans.stats.FlightStatsSort" exclude="${sortExclude}" />
<content:enum var="groupTypes" className="org.deltava.beans.stats.FlightStatsGroup" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="${isCharter ? 'charter' : 'flight'}stats.do" method="post" validate="return true">
<view:table cmd="flightstats">
<tr class="title">
 <td colspan="${noTours ? 6 : 7}" class="left caps"><span class="nophone"><content:airline />&nbsp;</span><c:if test="${isCharter}">CHARTER </c:if>FLIGHT STATISTICS</td>
 <td colspan="6" class="right">GROUP BY <el:combo name="groupType" size="1" idx="*" options="${groupTypes}" value="${groupType}" onChange="void golgotha.local.updateSort()" />
 SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${viewContext.sortType}" onChange="void golgotha.local.updateSort()" /></td>
</tr>
<%@ include file="/jsp/stats/pirepStats.jspf" %>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
