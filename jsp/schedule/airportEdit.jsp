<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Schedule - ${airport.IATA}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.name, 8, 'Airport Name')) return false;
if (!validateText(form.iata, 3, 'IATA Code')) return false;
if (!validateText(form.icao, 4, 'ICAO Code')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>
<content:sysdata var="airlines" name="airlines" mapValues="true" sort="true" />

<!-- Main Body Frame -->
<div id="main">
<el:form action="airport.do" method="post" linkID="${airport.IATA}" op="save" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">AIRPORT PROFILE</td>
</tr>
<tr>
 <td class="label">Airport Name</td>
 <td class="data"><el:text name="name" idx="*" className="pri bld" size="20" max="32" value="${airport.name}" /></td>
</tr>
<tr>
 <td class="label">IATA Code</td>
 <td class="data"><el:text name="iata" idx="*" className="bld" size="2" max="3" value="${airport.IATA}" /></td>
</tr>
<tr>
 <td class="label">ICAO Code</td>
 <td class="data"><el:text name="icao" idx="*" size="4" max="4" value="${airport.ICAO}" /></td>
</tr>
<tr>
 <td class="label">Latitude</td>
 <td class="data"><el:text name="latD" idx="*" size="2" max="2" value="${latD}" /> degrees 
<el:text name="latM" idx="*" size="2" max="2" value="${latM}" /> minutes 
<el:text name="latS" idx="*" size="2" max="2" value="${latS}" /> seconds 
<el:combo name="latDir" idx="*" size="1" options="${latDir}" value="${latNS}" /></td>
</tr>
<tr>
 <td class="label">Longitude</td>
 <td class="data"><el:text name="lonD" idx="*" size="2" max="4" value="${lonD}" /> degrees 
<el:text name="lonM" idx="*" size="2" max="2" value="${lonM}" /> minutes 
<el:text name="lonS" idx="*" size="2" max="2" value="${lonS}" /> seconds 
<el:combo name="lonDir" idx="*" size="1" options="${lonDir}" value="${lonEW}" /></td>
</tr>
<tr>
 <td class="label">Time Zone</td>
 <td class="data"><el:combo name="tz" size="1" idx="*" options="${timeZones}" value="${airport.TZ}" /></td>
</tr>
<tr>
 <td class="label" valign="top">Airlines</td>
 <td class="data"><el:check name="airline" idx="*" width="140" className="small" cols="5" options="${airlines}" separator="<div style=\"clear:both;\" />" checked="${airport.airlineCodes}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="SAVE AIRPORT PROFILE" />&nbsp;
<el:cmdbutton ID="DeleteButton" url="airportdelete" linkID="${airport.IATA}" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</div>
</body>
</html>
