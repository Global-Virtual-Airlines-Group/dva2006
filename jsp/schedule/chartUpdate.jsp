<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Approach Chart - ${chart.name}</title>
<content:css name="main" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:if test="${isDelete}">
<!-- Approach Chart Deleted -->
<div class="updateHdr">Approach Chart Deleted</div>
<br />
The Approach Chart <span class="pri bld caps">${chart.name}</span> for <fmt:airport airport="${chart.airport}" /> has been successfully removed from the database.<br />
</c:if>

<c:if test="${isCreate}">
<!-- Approach Chart Created -->
<div class="updateHdr">Approach Chart Created</div>
<br />
The Approach Chart <span class="pri bld caps">${chart.name}</span> for ${chart.airport.name} (<fmt:airport airport="${chart.airport}" />) has been successfully added to the database.<br />
<br />
To view this Approach Chart, <el:cmd url="chart" link="${chart}">Click Here</el:cmd>.<br />
To view all Approach Charts for ${chart.airport.name}, <el:cmd url="charts" linkID="${chart.airport.IATA}">Click Here</el:cmd>.<br />
</c:if>

<c:if test="${isUpdate}">
<!-- Approach Chart Updated -->
<div class="updateHdr">Approach Chart Updated</div>
<br />
The Approach Chart <span class="pri bld caps">${chart.name}</span> for ${chart.airport.name} (<fmt:airport airport="${chart.airport}" />) has been successfully updated.<br />
<br />
To view this Approach Chart, <el:cmd url="chart" link="${chart}">Click Here</el:cmd>.<br />
To view all Approach Charts for ${chart.airport.name}, <el:cmd url="charts" linkID="${chart.airport.IATA}">Click Here</el:cmd>.<br />
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
