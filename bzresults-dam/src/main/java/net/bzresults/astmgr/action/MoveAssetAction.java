package net.bzresults.astmgr.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import net.bzresults.astmgr.AssetManager;

import org.apache.commons.fileupload.FileUploadException;

public class MoveAssetAction implements IDAMAction {
	private HttpServletRequest request;
	private AssetManager am;

	public MoveAssetAction(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() throws FileUploadException, IOException, Exception {
		Long folderId;
		String assetName = request.getParameter("name");
		String strFolderId = request.getParameter("toid");
		if (assetName != null && strFolderId != null) {
			folderId = Long.valueOf(strFolderId);
			am.moveAsset(assetName, folderId);
		} else
			throw new Exception("Invalid/missing 'name' and/or 'toid' parameters");
	}
}
