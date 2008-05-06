package net.bzresults.astmgr.action;

import javax.servlet.http.HttpServletRequest;

import net.bzresults.astmgr.AssetManager;
import net.bzresults.astmgr.AssetManagerException;

public class ChangeToFolderAction implements IDAMAction {
	private HttpServletRequest request;
	private AssetManager am;

	public ChangeToFolderAction(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() throws AssetManagerException, Exception {
		Long folderId;
		String strFolderId = request.getParameter("id");
		if (strFolderId != null) {
			folderId = Long.valueOf(strFolderId);
			am.changeToFolder(folderId);
		} else
			throw new Exception("Invalid/missing 'id' parameter");
	}
}
