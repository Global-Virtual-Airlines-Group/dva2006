<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
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
<content:js name="common" />
<script type="text/javascript">
golgotha.local.dataFiles = ['pssawy.dat','pssawy.dat.gz'];
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
var isOK = false;
fName = f.navData.value.substring(f.navData.value.lastIndexOf('\\') + 1).toLowerCase();
for (var x = 0; x < golgotha.local.dataFiles.length && !isOK; x++)
	isOK |= (fName == golgotha.local.dataFiles[x]);
	
if (!isOK)
	throw new golgotha.event.ValidationError('This does not appear to be a valid PSS AIRAC data file.', f.navData);

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

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="awyimport.do" method="post" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">AIRAC NAVIGATION DATA UPLOAD</td>
</tr>
<tr>
 <td class="label">Current Cycle</td>
 <td class="data"><span class="pri bld">${currentNavCycle}</span><c:if test="${!empty currentNavCycle.releasedOn}">, released on 
 <fmt:date date="${currentNavCycle.releasedOn}" fmt="d" d="EEEE MMMM dd, YYYY" /></c:if></td>
</tr>
<tr>
 <td class="label">Upload Data File</td>
 <td class="data"><el:file name="navData" idx="*" className="small req" size="80" max="144" />&nbsp;
<span class="small ita">AIRAC data must be in PSS Format.</span></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="doPurge" idx="*" className="small" value="true" checked="true" label="Purge Airway Data before import" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="UPLOAD AIRAC AIRWAY DATA" /></td>
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
