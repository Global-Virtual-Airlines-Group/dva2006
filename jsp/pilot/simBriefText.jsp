<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title>SimBrief Briefing - ${pirep.flightCode}</title>
<content:expire expires="5" />
<content:css name="main" />
<content:js name="common" />
<content:googleAnalytics />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:cspHeader />
<style type="text/css">
@media print {
.noprint { display:none; }
}
</style>
</head>
<content:copyright visible="false" />
<body style="margin:8px">
<div class="noprint updateHdr">SimBrief generated Briefing data for ${pirep.flightCode}</div>
<br />
${pkg.briefingText}
<br />
<span class="noprint"><el:link url="javascript:void window.close()" className="sec bld">Click Here</el:link> to close this window.<br /></span>
<br />
<content:copyright />
</body>
</html>
