package net.bzresults.astmgr.action;

import javax.servlet.http.HttpServletRequest;

import net.bzresults.astmgr.AssetManager;

public class RenameAssetAction implements IDAMAction {
	private HttpServletRequest request;
	private AssetManager am;

	public RenameAssetAction(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() {
		String assetName = request.getParameter("name");
		String toName = request.getParameter("toname");
		am.renameAsset(assetName, toName);

	}

}
