<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Approach Chart - ${chart.name}</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
<style type="text/css">
@media print 
{
.noPrint
{
display:none;
}

.CHART
{
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
<span class="noPrint"><a href="javascript:window.print()">Print Chart</a><br /></span>
<span class="CHART"><img alt="${chart.name}, ${chart.size} bytes" src="/charts/${chart.ID}" border="0" /></span>
<div class="noPrint"><content:copyright /></div>
</body>
</html>
