<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Schedule - ${airline.code}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.name, l:6, t:'Airline Name'});
golgotha.form.validate({f:f.code, l:2, t:'Airline Code'});
golgotha.form.validate({f:f.color, t:'Airline Google Map Color'});
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
<content:sysdata var="airlines" name="apps" mapValues="true" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="airline.do" method="post" linkID="${airline.code}" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">AIRLINE PROFILE</td>
</tr>
<tr>
 <td class="label">Airline Name</td>
 <td class="data"><el:text name="name" idx="*" className="pri bld" required="true" size="24" max="32" value="${airline.name}" /></td>
</tr>
<tr>
 <td class="label">Airline Code</td>
 <td class="data"><el:text name="code" idx="*" className="bld" required="true" size="3" max="3" value="${airline.code}" /></td>
</tr>
<tr>
 <td class="label">Map Color</td>
 <td class="data"><el:combo name="color" idx="*" size="1" required="true" options="${colors}" firstEntry="-" value="${airline.color}" /></td>
</tr>
<tr>
 <td class="label top">Web Applications</td>
 <td class="data"><el:check name="airlines" width="180" options="${airlines}" checked="${airline.applications}" /></td>
</tr>
<c:if test="${airportCount > 0}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><fmt:int value="${airportCount}" /> Airports served by this Airline</td>
</tr>
</c:if>
<tr>
 <td class="label top">Alternate Codes</td>
 <td class="data"><el:textbox name="altCodes" idx="*" width="50" height="3">${altCodes}</el:textbox></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="active" idx="*" value="true" label="Airline is Active" checked="${airline.active}" /><br />
 <el:box name="historic" idx="*" value="true" label="This is a Historic Airline" checked="${airline.historic}" /><br />
 <el:box name="sync" idx="*" value="true" label="Synchronize schedule with other Web Applications" checked="${airline.scheduleSync}" /> </td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SAVE AIRLINE PROFILE" /></td>
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
