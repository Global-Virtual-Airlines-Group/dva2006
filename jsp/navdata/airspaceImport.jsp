<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Airway Data Import</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script>
golgotha.local.validate = function(f) {
    if (!golgotha.form.check()) return false;
    golgotha.form.validate({f:f.img, ext:['txt','gz'], t:'Airspace Data'});
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
<content:enum var="types" className="org.deltava.beans.navdata.AirspaceType" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="airspaceimport.do" method="post" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">AIRSPACE BOUNDARY DATA UPLOAD</td>
</tr>
<tr>
 <td class="label">Current Cycle</td>
 <td class="data"><span class="pri bld">${currentNavCycle}</span><c:if test="${!empty currentNavCycle.releasedOn}">, released on <fmt:date date="${currentNavCycle.releasedOn}" fmt="d" d="EEEE MMMM dd, YYYY" /></c:if></td>
</tr>
<tr>
 <td class="label">Country</td>
 <td class="data"><el:combo name="country" idx="*" size="1"  options="${countries}" firstEntry="[ SELECT COUNTRY]" value="US" /></td>
</tr>
<tr>
 <td class="label top">Airspace Types</td>
 <td class="data"><el:check name="types" idx="*" width="90" options="${types}" /></td>
</tr>
<tr>
 <td class="label">Upload Data File</td>
 <td class="data"><el:file name="navData" idx="*" className="small req" size="80" max="144" />&nbsp;<span class="small ita">Airspace data must be in OpenAir Format.</span></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="doPurge" idx="*" className="small" value="true" checked="true" label="Purge Airspace data before import" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="UPLOAD AIRSPACE DATA" /></td>
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
