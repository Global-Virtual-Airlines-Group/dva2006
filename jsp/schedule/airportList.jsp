<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Flight Schedule - Airports</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function updateSort()
{
var f = document.forms[0];
f.action = '/airports.do';
f.submit();
return true;
}

function validate(form)
{
var apCode = form.id.value.toUpperCase();
if (apCode.length < 3) {
	alert('Please select a valid ICAO or IATA airport code.');
	form.id.focus();
	return false;
}

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
<view:table className="view" pad="default" space="default" cmd="airports">

<!-- Table Header Bar -->
<tr class="title">
 <td width="11%"><el:cmdbutton url="airport" op="edit" label="NEW AIRPORT" /></td>
 <td width="14%">AIRPORT NAME</td>
 <td width="7%">IATA</td>
 <td width="7%">ICAO</td>
 <td width="15%">EDIT <el:text name="id" idx="*" size="3" max="4" value="" />
 <el:button ID="EditButton" type="submit" className="BUTTON" label="GO" /></td>
 <td width="10%">TIME ZONE</td>
 <td class="right">SORT BY <el:combo name="sortType" idx="*" size="1" options="${sortOptions}" value="${param.sortType}" onChange="void updateSort()" /> 
 AIRLINE <el:combo name="airline" idx="*" size="1" options="${airlines}" value="${airline}" onChange="void updateSort()" /></td>
</tr>

<!-- Table Airport Data -->
<c:forEach var="airport" items="${viewContext.results}">
<tr>
 <td class="pri bld" colspan="2"><el:cmd url="airport" linkID="${airport.IATA}" op="edit">${airport.name}</el:cmd></td>
 <td class="bld">${airport.IATA}</td>
 <td class="bld">${airport.ICAO}</td>
 <td class="sec small" colspan="2">${airport.TZ}</td>
 <td><fmt:geo pos="${airport.position}" /></td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="7"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar></td>
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
