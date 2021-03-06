package net.bzresults.astmgr.action;

import javax.servlet.http.HttpServletRequest;

import net.bzresults.astmgr.AssetManager;
import net.bzresults.astmgr.AssetManagerException;

public class CreateUserFolderAction implements IDAMAction {
	private HttpServletRequest request;
	private AssetManager am;

	public CreateUserFolderAction(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() throws AssetManagerException, Exception {
		String folderName = request.getParameter("name");
		if (folderName != null)
			am.createUserFolder(folderName);
		else
			throw new Exception("Invalid/missing 'name' parameter");
	}
}
