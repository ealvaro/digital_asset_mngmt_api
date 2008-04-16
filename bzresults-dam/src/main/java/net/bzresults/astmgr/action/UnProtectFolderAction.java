package net.bzresults.astmgr.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileUploadException;

import net.bzresults.astmgr.AssetManager;

public class UnProtectFolderAction implements IDAMAction {
	private HttpServletRequest request;
	private AssetManager am;

	public UnProtectFolderAction(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() throws FileUploadException, IOException, Exception {
		Long folderId = Long.valueOf(request.getParameter("id"));
		am.unProtectFolder(folderId);
	}

}
