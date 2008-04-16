<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<title>Digital Asset Manager Simulator - An ajax-based
			simulation page for seeing xml responses as though flex app made the
			call</title>
		<script src="/dam/scripts/prototype.js" type="text/javascript"></script>
		<script src="/dam/scripts/glue_ajax.js" type="text/javascript"></script>
		<style type="text/css">
body {
	font-family: Verdana;
	font-size: 10pt
}

li {
	margin: .35em 0
}

div#leftCol {
	float: left;
	margin: 3px 10px;
	width: 50%;
	height: 100%
}

div#rightCol {
	margin: 3px 10px;
}

.highlight {
	background-color: #FFFFDD;
	font-weight: bold;
	padding: 2px;
}
</style>
		<script type="text/javascript" language="javascript1.2">

function openWin(url) {
  window.open(url);
}

function showSessionInfo()
{  var win = window.open('','sessionInfo',"width=750,height=500,scrollbars=yes,status=yes,toolbar=no,menubar=no,location=no");
   win.document.open();
   msg = '<div style="font-family: verdana; font-size: 10pt"><h4>This is the session info when page was first loaded</h4>' ;
   msg += '<h4>ClientUser: </h4>';
   msg += 'id: <c:out value="${bccUser.id}"/> <br/>';
   msg += 'name: <c:out value="${bccUser.name}"/> <br/>';
   msg += 'username: <c:out value="${bccUser.username}"/> <br/>';
   msg += '<h4>Client</h4>id: <c:out value="${client.id}"/> <br/>';
   msg += 'name: <c:out value="${client.name}"/> <br/>';
   msg += '<h4>Current Valve</h4>';
   msg += 'id: <c:choose><c:when test="${not empty valve}"><c:out value="${valve.id}"/></c:when><c:otherwise>no valve</c:otherwise></c:choose> <br/>';
   msg += 'name: <c:choose><c:when test="${not empty valve}"><c:out value="${valve.name}"/></c:when><c:otherwise>no valve</c:otherwise></c:choose> <br/>';
   msg += 'country: <c:choose><c:when test="${not empty valve}"><c:out value="${valve.address.country}"/></c:when><c:otherwise>no valve</c:otherwise></c:choose> <br/>';
   msg += '<h4>Current Rooftop</h4>';
   msg += 'id: <c:choose><c:when test="${not empty rooftop}"><c:out value="${rooftop.id}"/></c:when><c:otherwise>no rooftop in session as key rooftop</c:otherwise></c:choose> <br/>';
   msg += 'name: <c:choose><c:when test="${not empty rooftop}"><c:out value="${rooftop.name}"/></c:when><c:otherwise>no rooftop in session as key rooftop</c:otherwise></c:choose> <br/>';
   msg += '<h4>AssetManager</h4>';
   <c:choose>
     <c:when test="${ empty sessionScope.am }">msg +='null';</c:when>
     <c:otherwise>
      msg += 'currentClientId: ${am.currentClientId} &nbsp;';
      msg += 'currentValveId: ${am.currentValveId} &nbsp;';
      msg += 'current folder id: ${am.currentFolder.id}<br/>';
     </c:otherwise>
   </c:choose> 
   win.document.write(msg);
   win.document.close();
   win.focus();
}

</script>
	</head>

	<body>
		<div id="leftCol">
			Currently simulating:
			<span class="highlight">Client: <c:out value="${client.id}" />
			</span> &nbsp;&nbsp;&nbsp;
			<a href="javascript:showSessionInfo()">Session Info</a> (must reload
			page for it to show latest)

			<p>
				Links to test/exercise service here:
				<span class="highlight">BE SURE TO START WITH initial request
					link !</span>
			</p>
			<ul>
				<li>
					<a href="javascript:void(0)" onclick="doIt('', '')">initial
						request </a> no params needed - sets up AM in session with root as
					current folder
				</li>
				<li>
					<a href="javascript:void(0)" onclick="doIt('closeSession', '')">closeSession
					</a> no params needed - nulls out AM in session
				</li>
				<li>
					<a href="javascript:void(0)" onclick="doIt('changeToParent', '')">changeToParent</a>
					- no param
				</li>
				<br />

				<li>
					Call to
					<b>createUserFolder</b> requires name param.
					<br />
					<blockquote>
						- Folder will be created in current folder
						<form style="padding: 6px 0">
							<table>
								<tr>
									<td>
										Folder Name:
									</td>
									<td>
										<input type="text" name="createUserFolderName" size="20" value="testingfolder" />
									</td>
								</tr>
								<tr>
									<td colspan="2">
										<input type="button" value="Call createUserFolder"
											onclick="doCreateUserFolderCall(this.form)" />
									</td>
								</tr>
							</table>
						</form>
					</blockquote>
				</li>
				<li>
					Call to
					<b>moveFolder</b> requires id and toid param.
					<br />
					<blockquote>
						- Folder name has to exist in current folder
						<form style="padding: 6px 0">
							<table>
								<tr>
									<td>
										Folder from Id:
									</td>
									<td>
										<input type="text" name="moveFolderId" size="10" value="" />
									</td>
								</tr>
								<tr>
									<td>
										Folder to Id:
									</td>
									<td>
										<input type="text" name="moveToFolderId" size="10" value="" />
									</td>
								</tr>
								<tr>
									<td colspan="2">
										<input type="button" value="Call moveFolder"
											onclick="doMoveToFolderCall(this.form)" />
									</td>
								</tr>
							</table>
						</form>
					</blockquote>
				</li>
				<li>
					Call to
					<b>changeToFolder</b> requires id param.
					<br />
					<blockquote>
						<form style="padding: 6px 0">
							<table>
								<tr>
									<td>
										Folder Id:
									</td>
									<td>
										<input type="text" name="changeToFolderId" size="10" value="" />
									</td>
								</tr>
								<tr>
									<td>
										&nbsp;
										<input type="button" value="Call changeToFolder"
											onclick="doChangeToFolderCall(this.form)" />
									</td>
								</tr>
							</table>
						</form>
					</blockquote>
				</li>
				<li>
					Call to
					<b>deleteFolder</b> requires id param.
					<br />
					<blockquote>
						<form style="padding: 6px 0">
							<table>
								<tr>
									<td>
										Folder Id:
									</td>
									<td>
										<input type="text" name="deleteFolderId" size="10" value="" />
									</td>
								</tr>
								<tr>
									<td>
										&nbsp;
										<input type="button" value="Call deleteFolder"
											onclick="doDeleteFolderCall(this.form)" />
									</td>
								</tr>
							</table>
						</form>
					</blockquote>
				</li>
				<li>
					Call to
					<b>protectFolder</b> requires id param.
					<br />
					<blockquote>
						<form style="padding: 6px 0">
							<table>
								<tr>
									<td>
										Folder Id:
									</td>
									<td>
										<input type="text" name="protectFolderId" size="10" value="" />
									</td>
								</tr>
								<tr>
									<td>
										&nbsp;
										<input type="button" value="Call protectFolder"
											onclick="doProtectFolderCall(this.form)" />
									</td>
								</tr>
							</table>
						</form>
					</blockquote>
				</li>
				<li>
					Call to
					<b>unProtectFolder</b> requires id param.
					<br />
					<blockquote>
						<form style="padding: 6px 0">
							<table>
								<tr>
									<td>
										Folder Id:
									</td>
									<td>
										<input type="text" name="unProtectFolderId" size="10" value="" />
									</td>
								</tr>
								<tr>
									<td>
										&nbsp;
										<input type="button" value="Call unProtectFolder"
											onclick="doUnProtectFolderCall(this.form)" />
									</td>
								</tr>
							</table>
						</form>
					</blockquote>
				</li>
				<li>
					<a href="javascript:openWin('/dam/uploadFiles.html')">createAsset</a>
					(Opens new window since can't upload via ajax)
					<br />
					- While this works in firefox or IE, the flex app For now, only
					works in IE
					<br />
					- Remember asset is created in whatever folder is the current
					folder in your DAM session
					<br />
					<br />
				</li>
				<li>
					Call to
					<b>renameAsset</b> requires name and toname param (and to be the owner).
					<br />
					<blockquote>
						- Asset has to exist in current folder
						<form style="padding: 6px 0">
							<table>
								<tr>
									<td>
										Asset Name:
									</td>
									<td>
										<input type="text" name="assetName" size="20" value="" />
									</td>
								</tr>
								<tr>
									<td>
										New Name:
									</td>
									<td>
										<input type="text" name="assetToName" size="20" value="" />
									</td>
								</tr>
								<tr>
									<td colspan="2">
										<input type="button" value="Call renameAsset"
											onclick="doRenameAssetCall(this.form)" />
									</td>
								</tr>
							</table>
						</form>
					</blockquote>
				</li>
				<li>
					Call to
					<b>deleteAsset</b> requires name param (and to be the owner).
					<br />
					<blockquote>
						<form style="padding: 6px 0">
							Asset Name:
							<input type="text" name="deleteAssetName" size="20" value="" />
							&nbsp;
							<input type="button" value="Call deleteAsset"
								onclick="doDeleteAssetCall(this.form)" />
						</form>
					</blockquote>
				</li>
				<li>
					Call to
					<b>protectAsset</b> requires asset name param (and to be the owner).
					<br />
					<blockquote>
						<form style="padding: 6px 0">
							<table>
								<tr>
									<td>
										Asset Name:
									</td>
									<td>
										<input type="text" name="protectAssetName" size="20" value="" />
									</td>
								</tr>
								<tr>
									<td>
										&nbsp;
										<input type="button" value="Call protectAsset"
											onclick="doProtectAssetCall(this.form)" />
									</td>
								</tr>
							</table>
						</form>
					</blockquote>
				</li>
				<li>
					Call to
					<b>unProtectAsset</b> requires asset name param (and to be the owner).
					<br />
					<blockquote>
						<form style="padding: 6px 0">
							<table>
								<tr>
									<td>
										Asset Name:
									</td>
									<td>
										<input type="text" name="unProtectAssetName" size="20" value="" />
									</td>
								</tr>
								<tr>
									<td>
										&nbsp;
										<input type="button" value="Call unprotectAsset"
											onclick="doUnProtectAssetCall(this.form)" />
									</td>
								</tr>
							</table>
						</form>
					</blockquote>
				</li>
				<li>
					Call to
					<b>moveAsset</b> requires name for the asset and id for the folder.
					<br />
					<blockquote>
						- Folder name has to exist in current folder
						<form style="padding: 6px 0">
							<table>
								<tr>
									<td>
										Asset name to move (from current folder):
									</td>
									<td>
										<input type="text" name="moveAssetName" size="20" value="" />
									</td>
								</tr>
								<tr>
									<td>
										Folder to Id:
									</td>
									<td>
										<input type="text" name="moveToFolderId" size="10" value="" />
									</td>
								</tr>
								<tr>
									<td colspan="2">
										<input type="button" value="Call moveAssetFolder"
											onclick="doMoveAssetToFolderCall(this.form)" />
									</td>
								</tr>
							</table>
						</form>
					</blockquote>
				</li>
				<li>
					Call to
					<b>addAssetTag</b> requires name, tag and value parameters
					<br />
					<blockquote>
						<form style="padding: 6px 0">
							<table>
								<tr>
									<td>
										Asset Name:
									</td>
									<td>
										<input type="text" name="addAssetName" size="20" value="" />
									</td>
								</tr>
								<tr>
									<td>
										Asset Tag:
									</td>
									<td>
										<input type="text" name="addAssetTag" size="20" value="" />
									</td>
								</tr>
								<tr>
									<td>
										Tag Value:
									</td>
									<td>
										<input type="text" name="addAssetValue" size="20" value="" />
									</td>
								</tr>
								<tr>
									<td>
										&nbsp;
										<input type="button" value="Call Add Asset Tag"
											onclick="doAddAssetTagCall(this.form)" />
									</td>
								</tr>
							</table>
						</form>
					</blockquote>
				</li>
				<li>
					Call to
					<b>deleteAssetTag</b> requires name and tag parameters
					<br />
					<blockquote>
						<form style="padding: 6px 0">
							<table>
								<tr>
									<td>
										Asset Name:
									</td>
									<td>
										<input type="text" name="deleteAssetName" size="20" value="" />
									</td>
								</tr>
								<tr>
									<td>
										Asset Tag:
									</td>
									<td>
										<input type="text" name="deleteAssetTag" size="20" value="" />
									</td>
								</tr>
								<tr>
									<td>
										&nbsp;
										<input type="button" value="Call Delete Asset Tag"
											onclick="doDeleteAssetTagCall(this.form)" />
									</td>
								</tr>
							</table>
						</form>
					</blockquote>
				</li>
				<li>
					Call to
					<b>findAssetsByName</b> requires name param.
					<br />
					<blockquote>
						<form style="padding: 6px 0">
							Asset Name:
							<input type="text" name="findAssetsName" size="20" value="" />
							&nbsp;
							<input type="button" value="Call Find Assets By Name"
								onclick="doFindAssetsByNameCall(this.form)" />
						</form>
					</blockquote>
				</li>
				<li>
					Call to
					<b>findAssetsByTag</b> requires name and tag parameters
					<br />
					<blockquote>
						<form style="padding: 6px 0">
							<table>
								<tr>
									<td>
										Asset Tag:
									</td>
									<td>
										<input type="text" name="deleteAssetTag" size="20" value="" />
									</td>
								</tr>
								<tr>
									<td>
										Tag Value:
									</td>
									<td>
										<input type="text" name="deleteTagValue" size="20" value="" />
										&nbsp;
										<input type="button" value="Call Find Assets By Tag"
											onclick="doFindAssetsByTagCall(this.form)" />
									</td>
								</tr>
							</table>
						</form>
					</blockquote>
				</li>
			</ul>
		</div>
		<div id="rightCol">
			<p style="text-align: right; font-size: 8pt; margin: 2px">
				Reload
				<a href="damSim">damSim</a>
			</p>
			<form
				onsubmit="makeCallFromTextField(this.callWithParams.value); return false;">
				<input style="font-family: verdana; font-size: 9pt; width: 450px"
					type="text" id="call" name="callWithParams"
					value="Calls will be shown here or you can put call here and submit" />
				<input type="button" value="Submit Call"
					onclick="makeCallFromTextField(this.form.callWithParams.value)" />
			</form>
			<h4 style="margin: 2px 10px">
				Output
			</h4>
			<textarea id="output" rows="60" cols="105">XML Response will be output here...
</textarea>
		</div>
	</body>
</html>
