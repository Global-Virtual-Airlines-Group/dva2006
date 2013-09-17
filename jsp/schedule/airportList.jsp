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
<title><content:airline /> Flight Schedule - Airports</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
function updateSort()
{
var f = document.forms[0];
f.action = '/airports.do';
f.submit();
return true;
}

function validate(form)
{
var apCode = form.id;
if (apCode.value.length < 3) {
	alert('Please select a valid ICAO or IATA airport code.');
	apCode.focus();
	return false;
}

apCode.value = apCode.value.toUpperCase();
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="airport.do" method="post" validate="return validate(this)">
<view:table cmd="airports">

<!-- Table Header Bar -->
<tr class="title">
 <td style="width:11%"><el:cmdbutton url="airport" op="edit" label="NEW AIRPORT" /></td>
 <td style="width:11%">AIRPORT NAME</td>
 <td style="width:6%">IATA</td>
 <td style="width:6%">ICAO</td>
 <td style="width:14%">EDIT <el:text name="id" idx="*" size="3" max="4" value="" /> <el:button ID="EditButton" type="submit" label="GO" /></td>
 <td style="width:9%">TIME ZONE</td>
 <td colspan="3" class="right">SORT BY <el:combo name="sortType" idx="*" size="1" options="${sortOptions}" value="${param.sortType}" onChange="void updateSort()" /> 
 AIRLINE <el:combo name="airline" idx="*" size="1" options="${airlines}" value="${airline}" onChange="void updateSort()" /></td>
</tr>

<!-- Table Airport Data -->
<c:forEach var="airport" items="${viewContext.results}">
<tr>
 <td class="pri bld" colspan="2"><el:cmd url="airport" linkID="${airport.IATA}" op="edit">${airport.name}</el:cmd></td>
 <td class="bld">${airport.IATA}</td>
 <td class="bld">${airport.ICAO}</td>
 <td class="sec small" colspan="2">${airport.TZ}</td>
 <td class="small">${airport.country}</td>
 <td class="small"><fmt:int value="${airport.maximumRunwayLength}" /> ft</td>
 <td class="small"><fmt:geo pos="${airport.position}" /></td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="9"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<el:text name="op" type="hidden" value="edit" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
