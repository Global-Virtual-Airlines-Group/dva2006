<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Fleet Gallery</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:sysdata var="imgDB" name="airline.db" />
<script language="JavaScript" type="text/javascript">
function selectAircraft(combo)
{
// Do nothing if nothing selected
if (combo.selectedIndex == 0) return false;

// Get the image object and its description object
var img = getElement('FleetPic');
var desc = getElement('FleetDesc');

// Load the picture in its place, save the description
img.src = '/gallery/${imgDB}/0x' + combo.options[combo.selectedIndex].value + '.jpg';
desc.innerHTML = dList[combo.selectedIndex - 1];

// Blur the combo box
desc.focus();
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@include file="/jsp/main/header.jspf" %> 
<%@include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="fleet.do" method="get" validate="return false">
<el:table className="form" pad="default" space="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> FLEET GALLERY</td>
</tr>
<tr>
 <td class="label">Select Aircraft</td>
 <td><el:combo name="Aircraft" size="1" idx="1" options="${fleetGallery}" onChange="void selectAircraft(this)" /></td>
</tr>
<tr>
 <td class="label">Description</td>
 <td><span id="FleetDesc"></span></td>
</tr>
<tr valign="middle">
 <td colspan="2" class="mid"><el:img ID="FleetPic" x="600" y="450" src="blank.png" caption="Fleet Gallery" /></td>
</tr>
<tr class="title">
 <td colspan="2">&nbsp;</td>
</tr>
</el:table>
<el:text ID="fleetDescs" name="Descriptions" type="HIDDEN" value="${fleetGalleryDesc}" />
</el:form>
<content:copyright />
</content:region>
</content:page>
<script language="JavaScript" type="text/javascript">
var dList = getElement('fleetDescs').value.split(',');
</script>
<content:googleAnalytics />
</body>
</html>
