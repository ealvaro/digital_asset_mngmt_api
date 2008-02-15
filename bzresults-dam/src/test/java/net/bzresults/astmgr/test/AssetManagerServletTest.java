package net.bzresults.astmgr.test;

import java.io.IOException;

import javax.servlet.ServletException;

import junit.framework.TestCase;
import net.bzresults.astmgr.services.AssetManagerServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class AssetManagerServletTest extends TestCase {

	private static final Log log = LogFactory.getLog(AssetManagerServlet.class);

	public AssetManagerServletTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testDoGetHttpServletRequestHttpServletResponse() {
		// http://localhost:8087/dam/damsvc?action=updateAssetDescription&name=My%20Picture.jpg&desc=File%20under%20testingfolder
		// http://localhost:8087/dam/damsvc?action=deleteAsset&name=My%20Picture.jpg
		// http://localhost:8087/dam/damsvc?action=closeSession
		MockHttpServletRequest mockHttpServletRequest = null;
		MockHttpServletResponse mockHttpServletResponse = null;
		AssetManagerServlet amserv = new AssetManagerServlet();
		try {
			mockHttpServletRequest = new MockHttpServletRequest("GET", "/dam/damsvc");
			// http://localhost:8087/dam/damsvc?clientid=20
			mockHttpServletRequest.setParameter("clientid", "20");
			amserv.doGet(mockHttpServletRequest, mockHttpServletResponse = new MockHttpServletResponse());
			log.debug(mockHttpServletResponse.getContentAsString());
			// http://localhost:8087/dam/damsvc?action=createUserFolder&name=testingfolder
			mockHttpServletRequest.setParameter("action", "createUserFolder");
			mockHttpServletRequest.setParameter("name", "testingfolder");
			amserv.doGet(mockHttpServletRequest, mockHttpServletResponse = new MockHttpServletResponse());
			log.debug(mockHttpServletResponse.getContentAsString());
			// http://localhost:8087/dam/damsvc?action=moveFolder&name=testingfolder&toname=My%20Images
			mockHttpServletRequest.setParameter("action", "moveFolder");
			mockHttpServletRequest.setParameter("name", "testingfolder");
			mockHttpServletRequest.setParameter("toname", "My Images");
			amserv.doGet(mockHttpServletRequest, mockHttpServletResponse = new MockHttpServletResponse());
			log.debug(mockHttpServletResponse.getContentAsString());
			// http://localhost:8087/dam/damsvc?action=changeToFolder&name=My%20Images
			mockHttpServletRequest.setParameter("action", "changeToFolder");
			mockHttpServletRequest.setParameter("name", "My Images");
			amserv.doGet(mockHttpServletRequest, mockHttpServletResponse = new MockHttpServletResponse());
			log.debug(mockHttpServletResponse.getContentAsString());
			// http://localhost:8087/dam/damsvc?action=createAsset&name1=JUnit%20Test%20Asset.jpg&file1=%2FJUnit%20Test%20Asset.jpg
			mockHttpServletRequest.setParameter("action", "createAsset");
			mockHttpServletRequest.setParameter("name1", "JUnit Test Asset");
			mockHttpServletRequest.setParameter("file1", "/JUnit Test Asset.jpg");
			amserv.doGet(mockHttpServletRequest, mockHttpServletResponse = new MockHttpServletResponse());
			log.debug(mockHttpServletResponse.getContentAsString());
			// http://localhost:8087/dam/damsvc?action=updateAssetTitle&name=JUnit%20Test%20Asset.jpg&title=Personal%20Picture
			mockHttpServletRequest.setParameter("action", "updateAssetTitle");
			mockHttpServletRequest.setParameter("name", "JUnit Test Asset");
			mockHttpServletRequest.setParameter("title", "Personal Picture");
			amserv.doGet(mockHttpServletRequest, mockHttpServletResponse = new MockHttpServletResponse());
			log.debug(mockHttpServletResponse.getContentAsString());
			// http://localhost:8087/dam/damsvc?action=addAssetTag&name=JUnit%20Test%20Asset.jpg&tag=MAKE&value=Ford
			mockHttpServletRequest.setParameter("action", "addAssetTag");
			mockHttpServletRequest.setParameter("name", "JUnit Test Asset");
			mockHttpServletRequest.setParameter("tag", "MAKE");
			mockHttpServletRequest.setParameter("value", "Ford");
			amserv.doGet(mockHttpServletRequest, mockHttpServletResponse = new MockHttpServletResponse());
			log.debug(mockHttpServletResponse.getContentAsString());
			// http://localhost:8087/dam/damsvc?action=renameAsset&name=JUnit%20Test%20Asset.jpg&toname=My%20Picture.jpg
			mockHttpServletRequest.setParameter("action", "renameAsset");
			mockHttpServletRequest.setParameter("name", "JUnit Test Asset");
			mockHttpServletRequest.setParameter("toname", "My Picture");
			amserv.doGet(mockHttpServletRequest, mockHttpServletResponse = new MockHttpServletResponse());
			log.debug(mockHttpServletResponse.getContentAsString());
			// http://localhost:8087/dam/damsvc?action=moveAsset&name=My%20Picture.jpg&toname=testingfolder
			mockHttpServletRequest.setParameter("action", "moveAsset");
			mockHttpServletRequest.setParameter("name", "My%20Picture.jpg");
			mockHttpServletRequest.setParameter("toname", "testingfolder");
			amserv.doGet(mockHttpServletRequest, mockHttpServletResponse = new MockHttpServletResponse());
			log.debug(mockHttpServletResponse.getContentAsString());
			// http://localhost:8087/dam/damsvc?action=deleteFolder&name=testingfolder
			mockHttpServletRequest.setParameter("action", "deleteFolder");
			mockHttpServletRequest.setParameter("name", "testingfolder");
			amserv.doGet(mockHttpServletRequest, mockHttpServletResponse = new MockHttpServletResponse());
			log.debug(mockHttpServletResponse.getContentAsString());
			// http://localhost:8087/dam/damsvc?action=changeToParent
			mockHttpServletRequest.setParameter("action", "changeToParent");
			amserv.doGet(mockHttpServletRequest, mockHttpServletResponse = new MockHttpServletResponse());
			log.debug(mockHttpServletResponse.getContentAsString());
		} catch (ServletException se) {
			fail("Servlet Exception");
		} catch (IOException ioe) {
			fail("I/O Exception");
		}
		assert (true);
	}

	public void testDoPostHttpServletRequestHttpServletResponse() {
		MockHttpServletRequest mockHttpServletRequest = null;
		MockHttpServletResponse mockHttpServletResponse = null;
		AssetManagerServlet amserv = new AssetManagerServlet();
		try {
			mockHttpServletRequest = new MockHttpServletRequest("POST", "/dam/dam");
			// http://localhost:8087/dam/damsvc?clientid=20
			mockHttpServletRequest.setParameter("clientid", "20");
			amserv.doPost(mockHttpServletRequest, mockHttpServletResponse = new MockHttpServletResponse());
			log.debug(mockHttpServletResponse.getContentAsString());
			// http://localhost:8087/dam/damsvc?action=createAsset&name1=JUnit%20Test%20Asset.jpg&file1=%2FJUnit%20Test%20Asset.jpg
			mockHttpServletRequest.setParameter("action", "createAsset");
			mockHttpServletRequest.setParameter("name1", "JUnit Test Asset.jpg");
			mockHttpServletRequest.setParameter("file1", "/JUnit Test Asset.jpg");
			mockHttpServletRequest.setContentType("multipart/form-data");
			amserv.doPost(mockHttpServletRequest, mockHttpServletResponse = new MockHttpServletResponse());
			log.debug(mockHttpServletResponse.getContentAsString());
		} catch (ServletException se) {
			fail("Servlet Exception");
		} catch (IOException ioe) {
			fail("I/O Exception");
		}
		assert (true);
	}

}
