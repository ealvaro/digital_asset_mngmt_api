package net.bzresults.astmgr.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileUploadException;

import net.bzresults.astmgr.AssetManager;

public class ProtectAssetAction implements IDAMAction {
	private HttpServletRequest request;
	private AssetManager am;

	public ProtectAssetAction(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() throws FileUploadException, IOException, Exception {
		String assetName = request.getParameter("name");
		if (assetName != null)
			am.protectAsset(assetName);

	}

}
