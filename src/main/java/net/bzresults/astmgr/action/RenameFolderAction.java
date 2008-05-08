package net.bzresults.astmgr.action;

import javax.servlet.http.HttpServletRequest;

import net.bzresults.astmgr.AssetManager;

public class RenameFolderAction implements IDAMAction {
	private HttpServletRequest request;
	private AssetManager am;

	public RenameFolderAction(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() throws Exception {
		Long folderId;
		String strFolderId = request.getParameter("id");
		String toName = request.getParameter("toname");
		if (strFolderId != null && toName != null) {
			folderId = Long.valueOf(strFolderId);
			am.renameUserFolder(folderId, toName);
		} else
			throw new Exception("Invalid/missing 'name' and/or 'toname' parameters");
	}
}
