<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<html lang="en">
<head>
<title><content:airline /> Approach Chart - ${chart.name}</title>
<content:css name="main" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<style type="text/css">
@media print 
{
.noPrint {
	display:none;
}

.CHART {
	display:inline;
	text-align:center;
	position:absolute;
	top: -5px;
}
}
</style>
</head>
<content:copyright visible="false" />
<body>
<span class="noPrint"><a href="javascript:void window.print()">Print Chart</a><br /></span>
<span class="CHART"><img alt="${chart.name}, ${chart.size} bytes" src="/charts/${chart.ID}" class="noborder" /></span>
<div class="noPrint"><content:copyright /></div>
<content:googleAnalytics />
</body>
</html>
