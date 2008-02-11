package net.bzresults.astmgr.action;

import javax.servlet.http.HttpServletRequest;

import net.bzresults.astmgr.AssetManager;

public class FindAssetsByNameAction implements IDAMAction {
	private HttpServletRequest request;
	private AssetManager am;

	public FindAssetsByNameAction(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() {
		String assetName = request.getParameter("name");
		am.findAssetsByName(assetName);

	}

}
