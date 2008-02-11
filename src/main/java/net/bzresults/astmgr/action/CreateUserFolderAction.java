package net.bzresults.astmgr.action;

import javax.servlet.http.HttpServletRequest;

import net.bzresults.astmgr.AssetManager;

public class CreateUserFolderAction implements IDAMAction {
	private HttpServletRequest request;
	private AssetManager am;

	public CreateUserFolderAction(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() {
		String folderName = request.getParameter("name");
		am.createUserFolder((folderName == null ? "test_folder" : folderName));

	}

}
