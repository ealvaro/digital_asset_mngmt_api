package net.bzresults.astmgr.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import net.bzresults.astmgr.AssetManager;
import net.bzresults.astmgr.AssetManagerException;

import org.apache.commons.fileupload.FileUploadException;

public class MoveFolderAction implements IDAMAction {
	private HttpServletRequest request;
	private AssetManager am;

	public MoveFolderAction(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() throws AssetManagerException, FileUploadException, IOException, Exception {
		Long folderId, toFolderId;
		String strfolderId = request.getParameter("id");
		String strtoFolderId = request.getParameter("toid");
		if (strfolderId != null && strtoFolderId != null) {
			folderId = Long.valueOf(strfolderId);
			toFolderId = Long.valueOf(strtoFolderId);
			am.moveFolder(folderId, toFolderId);
		} else
			throw new Exception("Invalid/missing 'id' and/or 'toid' parameters");
	}
}
