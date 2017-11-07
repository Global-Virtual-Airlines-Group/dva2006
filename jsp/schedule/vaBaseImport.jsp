<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> VABase Schedule Import</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<script>
golgotha.local.validate = function(f)
{
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.csvData, ext:['csv'], t:'Flight Schedule data'});
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
<el:form action="vaimport.do" method="post" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title">
 <td colspan="2">VABase FLIGHT SCHEDULE DATA IMPORT</td>
</tr>
<tr>
 <td class="label top">Upload Data File</td>
 <td class="data"><el:file name="csvData" idx="*" className="small req" size="80" max="144" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isTailCodes" value="true" label="This is VABase Fleet Tail Code data" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="UPLOAD FLIGHT SCHEDULE DATA" /></td>
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
