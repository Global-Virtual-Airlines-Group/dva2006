<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Client Build Statistics</title>
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<content:json />
<content:googleJS module="charts" />
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
<el:form action="acarsbuilds.do" method="post" validate="return false">
<el:table className="form">
<tr class="title">
 <td class="label">Number of Months</td>
 <td class="data"><el:combo name="count" value="${param.count}" firstEntry="[ MONTHS ]" options="${months}" /> months</td>
</tr>
<tr>
 <td colspan="2"><div id="clientStats" style="height:550px;"></div></td>
</tr>

<!-- Table Footer Bar -->
<tr class="bar">
 <td colspan="2">&nbsp;</td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
google.charts.load('current', {'packages':['corechart']});
golgotha.local.showChart = function() {
	if (golgotha.local.chartData) return false;

	var xmlreq = new XMLHttpRequest();
	xmlreq.open('get', 'acarsbuildstats.ws', true);
	xmlreq.onreadystatechange = function() {
		if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
		var js = JSON.parse(xmlreq.responseText);
		js.splice(0, 1); 
		golgotha.local.chartData = js.reverse();
		return golgotha.local.renderChart();
	};

	xmlreq.send(null);
	return true;
};

golgotha.local.renderChart = function() {
	var lgStyle = {color:'black',fontName:'Verdana',fontSize:8};

	
	
	
	return true;
};
</script>
<content:googleAnalytics />
</body>
</html>
