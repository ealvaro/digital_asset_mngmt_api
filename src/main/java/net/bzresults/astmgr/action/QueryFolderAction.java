package net.bzresults.astmgr.action;

import javax.servlet.http.HttpServletRequest;

import net.bzresults.astmgr.AssetManager;

public class QueryFolderAction implements IDAMAction {
	private HttpServletRequest request;
	private AssetManager am;

	public QueryFolderAction(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() {
		String queryName = request.getParameter("name");
		am.virtualFolder(queryName);

	}

}
