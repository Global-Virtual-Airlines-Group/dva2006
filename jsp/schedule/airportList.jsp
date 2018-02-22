<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
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
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script>
golgotha.local.updateSort = function() {
	var f = document.forms[0];
	f.action = '/airports.do';
	return f.submit();
};

golgotha.local.validate = function(f) {
    if (f.id.value.length < 3) throw new golgotha.event.ValidationError('Please select a valid ICAO or IATA airport code.', f.id);
    f.id.value = f.id.value.toUpperCase();
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
<el:form action="airport.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<view:table cmd="airports">

<!-- Table Header Bar -->
<tr class="title">
 <td style="width:11%"><el:cmdbutton url="airport" op="edit" label="NEW AIRPORT" /></td>
 <td style="width:11%">NAME</td>
 <td style="width:6%">IATA</td>
 <td style="width:6%">ICAO</td>
 <td style="width:12%">EDIT <el:text name="id" idx="*" size="3" max="4" value="" /> <el:button ID="EditButton" type="submit" label="GO" /></td>
 <td style="width:11">TIME ZONE</td>
 <td colspan="3" class="right"><el:cmd url="airportexport">EXPORT</el:cmd> | SORT BY <el:combo name="sortType" idx="*" size="1" options="${sortOptions}" value="${param.sortType}" onChange="void golgotha.local.updateSort()" /> 
 AIRLINE <el:combo name="airline" idx="*" size="1" options="${airlines}" value="${airline}" onChange="void golgotha.local.updateSort()" /></td>
</tr>

<!-- Table Airport Data -->
<c:forEach var="airport" items="${viewContext.results}">
<view:row entry="${airport}">
 <td class="pri bld" colspan="2"><el:cmd url="airport" linkID="${airport.IATA}" op="edit">${airport.name}</el:cmd></td>
 <td class="bld">${airport.IATA}</td>
 <td class="bld">${airport.ICAO}</td>
 <td class="sec small" colspan="2">${airport.TZ}</td>
 <td class="small nophone">${airport.country}</td>
 <td class="small"><fmt:int value="${airport.maximumRunwayLength}" /> ft</td>
 <td class="small nophone"><fmt:geo pos="${airport.position}" /></td>
</view:row>
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
