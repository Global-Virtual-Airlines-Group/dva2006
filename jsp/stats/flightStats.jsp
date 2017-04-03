<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
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
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
golgotha.local.updateSort = function() { return document.forms[0].submit(); };
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<c:set var="sortExclude" value="${isCharter ? 'AVGHOURS,AVGMILES,OLEGS' : 'PIDS'}" scope="page" />
<content:enum var="sortTypes" className="org.deltava.beans.stats.FlightStatsSort" exclude="${sortExclude}" />
<content:enum var="groupTypes" className="org.deltava.beans.stats.FlightStatsGroup" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="${isCharter ? 'charter' : 'flight'}stats.do" method="post" validate="return true">
<view:table cmd="flightstats">
<tr class="title">
 <td colspan="5" class="left caps"><span class="nophone"><content:airline /> </span><c:if test="${isCharter}">CHARTER </c:if>FLIGHT STATISTICS</td>
 <td colspan="7" class="right">GROUP BY <el:combo name="groupType" size="1" idx="*" options="${groupTypes}" value="${groupType}" onChange="void golgotha.local.updateSort()" />
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
