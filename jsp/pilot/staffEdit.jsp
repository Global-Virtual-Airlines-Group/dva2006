<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<c:if test="${access.canChangeStaffProfile && (!empty staff)}">
<!-- Edit Staff Profile -->
<tr class="title caps">
 <td colspan="${cspan + 1}">STAFF PROFILE</td>
</tr>
<tr>
 <td class="label">Title</td>
 <c:if test="${access.canChangeRoles}">
 <td colspan="${cspan}" class="data"><el:text className="bld req" name="staffTitle" value="${staff.title}" size="48" max="64" /></td>
 </c:if>
 <c:if test="${!access.canChangeRoles}">
 <td colspan="${cspan}" class="data bld">${staff.title}</td>
 </c:if>
</tr>
<tr>
 <td class="label" valign="top">Biographical Profile</td>
 <td colspan="${cspan}" class="data"><el:textbox name="staffBody" height="4" width="100" className="req">${staff.body}</el:textbox></td>
</tr>
<c:if test="${access.canChangeRoles}">
<tr>
 <td class="label">Sort Order</td>
 <td colspan="${cspan}" class="data"><el:text name="staffSort" value="${staff.sortOrder}" className="req" size="1" max="1" />
 <span class="small"><input type="checkbox" name="removeStaff" value="1" />Remove Staff Profile</span></td>
</tr>
</c:if>
</c:if>
<c:if test="${access.canChangeRoles && (empty staff)}">
<!-- Create Staff Profile -->
<tr class="title">
 <td colspan="${cspan + 1}">STAFF PROFILE</td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td colspan="${cspan}" class="data">${pilot.name} is not currently listed on the Staff Roster. 
<el:cmd url="newstaff" linkID="0x${pilot.ID}">Click Here</el:cmd> to create a new Staff Profile.</td>
</tr>
</c:if>
