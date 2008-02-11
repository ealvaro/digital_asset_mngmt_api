package net.bzresults.astmgr.action;

import javax.servlet.http.HttpServletRequest;

import net.bzresults.astmgr.AssetManager;

public class ChangeToParentAction implements IDAMAction {
	private HttpServletRequest request;
	private AssetManager am;

	public ChangeToParentAction(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() {
		am.changeToParent();
	}
}
