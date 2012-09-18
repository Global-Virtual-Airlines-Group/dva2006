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
<content:pics />
<script type="text/javascript">
function updateSort()
{
document.forms[0].submit();
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="${isCharter ? 'charter' : 'flight'}stats.do" method="post" validate="return true">
<view:table className="view" cmd="flightstats">
<tr class="title">
 <td colspan="4" class="left caps"><content:airline /> <c:if test="${isCharter}">CHARTER </c:if>FLIGHT STATISTICS</td>
 <td colspan="7" class="right">GROUP BY <el:combo name="groupType" size="1" idx="*" options="${groupTypes}" value="${param.groupType}" onChange="void updateSort()" />
 SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${viewContext.sortType}" onChange="void updateSort()" />
<c:if test="${!isCharter}"> <el:box name="activeOnly" idx="*" value="true" checked="${param.activeOnly}" label="Active Pilots Only" /></c:if></td>
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
