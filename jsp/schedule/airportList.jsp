<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Schedule - Airports</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function setAirline(combo)
{
self.location = '/airports.do?id=' + combo.options[combo.selectedIndex].value;
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/schedule/header.jsp" %> 
<%@include file="/jsp/schedule/sideMenu.jsp" %>
<content:sysdata var="airlines" name="airlines" mapValues="true" />

<!-- Main Body Frame -->
<div id="main">
<el:form action="airports.do" method="GET" validate="return false">
<view:table className="view" pad="default" space="default" cmd="airports">

<!-- Table Header Bar -->
<tr class="title">
 <td width="10%"><el:cmdbutton url="airport" op="edit" label="NEW AIRPORT" />
 <td width="20%">AIRPORT NAME</td>
 <td width="7%">IATA</td>
 <td width="8%">ICAO</td>
 <td width="25%">TIME ZONE</td>
 <td>AIRLINE <el:combo name="airline" idx="*" size="1" options="${airlines}" value="${airline}" onChange="void setAirline(this)" /></td>
</tr>

<!-- Table Airport Data -->
<c:forEach var="airport" items="${viewContext.results}">
<view:row entry="${airport}">
 <td class="pri bld" colspan="2"><el:cmd url="airport" linkID="${airport.IATA}" op="edit">${airport.name}</el:cmd></td>
 <td class="bld">${airport.IATA}</td>
 <td class="bld">${airport.ICAO}</td>
 <td class="sec small">${airport.TZ}</td>
 <td><fmt:geo pos="${airport.position}" /></td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="6"><view:pgUp />&nbsp;<view:pgDn /></td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</div>
</body>
</html>
