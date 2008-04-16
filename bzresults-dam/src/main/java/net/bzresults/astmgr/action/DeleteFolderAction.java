package net.bzresults.astmgr.action;

import javax.servlet.http.HttpServletRequest;

import net.bzresults.astmgr.AssetManager;
import net.bzresults.astmgr.AssetManagerException;

public class DeleteFolderAction implements IDAMAction {
	private HttpServletRequest request;
	private AssetManager am;

	public DeleteFolderAction(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() throws AssetManagerException {
		String strFolderID = request.getParameter("id");
		if (!(strFolderID == null)) {
			Long folderID = Long.parseLong(strFolderID);
			am.deleteFolder(folderID);
		}
	}
}
