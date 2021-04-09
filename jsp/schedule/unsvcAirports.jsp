<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Unserviced Airports</title>
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:pics />
<content:favicon />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	if (!confirm("Are you sure you wish to continue?")) return false;
	golgotha.form.submit(f);
	return true;
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
<el:form method="post" action="usvcairports.do" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> UNSERVICED AIRPORTS</td>
</tr>
<tr>
 <td colspan="2" class="left">
<c:if test="${totalResults == 0}">
<div class="pri bld">There are no Airports listed for an Airline without at least one corresponding entry in the <content:airline /> Flight Schedule.</div>
</c:if>
<c:forEach var="airline" items="${fn:keys(results)}">
<c:set var="airports" value="${results[airline]}" scope="page" />
The following <fmt:int value="${fn:sizeof(airports)}" /> airports are no longer served by <span class="pri bld">${airline.name}</span>:<br />
<br />
<c:forEach var="airport" items="${airports}">
<el:cmd url="airport" linkID="${airport.IATA}" op="edit" className="bld">${airport.name}</el:cmd> (${airport.ICAO} / ${airport.IATA})<br />
</c:forEach>
<hr />
</c:forEach>
 </td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="updateDB" idx="*" value="true" label="Update Airports in Database" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="UPDATE AIRPORTS" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
