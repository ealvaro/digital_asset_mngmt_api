package net.bzresults.astmgr.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileUploadException;

import net.bzresults.astmgr.AssetManager;

public class ProtectFolderAction implements IDAMAction {
	private HttpServletRequest request;
	private AssetManager am;

	public ProtectFolderAction(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() throws FileUploadException, IOException, Exception {
		String folderName = request.getParameter("name");
		if (folderName != null)
			am.protectFolder(folderName);

	}

}