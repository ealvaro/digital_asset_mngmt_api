package net.bzresults.astmgr.services;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.bzresults.astmgr.AllFilesFilter;
import net.bzresults.astmgr.AssetManager;
import net.bzresults.astmgr.AssetManagerException;
import net.bzresults.astmgr.XMLAssetManager;
import net.bzresults.astmgr.action.AddAssetTagAction;
import net.bzresults.astmgr.action.ChangeToFolderAction;
import net.bzresults.astmgr.action.ChangeToParentAction;
import net.bzresults.astmgr.action.CreateAssetAction;
import net.bzresults.astmgr.action.CreateUserFolderAction;
import net.bzresults.astmgr.action.DeleteAssetAction;
import net.bzresults.astmgr.action.DeleteAssetTagNameAction;
import net.bzresults.astmgr.action.DeleteAssetTagValueAction;
import net.bzresults.astmgr.action.DeleteFolderAction;
import net.bzresults.astmgr.action.FindAssetsByNameAction;
import net.bzresults.astmgr.action.FindAssetsByTagAction;
import net.bzresults.astmgr.action.IDAMAction;
import net.bzresults.astmgr.action.MoveAssetAction;
import net.bzresults.astmgr.action.MoveFolderAction;
import net.bzresults.astmgr.action.ProtectAssetAction;
import net.bzresults.astmgr.action.ProtectFolderAction;
import net.bzresults.astmgr.action.QueryFolderAction;
import net.bzresults.astmgr.action.RenameAssetAction;
import net.bzresults.astmgr.action.UnProtectAssetAction;
import net.bzresults.astmgr.action.UnProtectFolderAction;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class AssetManagerServlet extends HttpServlet {
	private static final long serialVersionUID = -6204882870976083593L;
	private static final Log log = LogFactory.getLog(AssetManagerServlet.class);

	private static final String AM_PARAM = "am";

	private static final String ERROR_TAG = "error";
	private static final String MSG_TAG = "msg";

	public static final String CLIENT_KEY = "client";
	public static final String VALVE_KEY = "valve";
	public static final String BCC_USER_KEY = "bccUser";
	public static final String ACTION_KEY = "action";

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
		// Client client = (Client) session.getAttribute(CLIENT_KEY);
		// Valve currentValve = (Valve) session.getAttribute(VALVE_KEY);
		// ClientUser user = (ClientUser) session.getAttribute(BCC_USER_KEY);
		// ClientUser user = WebHelper.getBccUser(request.getSession());
		String strClient = request.getParameter(CLIENT_KEY);
		String strValve = request.getParameter(VALVE_KEY);
		String strUser = request.getParameter(BCC_USER_KEY);
		String action = request.getParameter(ACTION_KEY);
		if (strClient == null || strClient.equals(""))
			if (session.getAttribute(CLIENT_KEY) == null)
				strClient = "20";
			else
				strClient = ((Long) session.getAttribute(CLIENT_KEY)).toString();
		if (strValve == null || strValve.equals(""))
			if (session.getAttribute(VALVE_KEY) == null)
				strValve = "V54A";
			else
				strValve = (String) session.getAttribute(VALVE_KEY);
		if (strUser == null || strUser.equals(""))
			if (session.getAttribute(BCC_USER_KEY) == null)
				strUser = "1";
			else
				strUser = ((Long) session.getAttribute(BCC_USER_KEY)).toString();
		action = (action == null) ? "" : action;
		String clientIdDebugStr = strClient;
		String actionDebugStr = (action == null) ? "null" : action;
		String amDebugStr = (am == null) ? "am is null at start of request" : am.getCurrentClientId() + " "
				+ am.getCurrentValveId();
		String amCurFolderDebugStr = (am == null) ? "am is null" : am.getCurrentFolder().getId() + " "
				+ am.getCurrentFolder().getName();
		log.debug("*******************************************************************************"
				+ "\n IN AssetManagerController handleRequestInternal have:\nsessionId: " + session.getId()
				+ " isNew: " + session.isNew() + "\n client in session: " + clientIdDebugStr + "\n action: "
				+ actionDebugStr + "\n am in session: " + amDebugStr + "\n" + amCurFolderDebugStr + "\n\n");

		if (needToCreateAMSession(am, strClient, strValve)) {
			am = createAMSession(session, out, strClient, strValve, strUser);
		}
		if (action == null || action.equals("")) {
			XMLAssetManager.sendXMLStructure(out, am.getCurrentFolder());
		} else {
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
				XMLAssetManager.sendXMLMsg(out, ERROR_TAG, "Multiple upload for clientid:" + am.getCurrentClientId()
						+ " had an error.");
			} catch (Exception e) {
				e.printStackTrace();
				XMLAssetManager.sendXMLMsg(out, ERROR_TAG, "Error: " + e.getMessage() + " \ncurrent client id: "
						+ am.getCurrentClientId());
			}
		}
		out.flush();
		out.close();
	}

	private boolean needToCreateAMSession(AssetManager am, String client, String valve) {
		if (am == null)
			return true;
		boolean sameClient = am.getCurrentClientId().equals(Long.parseLong(client));
		boolean sameValve = am.getCurrentValveId().equals(valve);
		return !(sameClient && sameValve);
	}

	private AssetManager createAMSession(HttpSession session, PrintWriter out, String strClient, String strValve,
			String strUser) {
		AssetManager am = null;
		long clientId = Long.parseLong(strClient);
		long cuserId = Long.parseLong(strUser);
		am = new AssetManager(strValve, clientId, cuserId);
		session.setAttribute(AM_PARAM, am);
		session.setAttribute(CLIENT_KEY, clientId);
		session.setAttribute(VALVE_KEY, strValve);
		session.setAttribute(BCC_USER_KEY, cuserId);
		log.debug("****** put new am in session with clientId : " + am.getCurrentClientId() + " valveId : "
				+ am.getCurrentValveId());
		return am;
	}

	private void processAction(HttpServletRequest request, AssetManager am, String action) throws FileUploadException,
			IOException, Exception, AssetManagerException {
		IDAMAction damAction = null;
		if (action.equals("browseBZAssets")) {
			getBZFolderContents(request.getParameter("currentBZFolder"));
		}
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
		// if (action.equals("updateAssetTitle")) {
		// damAction = new UpdateAssetTitle(request, am);
		// } else
		// ?action=moveAsset&name=My%20Picture.jpg&toname=testingfolder
		if (action.equals("moveAsset")) {
			damAction = new MoveAssetAction(request, am);
		} else
		// ?action=protectAsset&name=My%20Picture.jpg
		if (action.equals("protectAsset")) {
			damAction = new ProtectAssetAction(request, am);
		} else
		// ?action=unprotectAsset&name=My%20Picture.jpg
		if (action.equals("unprotectAsset")) {
			damAction = new UnProtectAssetAction(request, am);
		} else
		// ?action=protectFolder&name=test_folder
		if (action.equals("protectFolder")) {
			damAction = new ProtectFolderAction(request, am);
		} else
		// ?action=unprotectFolder&name=test_folder
		if (action.equals("unprotectFolder")) {
			damAction = new UnProtectFolderAction(request, am);
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
		// ?action=deleteAssetTagName&name=My%20Picture.jpg&tag=make
		if (action.equals("deleteAssetTagName")) {
			damAction = new DeleteAssetTagNameAction(request, am);
		} else
		// ?action=deleteAssetTagValue&name=My%20Picture.jpg&value=Ford
		if (action.equals("deleteAssetTagValue")) {
			damAction = new DeleteAssetTagValueAction(request, am);
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

	public String getBZFolderContents(String folder) {
		String root = "/var/www/bzwebs/assets/bz";
		if (folder != null) {
			folder = root + "/" + folder;
		} else {
			folder = root;
		}

		File folderList = new File(folder);

		FileFilter filter = new AllFilesFilter();
		File[] listing = folderList.listFiles(filter);
		org.dom4j.Document dom = org.dom4j.DocumentHelper.createDocument(DocumentHelper.createElement("folder"));

		for (File item : listing) {
			if (item.isDirectory())
				dom.getRootElement().add(createFolderElement(item));

			else
				dom.getRootElement().add(createFileElement(item));
		}

		return dom.asXML();
	}

	public Element createFolderElement(File file) {
		Element element = DocumentHelper.createElement("folder");
		element.addAttribute("name", file.getName());

		return element;

	}

	public Element createFileElement(File file) {
		Element element = DocumentHelper.createElement("asset");
		element.addAttribute("file_name", file.getName());

		return element;

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
	 *             if an error occur
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
