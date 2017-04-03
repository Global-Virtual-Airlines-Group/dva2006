<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Status Comment</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.comment, l:4, t:'Status Comment'});
golgotha.form.submit(f);
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<c:set var="cspan" value="6" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="statuscomment.do" link="${pilot}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<%@ include file="/jsp/pilot/pilotStatusUpdate.jspf" %>
<tr class="title">
 <td colspan="${cspan + 1}" class="left caps">NEW STATUS COMMENT FOR ${pilot.name}</td>
</tr>
<tr class="mid">
 <td colspan="${cspan + 1}"><el:textbox name="comment" idx="*" width="80%" height="3" className="req" resize="true" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SAVE COMMENT" /></td>
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
