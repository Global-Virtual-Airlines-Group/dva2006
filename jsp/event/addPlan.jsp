<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Online Event Flight Plan</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateCombo(form.airportD, 'Departure Airport')) return false;
if (!validateFile(form.planFile, '<fmt:list value="${planExts}" delim="," />', 'Flight Plan')) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/event/header.jsp" %> 
<%@include file="/jsp/event/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:form action="eventplan.do" linkID="0x${event.ID}" method="POST" allowUpload="true" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">FLIGHT PLAN - ${event.name}</td>
</tr>
<tr>
 <td class="label">Departure Airport</td>
 <td class="data"><el:combo name="airportD" idx="*" size="1" options="${event.airportD}" firstEntry="" /></td>
</tr>
<tr>
 <td class="label">Destination Airport</td>
 <td class="data"><el:combo name="airportA" size="1" options="${airportA}" /></td>
</tr>
<tr>
 <td class="label">Flight Plan File</td>
 <td class="data"><el:file name="planFile" className="small" idx="*" size="80" max="144" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="SAVE FLIGHT PLAN" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</div>
</body>
</html>
