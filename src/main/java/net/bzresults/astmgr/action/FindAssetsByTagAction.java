package net.bzresults.astmgr.action;

import javax.servlet.http.HttpServletRequest;

import net.bzresults.astmgr.AssetManager;

public class FindAssetsByTagAction implements IDAMAction {
	private HttpServletRequest request;
	private AssetManager am;

	public FindAssetsByTagAction(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() {
		String tagName = request.getParameter("tag");
		String tagValue = request.getParameter("value");
		am.findAssetsByTag(tagName, tagValue);

	}

}
