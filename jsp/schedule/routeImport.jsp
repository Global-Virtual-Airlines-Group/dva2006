<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Preferred Route Import</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.routeData, ext:['csv'], t:'FAA Preferred Route Data'});
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
<el:form action="routeimport.do" method="post" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">FAA PREFERRED ROUTE DATA UPLOAD</td>
</tr>
<tr>
 <td class="label top">Upload CSV File</td>
 <td class="data"><el:file name="routeData" idx="*" className="small req" size="80" max="144" /><br />
<span class="small">FAA Preferred Route data must be in CSV format, with the following fields in the file:<br />
<span class="bld ita">Orig, Route String, Dest, Hours1, Hours2, Hours3, Type, Area, Altitude, Aircraft, Direction, Seq,DCNTR,ACNTR</span>.<br />
This is a total of 14 tokens, and we are only importing #1, #2, #3, #13 and #14.</span></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="UPLOAD PREFERRED ROUTE DATA" /></td>
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
