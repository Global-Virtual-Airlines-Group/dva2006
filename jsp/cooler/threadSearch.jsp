<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>New <content:airline /> Water Cooler Search</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.searchStr, 3, 'Search Term')) return false;

setSubmit();
disableButton('SearchButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onload="void enableButton('SearchButton', true)">
<content:page>
<%@ include file="/jsp/cooler/header.jsp" %> 
<%@ include file="/jsp/cooler/sideMenu.jsp" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="coolersearch.do" method="post" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
<tr class="title">
 <td colspan="2" class="left caps">Water Cooler Search</td>
</tr>
<tr>
 <td class="label">Search String</td>
 <td class="data"><el:text name="searchStr" idx="*" size="20" className="pri bld req" max="34" value="${param.searchStr}" /></td>
</tr>
<tr>
 <td class="label">Cooler Channel</td>
 <td class="data"><el:combo name="channel" idx="*" size="1" options="${channels}" /></td>
</tr>
<tr>
 <td class="label">Pilot Name</td>
 <td class="data"><el:text name="pilotName" idx="*" size="16" max="32" value="${param.pilotName}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="checkSubject" idx="*" value="true" label="Check Subjects as well as Message Body" checked="${param.checkSubject}" /><br />
<el:box name="nameMatch" idx="*" value="true" label="Partial Pilot Name match" checked="${param.nameMatch}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH WATER COOLER" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
