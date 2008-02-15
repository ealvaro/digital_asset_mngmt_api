package net.bzresults.astmgr.services;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.bzresults.astmgr.AssetManager;
import net.bzresults.astmgr.AssetManagerException;
import net.bzresults.astmgr.XMLAssetManager;
import net.bzresults.astmgr.action.AddAssetTagAction;
import net.bzresults.astmgr.action.ChangeToFolderAction;
import net.bzresults.astmgr.action.ChangeToParentAction;
import net.bzresults.astmgr.action.CreateAssetAction;
import net.bzresults.astmgr.action.CreateUserFolderAction;
import net.bzresults.astmgr.action.DeleteAssetAction;
import net.bzresults.astmgr.action.DeleteAssetTagAction;
import net.bzresults.astmgr.action.DeleteFolderAction;
import net.bzresults.astmgr.action.FindAssetsByNameAction;
import net.bzresults.astmgr.action.FindAssetsByTagAction;
import net.bzresults.astmgr.action.IDAMAction;
import net.bzresults.astmgr.action.MoveAssetAction;
import net.bzresults.astmgr.action.MoveFolderAction;
import net.bzresults.astmgr.action.QueryFolderAction;
import net.bzresults.astmgr.action.RenameAssetAction;
import net.bzresults.astmgr.action.UpdateAssetTitle;

import org.apache.commons.fileupload.FileUploadException;

public class AssetManagerServlet extends HttpServlet {
	private static final long serialVersionUID = -6204882870976083593L;

	private static final String AM_PARAM = "am";

	private static final String ERROR_TAG = "error";

	private static final String MSG_TAG = "msg";

	/**
	 * Constructor of the object.
	 */
	public AssetManagerServlet() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/xml");
		PrintWriter out = response.getWriter();
		HttpSession session = request.getSession();
		AssetManager am = (AssetManager) session.getAttribute(AM_PARAM);
		String strClient = request.getParameter("clientid");
		String action = request.getParameter("action");
		if (am == null) {
			if (strClient == null)
				strClient = "20";
			am = createAMSession(session, out, strClient);
			if (action == null) {
				XMLAssetManager.sendXMLStructure(out, am.getRoot());
			}
		} else {
			if (action != null) {
				try {
					processAction(request, am, action);
					// http://localhost:8080/damw/servlet/assetmanager?action=closeSession
					if (action.equals("closeSession")) {
						XMLAssetManager.sendXMLMsg(out, MSG_TAG, "Session for clientid:" + am.getCurrentClientId()
								+ " has been closed.");
						am = null;
						session.setAttribute(AM_PARAM, am);
					} else
						XMLAssetManager.sendXMLResponse(out, am.getCurrentFolder());
				} catch (AssetManagerException ame) {
					XMLAssetManager.sendXMLMsg(out, ERROR_TAG, ame.getMessage());
				} catch (FileUploadException fue) {
					XMLAssetManager.sendXMLMsg(out, ERROR_TAG, "Multiple upload for clientid:"
							+ am.getCurrentClientId() + " had an error.");
				} catch (Exception e) {
					XMLAssetManager.sendXMLMsg(out, ERROR_TAG, "Error writing uploaded files for clientid:"
							+ am.getCurrentClientId());
				}
			} else
				XMLAssetManager.sendXMLStructure(out, am.getRoot());
			// sendXMLMsg(out, ERROR_TAG, "Session for clientid:" +
			// am.getCurrentClientId()+ " is already created. 'action' parameter
			// missing.");
		}
		out.flush();
		out.close();
	}

	private AssetManager createAMSession(HttpSession session, PrintWriter out, String strClient) {
		AssetManager am = null;
		long clientId = Long.parseLong(strClient);
		am = new AssetManager("", clientId);
		session.setAttribute(AM_PARAM, am);
		return am;
	}

	private void processAction(HttpServletRequest request, AssetManager am, String action) throws FileUploadException,
			IOException, Exception, AssetManagerException {
		IDAMAction damAction = null;
		// ?action=createUserFolder&name=testingfolder
		if (action.equals("createUserFolder")) {
			damAction = new CreateUserFolderAction(request, am);
		} else
		// ?action=moveFolder&name=testingfolder&toname=My%20Images
		if (action.equals("moveFolder")) {
			damAction = new MoveFolderAction(request, am);
		} else
		// ?action=changeToFolder&name=My%20Images
		if (action.equals("changeToFolder")) {
			damAction = new ChangeToFolderAction(request, am);
		} else
		// ?action=createAsset&name1=JUnit%20Test%20Asset.jpg&file1=%2FJUnit%20Test%20Asset.jpg
		if (action.equals("createAsset")) {
			damAction = new CreateAssetAction(request, am);
		} else
		// ?action=renameAsset&name=JUnit%20Test%20Asset.jpg&toname=My%20Picture.jpg
		if (action.equals("renameAsset")) {
			damAction = new RenameAssetAction(request, am);
		} else
		// ?action=updateAssetTitle&name=My%20Picture.jpg&title=File%20under%20testingfolder
		if (action.equals("updateAssetTitle")) {
			damAction = new UpdateAssetTitle(request, am);
		} else
		// ?action=moveAsset&name=My%20Picture.jpg&toname=testingfolder
		if (action.equals("moveAsset")) {
			damAction = new MoveAssetAction(request, am);
		} else
		// ?action=deleteFolder&name=testingfolder
		if (action.equals("deleteFolder")) {
			damAction = new DeleteFolderAction(request, am);
		} else
		// ?action=deleteAsset&name=My%20Picture.jpg
		if (action.equals("deleteAsset")) {
			damAction = new DeleteAssetAction(request, am);
		} else
		// ?action=changeToParent
		if (action.equals("changeToParent")) {
			damAction = new ChangeToParentAction(request, am);
		} else
		// ?action=queryFolder&name=recent
		if (action.equals("queryFolder")) {
			damAction = new QueryFolderAction(request, am);
		} else
		// ?action=addAssetTag&name=My%20Picture.jpg&tag=make&value=Ford
		if (action.equals("addAssetTag")) {
			damAction = new AddAssetTagAction(request, am);
		} else
		// ?action=deleteAssetTag&name=My%20Picture.jpg&tag=make
		if (action.equals("deleteAssetTag")) {
			damAction = new DeleteAssetTagAction(request, am);
		} else
		// ?action=findAssetsByName&name=My%20Picture.jpg
		if (action.equals("findAssetsByName")) {
			damAction = new FindAssetsByNameAction(request, am);
		} else
		// ?action=findAssetsByTag&tag=make&value=Ford
		if (action.equals("findAssetsByTag")) {
			damAction = new FindAssetsByTagAction(request, am);
		}
		if (damAction != null)
			damAction.execute();
	}

	/**
	 * The doPost method of the servlet. <br>
	 * 
	 * This method is called when a form has its tag value method equals to
	 * post.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException
	 *             if an error occure
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
