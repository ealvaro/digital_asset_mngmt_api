package net.bzresults.astmgr.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.bzresults.astmgr.AssetManager;
import net.bzresults.astmgr.model.DAMFolder;

public class QueryFolderAction implements IDAMAction {
	
	private static final String OEM_PARAM = "oemlogos";

	private HttpServletRequest request;
	private AssetManager am;

	public QueryFolderAction(HttpServletRequest request, AssetManager am) {
		super();
		this.request = request;
		this.am = am;
	}

	public void execute() {
		String queryName = request.getParameter("name");
		if (queryName.equals("oemlogos")) {
			HttpSession session = request.getSession();
			DAMFolder oem =(DAMFolder) session.getAttribute(OEM_PARAM);
			if (oem == null) {
				am.virtualFolder(queryName);
				session.setAttribute(OEM_PARAM, am.getCurrentFolder());
			} else
				am.setCurrentFolder(oem);
		} else
			am.virtualFolder(queryName);
	}

}
